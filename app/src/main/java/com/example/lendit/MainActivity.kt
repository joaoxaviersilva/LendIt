package com.example.lendit

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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

    // Controla qual usuário está logado usando seu ID de banco de dados
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
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var erroMensagem by remember { mutableStateOf("") }
    val context = LocalContext.current

    Scaffold(
        topBar = { TopAppBar(title = { Text("LendIt - Acessar Conta") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(24.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Bem-vindo de volta!", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = username,
                onValueChange = { username = it; erroMensagem = "" },
                label = { Text("Nome de usuário") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))

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

            Button(
                onClick = {
                    val userId = dbHelper.realizarLogin(username, password)
                    if (userId != null) {
                        onLoginSucesso(userId)
                    } else {
                        erroMensagem = "Usuário ou senha incorretos."
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Entrar")
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = {
                    val resultado = dbHelper.cadastrarUsuario(username, password)
                    if (resultado > 0) {
                        Toast.makeText(
                            context,
                            "Usuário registrado! Clique em Entrar.",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        erroMensagem = "Nome de usuário indisponível ou inválido."
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Criar Nova Conta")
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Meus Empréstimos") },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(imageVector = Icons.Default.ExitToApp, contentDescription = "Sair")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
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
            OutlinedTextField(
                value = paraQuem,
                onValueChange = { paraQuem = it },
                label = { Text("Para quem?") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (nomeItem.isNotBlank() && paraQuem.isNotBlank()) {
                        val dataAtual =
                            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
                        val novoItem = ItemEmprestado(
                            usuarioId = usuarioId,
                            nome = nomeItem.trim(),
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
                    Text("Nenhum item pendente para este usuário.", color = Color.Gray)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(listaItens) { item ->
                        ItemCard(
                            item = item,
                            onToggleFinalizado = {
                                val atualizado = item.copy(finalizado = !item.finalizado)
                                dbHelper.atualizarItem(atualizado)
                                listaItens = dbHelper.listarItensDoUsuario(usuarioId)
                            },
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
}

@Composable
fun ItemCard(item: ItemEmprestado, onToggleFinalizado: () -> Unit, onDeletar: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggleFinalizado() },
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
                IconButton(onClick = onToggleFinalizado) {
                    Icon(
                        imageVector = if (item.finalizado) Icons.Default.CheckCircle else Icons.Default.Refresh,
                        contentDescription = "Status",
                        tint = if (item.finalizado) MaterialTheme.colorScheme.primary else Color.Gray
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