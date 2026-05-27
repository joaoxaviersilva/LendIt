/*
 * Eu João Victor fiz esse arquivo para gerenciar a persistência local de dados utilizando SQLiteOpenHelper,
 * controlando tabelas de usuários, criptografia de senhas em SHA-256 e o CRUD de itens emprestados do LendIt.
 */

package com.example.lendit.data.local

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.example.lendit.model.ItemEmprestado
import java.security.MessageDigest

class DbHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val TAG = "DbHelper"
        private const val DATABASE_NAME = "lendit.db"
        private const val DATABASE_VERSION = 4

        // Definição da estrutura da tabela de Usuários
        private const val TABLE_USERS = "usuarios"
        private const val COLUMN_USER_ID = "id"
        private const val COLUMN_USER_NAME = "username" // Armazena o e-mail/login
        private const val COLUMN_USER_DISPLAY_NAME = "nome_exibicao"
        private const val COLUMN_USER_PASSWORD = "password"

        // Definição da estrutura da tabela de Itens Emprestados
        private const val TABLE_ITEMS = "itens_emprestados"
        private const val COLUMN_ITEM_ID = "id"
        private const val COLUMN_ITEM_USER_REF = "usuario_id" // Chave estrangeira
        private const val COLUMN_ITEM_NOME = "nome"
        private const val COLUMN_ITEM_PARA_QUEM = "para_quem"
        private const val COLUMN_ITEM_DATA = "data_emprestimo"
        private const val COLUMN_ITEM_FINALIZADO = "finalizado"
    }

    /**
     * Gera uma hash segura SHA-256 a partir de uma String de texto puro.
     * Utilizada para evitar o armazenamento de senhas expostas no banco de dados local.
     */
    private fun gerarHashSHA256(senha: String): String {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            val hashBytes = digest.digest(senha.toByteArray(Charsets.UTF_8))
            hashBytes.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao gerar hash da senha: ${e.message}")
            senha
        }
    }

    /**
     * Cria as tabelas do banco de dados na primeira inicialização do aplicativo.
     */
    override fun onCreate(db: SQLiteDatabase) {
        try {
            // Script de criação da tabela de usuários com constraint UNIQUE no e-mail
            val createTableUsers = """
                CREATE TABLE $TABLE_USERS (
                    $COLUMN_USER_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    $COLUMN_USER_NAME TEXT NOT NULL UNIQUE,
                    $COLUMN_USER_DISPLAY_NAME TEXT NOT NULL,
                    $COLUMN_USER_PASSWORD TEXT NOT NULL
                )
            """.trimIndent()
            db.execSQL(createTableUsers)

            // Script de criação da tabela de itens com relacionamento e exclusão em cascata
            val createTableItems = """
                CREATE TABLE $TABLE_ITEMS (
                    $COLUMN_ITEM_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    $COLUMN_ITEM_USER_REF INTEGER NOT NULL,
                    $COLUMN_ITEM_NOME TEXT NOT NULL,
                    $COLUMN_ITEM_PARA_QUEM TEXT NOT NULL,
                    $COLUMN_ITEM_DATA TEXT NOT NULL,
                    $COLUMN_ITEM_FINALIZADO INTEGER NOT NULL DEFAULT 0,
                    FOREIGN KEY($COLUMN_ITEM_USER_REF) REFERENCES $TABLE_USERS($COLUMN_USER_ID) ON DELETE CASCADE
                )
            """.trimIndent()
            db.execSQL(createTableItems)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao criar tabelas: ${e.message}")
        }
    }

    /**
     * Executado quando há uma mudança na versão do banco de dados (DATABASE_VERSION),
     * recriando a estrutura das tabelas.
     */
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        try {
            db.execSQL("DROP TABLE IF EXISTS $TABLE_ITEMS")
            db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
            onCreate(db)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao atualizar banco: ${e.message}")
        }
    }

    /**
     * Insere um novo usuário no banco com e-mail sanitizado e senha criptografada.
     */
    fun cadastrarUsuario(email: String, nome: String, senhaLimpa: String): Long {
        if (email.isBlank() || nome.isBlank() || senhaLimpa.isBlank()) return -1
        return try {
            val db = this.writableDatabase
            val values = ContentValues().apply {
                put(COLUMN_USER_NAME, email.trim().lowercase())
                put(COLUMN_USER_DISPLAY_NAME, nome.trim())
                put(COLUMN_USER_PASSWORD, gerarHashSHA256(senhaLimpa.trim()))
            }
            val id = db.insert(TABLE_USERS, null, values)
            db.close()
            id
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao cadastrar: ${e.message}")
            -1
        }
    }

    /**
     * Valida as credenciais fornecidas comparando o hash gerado com o armazenado no banco.
     * Retorna o ID do usuário se autenticado com sucesso.
     */
    fun realizarLogin(email: String, senhaLimpa: String): Int? {
        return try {
            val db = this.readableDatabase
            val query =
                "SELECT $COLUMN_USER_ID FROM $TABLE_USERS WHERE $COLUMN_USER_NAME = ? AND $COLUMN_USER_PASSWORD = ?"
            val cursor = db.rawQuery(
                query,
                arrayOf(email.trim().lowercase(), gerarHashSHA256(senhaLimpa.trim()))
            )

            var userId: Int? = null
            if (cursor.moveToFirst()) {
                userId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_USER_ID))
            }
            cursor.close()
            db.close()
            userId
        } catch (e: Exception) {
            Log.e(TAG, "Erro no login: ${e.message}")
            null
        }
    }

    /**
     * Recupera o e-mail de um usuário específico com base no seu ID.
     */
    fun obterEmailUsuario(usuarioId: Int): String {
        var email = ""
        try {
            val db = this.readableDatabase
            val cursor = db.rawQuery(
                "SELECT $COLUMN_USER_NAME FROM $TABLE_USERS WHERE $COLUMN_USER_ID = ?",
                arrayOf(usuarioId.toString())
            )
            if (cursor.moveToFirst()) {
                email = cursor.getString(0)
            }
            cursor.close()
            db.close()
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao obter email: ${e.message}")
        }
        return email
    }

    /**
     * Recupera o nome de exibição de um usuário específico com base no seu ID.
     */
    fun obterNomeUsuario(usuarioId: Int): String {
        var nome = ""
        try {
            val db = this.readableDatabase
            val cursor = db.rawQuery(
                "SELECT $COLUMN_USER_DISPLAY_NAME FROM $TABLE_USERS WHERE $COLUMN_USER_ID = ?",
                arrayOf(usuarioId.toString())
            )
            if (cursor.moveToFirst()) {
                nome = cursor.getString(0)
            }
            cursor.close()
            db.close()
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao obter nome: ${e.message}")
        }
        return nome
    }

    /**
     * Salva um novo registro de empréstimo vinculado a um usuário específico.
     */
    fun inserirItem(item: ItemEmprestado): Long {
        return try {
            val db = this.writableDatabase
            val values = ContentValues().apply {
                put(COLUMN_ITEM_USER_REF, item.usuarioId)
                put(COLUMN_ITEM_NOME, item.nome)
                put(COLUMN_ITEM_PARA_QUEM, item.paraQuem)
                put(COLUMN_ITEM_DATA, item.dataEmprestimo)
                put(COLUMN_ITEM_FINALIZADO, if (item.finalizado) 1 else 0)
            }
            val id = db.insert(TABLE_ITEMS, null, values)
            db.close()
            id
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao inserir item: ${e.message}")
            -1
        }
    }

    /**
     * Lista todos os empréstimos ativos e finalizados pertencentes ao usuário logado,
     * ordenando de forma decrescente pelo ID (mais recentes primeiro).
     */
    fun listarItensDoUsuario(usuarioId: Int): List<ItemEmprestado> {
        val lista = mutableListOf<ItemEmprestado>()
        try {
            val db = this.readableDatabase
            val query =
                "SELECT * FROM $TABLE_ITEMS WHERE $COLUMN_ITEM_USER_REF = ? ORDER BY $COLUMN_ITEM_ID DESC"
            val cursor = db.rawQuery(query, arrayOf(usuarioId.toString()))

            if (cursor.moveToFirst()) {
                do {
                    val item = ItemEmprestado(
                        id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ITEM_ID)),
                        usuarioId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ITEM_USER_REF)),
                        nome = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ITEM_NOME)),
                        paraQuem = cursor.getString(
                            cursor.getColumnIndexOrThrow(
                                COLUMN_ITEM_PARA_QUEM
                            )
                        ),
                        dataEmprestimo = cursor.getString(
                            cursor.getColumnIndexOrThrow(
                                COLUMN_ITEM_DATA
                            )
                        ),
                        finalizado = cursor.getInt(
                            cursor.getColumnIndexOrThrow(
                                COLUMN_ITEM_FINALIZADO
                            )
                        ) == 1
                    )
                    lista.add(item)
                } while (cursor.moveToNext())
            }
            cursor.close()
            db.close()
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao listar itens: ${e.message}")
        }
        return lista
    }

    /**
     * Atualiza os dados de um item existente, garantindo a validação por ID do item e do usuário.
     */
    fun atualizarItem(item: ItemEmprestado): Int {
        return try {
            val db = this.writableDatabase
            val values = ContentValues().apply {
                put(COLUMN_ITEM_NOME, item.nome)
                put(COLUMN_ITEM_PARA_QUEM, item.paraQuem)
                put(COLUMN_ITEM_DATA, item.dataEmprestimo)
                put(COLUMN_ITEM_FINALIZADO, if (item.finalizado) 1 else 0)
            }
            val linhas = db.update(
                TABLE_ITEMS,
                values,
                "$COLUMN_ITEM_ID = ? AND $COLUMN_ITEM_USER_REF = ?",
                arrayOf(item.id.toString(), item.usuarioId.toString())
            )
            db.close()
            linhas
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao atualizar item: ${e.message}")
            0
        }
    }

    /**
     * Remove permanentemente um item do banco de dados, protegendo a operação pelo ID do dono do item.
     */
    fun deletarItem(itemId: Int, usuarioId: Int): Int {
        return try {
            val db = this.writableDatabase
            val linhas = db.delete(
                TABLE_ITEMS,
                "$COLUMN_ITEM_ID = ? AND $COLUMN_ITEM_USER_REF = ?",
                arrayOf(itemId.toString(), usuarioId.toString())
            )
            db.close()
            linhas
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao deletar item: ${e.message}")
            0
        }
    }
}