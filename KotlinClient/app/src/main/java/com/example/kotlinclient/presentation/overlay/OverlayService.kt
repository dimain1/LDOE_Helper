package com.example.kotlinclient.presentation.overlay

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.graphics.PixelFormat
import android.graphics.Point
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.kotlinclient.MainActivity
import com.example.kotlinclient.R
import com.example.kotlinclient.state_management.utility.EventAlarmScheduler
import com.example.kotlinclient.state_management.repository.UserSessionProvider
import com.example.kotlinclient.state_management.repository.interfaces.EventRepository
import com.example.kotlinclient.state_management.repository.interfaces.EventTemplateRepository
import com.example.kotlinclient.ui.theme.KotlinClientTheme
import com.example.kotlinclient.ui.theme.ServiceFloatingButtonColor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.math.roundToInt

class OverlayService : LifecycleService(), KoinComponent, SavedStateRegistryOwner {

    companion object {
        private val _isRunning = MutableStateFlow(false)
        val isRunning: StateFlow<Boolean> = _isRunning
    }

    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    private lateinit var windowManager: WindowManager
    private lateinit var composeView: ComposeView
    private lateinit var params: WindowManager.LayoutParams
    private var overlayView: ComposeView? = null

    private val repository:       EventRepository         by inject()
    private val templateRepository: EventTemplateRepository by inject()
    private val alarmScheduler:   EventAlarmScheduler     by inject()
    private val session:          UserSessionProvider      by inject()

