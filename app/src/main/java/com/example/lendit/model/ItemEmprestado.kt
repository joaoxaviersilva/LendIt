package com.example.lendit.model

data class ItemEmprestado(
    val id: Int = 0,
    val usuarioId: Int, // Vincula o empréstimo ao ID do usuário logado
    val nome: String,
    val paraQuem: String,
    val dataEmprestimo: String,
    val finalizado: Boolean = false
)