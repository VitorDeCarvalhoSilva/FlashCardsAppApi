package com.example.models

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: Int? = null,
    val username: String,
    val password: String, // Campo para a senha BRUTA (não armazenar)
    val token: String? = null // Campo para o token JWT (opcional)


)