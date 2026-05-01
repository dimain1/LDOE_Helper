package com.example.kotlinclient.local_cache.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.kotlinclient.local_cache.entity.UserEntity


@Dao
interface UserDao {

    @Insert(entity= UserEntity::class, onConflict= OnConflictStrategy.REPLACE) // По логике при каждом логине пользователя будет проверяться есть ли такой пользователь в базе и если нет, то создавать на основе данных полученных с сервера(id) и данных с клиента(login, email)
    fun addNewUser(user: UserEntity)

    @Query("SELECT * FROM users WHERE id = :id") // Получение информации о пользователе по его id, по логике будет вызываться только для id = id текущего активного пользоватея
    fun getUserById(id:Long): UserEntity

    @Delete(entity= UserEntity::class)
    fun deleteUser(user: UserEntity)

    @Update(entity = UserEntity::class)
    fun updateUserInfo(user: UserEntity)
}