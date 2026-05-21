package com.example.lendit

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.lendit.data.local.DbHelper
import com.example.lendit.model.ItemEmprestado
import com.example.lendit.ui.theme.LendItTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LendItTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainFlow()
                }
            }
        }
    }
}

@Composable
fun MainFlow() {
    val context = LocalContext.current
    val dbHelper = remember { DbHelper(context) }
    var usuarioLogadoId by remember { mutableStateOf<Int?>(null) }

    if (usuarioLogadoId == null) {
        LoginScreen(
            dbHelper = dbHelper,
            onLoginSucesso = { id -> usuarioLogadoId = id }
        )
    } else {
        LendItApp(
            dbHelper = dbHelper,
            usuarioId = usuarioLogadoId!!,
            onLogout = { usuarioLogadoId = null }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(dbHelper: DbHelper, onLoginSucesso: (Int) -> Unit) {
    var email by remember { mutableStateOf("") }
    var nomeUsuario by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var erroMensagem by remember { mutableStateOf("") }
    var modoCadastro by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$".toRegex()

    Scaffold(
        topBar = { TopAppBar(title = { Text(if (modoCadastro) "LendIt - Criar Conta" else "LendIt - Entrar") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(24.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                if (modoCadastro) "Cadastre-se grátis" else "Acesse sua Conta",
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it; erroMensagem = "" },
                label = { Text("E-mail") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (modoCadastro) {
                OutlinedTextField(
                    value = nomeUsuario,
                    onValueChange = { nomeUsuario = it; erroMensagem = "" },
                    label = { Text("Seu Nome") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            OutlinedTextField(
                value = password,
                onValueChange = { password = it; erroMensagem = "" },
                label = { Text("Senha") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            if (erroMensagem.isNotBlank()) {
                Text(
                    text = erroMensagem,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (!modoCadastro) {
                Button(
                    onClick = {
                        if (email.isBlank() || password.isBlank()) {
                            erroMensagem = "Preencha todos os campos."
                            return@Button
                        }
                        val userId = dbHelper.realizarLogin(email, password)
                        if (userId != null) {
                            onLoginSucesso(userId)
                        } else {
                            erroMensagem = "E-mail ou senha incorretos."
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Entrar")
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = { modoCadastro = true; erroMensagem = "" },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Quero Criar uma Conta")
                }
            } else {
                Button(
                    onClick = {
                        val emailLimpo = email.trim().lowercase()
                        val nomeLimpo = nomeUsuario.trim()

                        if (!emailLimpo.matches(emailRegex)) {
                            erroMensagem = "Insira um formato de e-mail válido."
                            return@Button
                        }
                        if (nomeLimpo.isBlank()) {
                            erroMensagem = "Por favor, insira o seu nome."
                            return@Button
                        }
                        if (password.trim().length < 4) {
                            erroMensagem = "A senha deve ter pelo menos 4 caracteres."
                            return@Button
                        }

                        val novoUserId =
                            dbHelper.cadastrarUsuario(emailLimpo, nomeLimpo, password.trim())
                        if (novoUserId > 0) {
                            Toast.makeText(context, "Conta criada com sucesso!", Toast.LENGTH_SHORT)
                                .show()
                            onLoginSucesso(novoUserId.toInt())
                        } else {
                            erroMensagem = "Este e-mail já está cadastrado."
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Finalizar Cadastro")
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(onClick = { modoCadastro = false; erroMensagem = "" }) {
                    Text("Voltar para o Login")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LendItApp(dbHelper: DbHelper, usuarioId: Int, onLogout: () -> Unit) {
    var nomeItem by remember { mutableStateOf("") }
    var paraQuem by remember { mutableStateOf("") }
    var listaItens by remember { mutableStateOf(dbHelper.listarItensDoUsuario(usuarioId)) }

    val appContext = LocalContext.current

    val meuEmail = remember { dbHelper.obterEmailUsuario(usuarioId).lowercase() }
    val meuNome = remember { dbHelper.obterNomeUsuario(usuarioId).lowercase() }

    var dropdownExpandido by remember { mutableStateOf(false) }
    var itemSendoEditado by remember { mutableStateOf<ItemEmprestado?>(null) }

    val todosUsuarios = remember {
        val lista = mutableListOf<String>()
        try {
            val db = dbHelper.readableDatabase
            val cursor = db.rawQuery("SELECT nome_exibicao FROM usuarios", null)
            if (cursor.moveToFirst()) {
                do {
                    lista.add(cursor.getString(0))
                } while (cursor.moveToNext())
            }
            cursor.close()
            db.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        lista
    }

    val usuariosFiltrados = remember(paraQuem) {
        if (paraQuem.isBlank()) todosUsuarios else todosUsuarios.filter {
            it.contains(
                paraQuem,
                ignoreCase = true
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Meus Empréstimos") },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Sair"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            OutlinedTextField(
                value = nomeItem,
                onValueChange = { nomeItem = it },
                label = { Text("O que você emprestou?") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            ExposedDropdownMenuBox(
                expanded = dropdownExpandido,
                onExpandedChange = { dropdownExpandido = it }
            ) {
                OutlinedTextField(
                    value = paraQuem,
                    onValueChange = { paraQuem = it; dropdownExpandido = true },
                    label = { Text("Para quem?") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryEditable, enabled = true),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpandido) }
                )
                if (usuariosFiltrados.isNotEmpty()) {
                    ExposedDropdownMenu(
                        expanded = dropdownExpandido,
                        onDismissRequest = { dropdownExpandido = false }) {
                        usuariosFiltrados.forEach { usuarioSugerido ->
                            DropdownMenuItem(
                                text = { Text(usuarioSugerido) },
                                onClick = { paraQuem = usuarioSugerido; dropdownExpandido = false }
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val nomeLimpo = nomeItem.trim()
                    val destinoLimpo = paraQuem.trim().lowercase()

                    if (nomeLimpo.isNotBlank() && destinoLimpo.isNotBlank()) {
                        if (destinoLimpo == meuEmail || destinoLimpo == meuNome) {
                            Toast.makeText(
                                appContext,
                                "Você não pode emprestar algo para si mesmo!",
                                Toast.LENGTH_LONG
                            ).show()
                            return@Button
                        }

                        val dataAtual =
                            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
                        val novoItem = ItemEmprestado(
                            usuarioId = usuarioId,
                            nome = nomeLimpo,
                            paraQuem = paraQuem.trim(),
                            dataEmprestimo = dataAtual
                        )
                        dbHelper.inserirItem(novoItem)
                        listaItens = dbHelper.listarItensDoUsuario(usuarioId)
                        nomeItem = ""
                        paraQuem = ""
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Salvar Empréstimo")
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            if (listaItens.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Nenhum item pendente.", color = Color.Gray)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(listaItens) { item ->
                        ItemCard(
                            item = item,
                            onEditClick = { itemSendoEditado = item },
                            onDeletar = {
                                dbHelper.deletarItem(item.id, usuarioId)
                                listaItens = dbHelper.listarItensDoUsuario(usuarioId)
                            }
                        )
                    }
                }
            }
        }
    }

    if (itemSendoEditado != null) {
        var nomeEditado by remember { mutableStateOf(itemSendoEditado!!.nome) }
        var paraQuemEditado by remember { mutableStateOf(itemSendoEditado!!.paraQuem) }
        var finalizadoEditado by remember { mutableStateOf(itemSendoEditado!!.finalizado) }

        AlertDialog(
            onDismissRequest = { itemSendoEditado = null },
            title = { Text("Editar Empréstimo") },
            text = {
                Column {
                    OutlinedTextField(
                        value = nomeEditado,
                        onValueChange = { nomeEditado = it },
                        label = { Text("Item") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = paraQuemEditado,
                        onValueChange = { paraQuemEditado = it },
                        label = { Text("Para quem?") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = finalizadoEditado,
                            onCheckedChange = { finalizadoEditado = it })
                        Text("Marcar como Devolvido (Finalizado)")
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val destinoEditadoLimpo = paraQuemEditado.trim().lowercase()
                        if (destinoEditadoLimpo == meuEmail || destinoEditadoLimpo == meuNome) {
                            Toast.makeText(
                                appContext,
                                "Você não pode emprestar para si mesmo!",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@Button
                        }

                        if (nomeEditado.isNotBlank() && paraQuemEditado.trim().isNotBlank()) {
                            val itemAtualizado = itemSendoEditado!!.copy(
                                nome = nomeEditado.trim(),
                                paraQuem = paraQuemEditado.trim(),
                                finalizado = finalizadoEditado
                            )
                            dbHelper.atualizarItem(itemAtualizado)
                            listaItens = dbHelper.listarItensDoUsuario(usuarioId)
                            itemSendoEditado = null
                        }
                    }
                ) { Text("Salvar") }
            },
            dismissButton = {
                TextButton(onClick = { itemSendoEditado = null }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
fun ItemCard(item: ItemEmprestado, onEditClick: () -> Unit, onDeletar: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (item.finalizado) MaterialTheme.colorScheme.surfaceVariant.copy(
                alpha = 0.5f
            )
            else MaterialTheme.colorScheme.surfaceContainerHighest
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.nome,
                    style = MaterialTheme.typography.bodyLarge,
                    textDecoration = if (item.finalizado) TextDecoration.LineThrough else TextDecoration.None
                )
                Text(text = "Para: ${item.paraQuem}", style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = "Data: ${item.dataEmprestimo}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            Row {
                IconButton(onClick = onEditClick) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onDeletar) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Deletar",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}