    private lateinit var controller: OverlayController

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        startForeground(1, createNotification())
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        controller = OverlayController(repository, templateRepository, alarmScheduler, session)
        _isRunning.value = true
        showFloatingButton()
    }

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return null
    }

    override fun onDestroy() {
        _isRunning.value = false
        if (::composeView.isInitialized && composeView.parent != null)
            windowManager.removeView(composeView)
        overlayView?.let { if (it.parent != null) windowManager.removeView(it) }
        overlayView = null
        controller.clear()
        super.onDestroy()
    }

    private fun showFloatingButton() {
        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 0; y = 200
        }

        composeView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@OverlayService)
            setViewTreeSavedStateRegistryOwner(this@OverlayService)
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

            setContent {
                KotlinClientTheme {
                    var isDragging  by remember { mutableStateOf(false) }
                    var hiddenEdge  by remember { mutableStateOf<HiddenEdge?>(null) }
                    val isHidden = hiddenEdge != null

                    val btnWidth = when (hiddenEdge) {
                        HiddenEdge.Left, HiddenEdge.Right -> 32.dp
                        null -> 48.dp
                    }
                    val btnHeight = when (hiddenEdge) {
                        HiddenEdge.Left, HiddenEdge.Right -> 44.dp
                        null -> 48.dp
                    }
                    val btnShape: Shape = if (isHidden) RoundedCornerShape(10.dp) else CircleShape
                    val iconSize = if (isHidden) 16.dp else 24.dp

                    FloatingActionButton(
                        containerColor = ServiceFloatingButtonColor,
                        shape = btnShape,
                        modifier = Modifier
                            .size(width = btnWidth, height = btnHeight)
                            .graphicsLayer { alpha = if (isHidden) 0.65f else 1f }
                            .pointerInput(Unit) {
                                detectDragGestures(
                                    onDragStart = { isDragging = false },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        isDragging = true

                                        val visibleSize  = getButtonSize(null)
                                        val currentSize  = getButtonSize(hiddenEdge)
                                        val currentBounds = getBounds(currentSize)

                                        val rawX = when (hiddenEdge) {
                                            HiddenEdge.Left  -> if (dragAmount.x > 0) currentBounds.minX else params.x.toFloat()
                                            HiddenEdge.Right -> if (dragAmount.x < 0) currentBounds.maxX else params.x.toFloat()
                                            null             -> params.x.toFloat()
                                        }
                                        // Y никогда не «прилипает» — кнопка свободно движется по вертикали
                                        val rawY = params.y.toFloat()

                                        val nextX = rawX + dragAmount.x
                                        val nextY = rawY + dragAmount.y

                                        hiddenEdge = detectEdge(nextX, nextY, getBounds(visibleSize))

                                        val nextSize   = getButtonSize(hiddenEdge)
                                        val nextBounds = getBounds(nextSize)

                                        params.x = when (hiddenEdge) {
                                            HiddenEdge.Left  -> (nextBounds.minX - nextSize.width  / 2f).roundToInt()
                                            HiddenEdge.Right -> (nextBounds.maxX + nextSize.width  / 2f).roundToInt()
                                            null             -> nextX.coerceIn(nextBounds.minX, nextBounds.maxX).roundToInt()
                                        }
                                        // Y всегда остаётся в безопасной зоне без прилипания
                                        params.y = nextY.coerceIn(nextBounds.minY, nextBounds.maxY).roundToInt()
                                        windowManager.updateViewLayout(composeView, params)
                                    },
                                    onDragEnd = { isDragging = false }
                                )
                            },
                        onClick = {
                            if (!isDragging) { controller.expand(); showOverlay() }
                        }
                    ) {
                        Image(
                            painter = painterResource(R.drawable.service_button),
                            contentDescription = "Overlay",
                            colorFilter = ColorFilter.tint(Color.White),
                            modifier = Modifier.size(iconSize)
                        )
                    }
                }
            }

            if (parent == null) windowManager.addView(this, params)
        }
    }

    private fun showOverlay() {
        if (overlayView != null) return

        overlayView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@OverlayService)
            setViewTreeSavedStateRegistryOwner(this@OverlayService)
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        }

        val overlayParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        )

        val view = overlayView ?: return

        view.setContent {
            KotlinClientTheme {
                val state by controller.state.collectAsState()

                // Лямбда, которая удаляет оверлей из WindowManager
                val dismiss: () -> Unit = {
                    overlayView?.let { v -> if (v.parent != null) windowManager.removeView(v) }
                    overlayView = null
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.55f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        when (state.screen) {
                            OverlayScreen.Menu -> OverlayMenuScreen(
                                onEvents = { controller.openEvents() },
                                onCreate = { controller.openCreate() },
                                onClose  = { controller.collapse(); dismiss() }
                            )
                            OverlayScreen.Events -> OverlayEventsScreen(
                                controller = controller,
                                onBack     = { controller.openMenu() }
                            )
                            OverlayScreen.Create -> OverlayCreateScreen(
                                controller = controller,
                                onBack     = { controller.openMenu() }
                            )
                        }
                    }
                }
            }
        }

        windowManager.addView(view, overlayParams)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Geometry helpers
    // ─────────────────────────────────────────────────────────────────────────

    private fun dpToPx(value: Int) = (value * resources.displayMetrics.density).roundToInt()

    private fun getButtonSize(edge: HiddenEdge?) = when (edge) {
        HiddenEdge.Left, HiddenEdge.Right -> ButtonSize(dpToPx(32), dpToPx(44))
        null                              -> ButtonSize(dpToPx(48), dpToPx(48))
    }

    /**
     * Допустимые координаты для кнопки.
     * maxY учитывает высоту навигационной панели, чтобы скрытая кнопка
     * выглядывала над кнопками/жестовой зоной системы, а не пряталась за ними.
     */
    private fun getBounds(btn: ButtonSize): OverlayBounds {
        val screen = getScreenSize()
        val navBar = getNavBarHeight()
        return OverlayBounds(
            minX = 0f,
            minY = 0f,
            maxX = (screen.x - btn.width).toFloat(),
            maxY = (screen.y - btn.height - navBar).toFloat()
        )
    }

    private fun getScreenSize(): Point {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val b = windowManager.currentWindowMetrics.bounds
            return Point(b.width(), b.height())
        }
        return Point().also { @Suppress("DEPRECATION") windowManager.defaultDisplay.getRealSize(it) }
    }

    /**
     * Высота навигационной панели в пикселях.
     * На API 30+ берём из WindowInsets; на старых — из системного ресурса.
     */
    private fun getNavBarHeight(): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val insets = windowManager.currentWindowMetrics.windowInsets
            return insets.getInsets(android.view.WindowInsets.Type.navigationBars()).bottom
        }
        val id = resources.getIdentifier("navigation_bar_height", "dimen", "android")
        return if (id > 0) resources.getDimensionPixelSize(id) else dpToPx(56)
    }

    /** Только левый и правый края — вертикальное прилипание отключено намеренно. */
    private fun detectEdge(x: Float, y: Float, bounds: OverlayBounds): HiddenEdge? = when {
        x <= bounds.minX -> HiddenEdge.Left
        x >= bounds.maxX -> HiddenEdge.Right
        else             -> null
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Notification
    // ─────────────────────────────────────────────────────────────────────────

    private fun createNotification(): Notification {
        val channelId = "overlay_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, "Оверлей-сервис", NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Overlay запущен")
            .setContentText("Нажмите для перехода в приложение")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .build()
    }
}
