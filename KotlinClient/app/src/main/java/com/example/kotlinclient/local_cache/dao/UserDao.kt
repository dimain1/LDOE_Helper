package com.example.kotlinclient.local_cache.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.kotlinclient.local_cache.entity.UserEntity
import kotlinx.coroutines.flow.Flow


@Dao
interface UserDao {

    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Insert(entity= UserEntity::class, onConflict= OnConflictStrategy.REPLACE)
    fun addNewUser(user: UserEntity)

    @Query("SELECT * FROM users WHERE id = :id")
    fun getUserById(id:Long): Flow<UserEntity?>

    @Delete(entity= UserEntity::class)
    fun deleteUser(user: UserEntity)

    @Update(entity = UserEntity::class, onConflict = OnConflictStrategy.REPLACE)
    fun updateUserInfo(user: UserEntity)
}