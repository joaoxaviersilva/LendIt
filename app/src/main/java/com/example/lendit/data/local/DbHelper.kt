package com.example.lendit.data.local

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.example.lendit.model.ItemEmprestado

class DbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val TAG = "DbHelper"
        private const val DATABASE_NAME = "lendit.db"
        private const val DATABASE_VERSION = 2 // Incrementado para a nova versão com Login

        // Tabela de Usuários
        private const val TABLE_USERS = "usuarios"
        private const val COLUMN_USER_ID = "id"
        private const val COLUMN_USER_NAME = "username"
        private const val COLUMN_USER_PASSWORD = "password"

        // Tabela de Itens Emprestados
        private const val TABLE_ITEMS = "itens_emprestados"
        private const val COLUMN_ITEM_ID = "id"
        private const val COLUMN_ITEM_USER_REF = "usuario_id" // Chave estrangeira
        private const val COLUMN_ITEM_NOME = "nome"
        private const val COLUMN_ITEM_PARA_QUEM = "para_quem"
        private const val COLUMN_ITEM_DATA = "data_emprestimo"
        private const val COLUMN_ITEM_FINALIZADO = "finalizado"
    }

    override fun onCreate(db: SQLiteDatabase) {
        try {
            // Criar tabela de usuários
            val createTableUsers = """
                CREATE TABLE $TABLE_USERS (
                    $COLUMN_USER_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    $COLUMN_USER_NAME TEXT NOT NULL UNIQUE,
                    $COLUMN_USER_PASSWORD TEXT NOT NULL
                )
            """.trimIndent()
            db.execSQL(createTableUsers)

            // Criar tabela de itens
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

            Log.d(TAG, "Tabelas criadas com sucesso.")
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao criar tabelas: ${e.message}")
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        try {
            db.execSQL("DROP TABLE IF EXISTS $TABLE_ITEMS")
            db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
            onCreate(db)
            Log.d(TAG, "Banco atualizado com sucesso.")
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao atualizar banco: ${e.message}")
        }
    }

    // --- OPERAÇÕES DE AUTENTICAÇÃO ---

    // Cadastra um novo usuário. Retorna o ID ou -1 se der erro (ex: usuário já existe)
    fun cadastrarUsuario(username: String, email: String): Long {
        if (username.isBlank() || email.isBlank()) return -1
        return try {
            val db = this.writableDatabase
            val values = ContentValues().apply {
                put(COLUMN_USER_NAME, username.trim().lowercase())
                put(COLUMN_USER_PASSWORD, email) // Usando o campo de senha
            }
            val id = db.insert(TABLE_USERS, null, values)
            db.close()
            id
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao cadastrar usuário: ${e.message}")
            -1
        }
    }

    // Realiza o login. Retorna o ID do usuário se estiver correto, ou null se falhar
    fun realizarLogin(username: String, email: String): Int? {
        return try {
            val db = this.readableDatabase
            val query = "SELECT $COLUMN_USER_ID FROM $TABLE_USERS WHERE $COLUMN_USER_NAME = ? AND $COLUMN_USER_PASSWORD = ?"
            val cursor = db.rawQuery(query, arrayOf(username.trim().lowercase(), email))

            var userId: Int? = null
            if (cursor.moveToFirst()) {
                userId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_USER_ID))
            }
            cursor.close()
            db.close()
            userId
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao realizar login: ${e.message}")
            null
        }
    }

    // --- OPERAÇÕES DOS ITENS (Filtrados por Usuário) ---

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

    fun listarItensDoUsuario(usuarioId: Int): List<ItemEmprestado> {
        val lista = mutableListOf<ItemEmprestado>()
        try {
            val db = this.readableDatabase
            val query = "SELECT * FROM $TABLE_ITEMS WHERE $COLUMN_ITEM_USER_REF = ? ORDER BY $COLUMN_ITEM_ID DESC"
            val cursor = db.rawQuery(query, arrayOf(usuarioId.toString()))

            if (cursor.moveToFirst()) {
                do {
                    val item = ItemEmprestado(
                        id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ITEM_ID)),
                        usuarioId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ITEM_USER_REF)),
                        nome = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ITEM_NOME)),
                        paraQuem = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ITEM_PARA_QUEM)),
                        dataEmprestimo = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ITEM_DATA)),
                        finalizado = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ITEM_FINALIZADO)) == 1
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

    fun atualizarItem(item: ItemEmprestado): Int {
        return try {
            val db = this.writableDatabase
            val values = ContentValues().apply {
                put(COLUMN_ITEM_NOME, item.nome)
                put(COLUMN_ITEM_PARA_QUEM, item.paraQuem)
                put(COLUMN_ITEM_DATA, item.dataEmprestimo)
                put(COLUMN_ITEM_FINALIZADO, if (item.finalizado) 1 else 0)
            }
            val linhasAfetadas = db.update(TABLE_ITEMS, values, "$COLUMN_ITEM_ID = ? AND $COLUMN_ITEM_USER_REF = ?", arrayOf(item.id.toString(), item.usuarioId.toString()))
            db.close()
            linhasAfetadas
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao atualizar item: ${e.message}")
            0
        }
    }

    fun deletarItem(itemId: Int, usuarioId: Int): Int {
        return try {
            val db = this.writableDatabase
            val linhasDeletadas = db.delete(TABLE_ITEMS, "$COLUMN_ITEM_ID = ? AND $COLUMN_ITEM_USER_REF = ?", arrayOf(itemId.toString(), usuarioId.toString()))
            db.close()
            linhasDeletadas
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao deletar item: ${e.message}")
            0
        }
    }
}