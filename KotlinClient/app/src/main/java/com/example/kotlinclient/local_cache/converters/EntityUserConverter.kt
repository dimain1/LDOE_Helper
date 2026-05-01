package com.example.kotlinclient.local_cache.converters

import com.example.kotlinclient.local_cache.entity.UserEntity
import com.example.kotlinclient.state_management.entity.User

fun User.toEntity(): UserEntity{
    return UserEntity(
        id = this.id,
        login = this.login,
        email = this.email
    )
}

fun UserEntity.toModel(): User{
    return User(
        id = this.id,
        login = this.login,
        email = this.email
    )

}