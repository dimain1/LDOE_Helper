package com.example.kotlinclient.local_cache.entity

enum class SyncStatus {
    /** Запись полностью синхронизирована с сервером. */
    SYNCED,

    /** Создана локально, ещё не отправлена на сервер. */
    PENDING_CREATE,

    /** Изменена локально, изменения не отправлены. */
    PENDING_UPDATE,

    /** Помечена на удаление; физически удаляется после подтверждения сервером. */
    PENDING_DELETE
}
