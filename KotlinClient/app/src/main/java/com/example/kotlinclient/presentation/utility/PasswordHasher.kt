package com.example.kotlinclient.presentation.utility

import org.mindrot.jbcrypt.BCrypt

object PasswordHasher{
    fun hashPassword(password: String): String{
        return BCrypt.hashpw(password, BCrypt.gensalt())
    }

    fun checkPassword(password: String, hashed: String): Boolean {
        return try {
            BCrypt.checkpw(password, hashed)
        } catch (e: Exception) {
            false
        }
    }
}