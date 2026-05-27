/*
 * Eu João Victor fiz esse arquivo para representar o modelo de dados (Model) de um item emprestado,
 * estruturando os campos necessários para o armazenamento e controle no banco de dados SQLite.
 */

package com.example.lendit.model

data class ItemEmprestado(
    // Identificador único do empréstimo (chave primária no banco)
    val id: Int = 0,

    // Vincula o empréstimo ao ID do usuário logado que cadastrou o item
    val usuarioId: Int,

    // Nome ou descrição do objeto que foi emprestado
    val nome: String,

    // Nome ou e-mail da pessoa que recebeu o item emprestado
    val paraQuem: String,

    // Data em que o empréstimo foi realizado (armazenada como String formatada)
    val dataEmprestimo: String,

    // Define se o item já foi devolvido (falso por padrão)
    val finalizado: Boolean = false
)