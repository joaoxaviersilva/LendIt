/*
 * Eu João Victor fiz esse arquivo como uma Activity secundária convertida para Jetpack Compose,
 * garantindo a compatibilidade com a arquitetura atual do LendIt e eliminando os erros de referências antigas.
 */

package com.example.lendit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.lendit.ui.theme.LendItTheme

/**
 * Activity secundária do aplicativo LendIt.
 * Herdando de ComponentActivity para garantir suporte nativo ao Jetpack Compose,
 * substituindo a antiga estrutura baseada em AppCompatActivity e layouts XML.
 */
class RespostaActivity : ComponentActivity() {

    // Método de ciclo de vida executado na criação da Activity
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Define o bloco de UI injetando o ecossistema do Jetpack Compose
        setContent {
            // Aplica o tema visual unificado do LendIt
            LendItTheme {
                // Surface atua como a camada base de fundo, adaptando-se às cores do tema (Light/Dark)
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Invoca a função de composição da tela passando a ação de encerramento
                    TelaResposta(
                        onVoltar = {
                            // Encerra a execução desta Activity atual e destrói a pilha,
                            // retornando o utilizador para a MainActivity de forma nativa.
                            finish()
                        }
                    )
                }
            }
        }
    }
}

/**
 * Função de Composição (@Composable) responsável por renderizar a interface gráfica.
 * @param onVoltar Lambda executada ao interagir com o botão de retorno.
 */
@Composable
fun TelaResposta(onVoltar: () -> Unit) {
    // Organiza os componentes visuais de forma vertical na tela
    Column(
        modifier = Modifier
            .fillMaxSize()           // Força a coluna a preencher toda a área disponível
            .padding(24.dp),         // Aplica uma margem interna de segurança nas bordas
        verticalArrangement = Arrangement.Center, // Centraliza verticalmente todos os elementos filhos
        horizontalAlignment = Alignment.CenterHorizontally // Alinha os componentes no centro horizontal
    ) {
        // Título principal da interface utilizando a tipografia padrão do Material 3
        Text(
            text = "Tela de Suporte / Resposta",
            style = MaterialTheme.typography.headlineMedium
        )

        // Espaçador rígido vertical para separar o título do texto descritivo
        Spacer(modifier = Modifier.height(8.dp))

        // Texto de feedback para o utilizador/professor identificando a migração do código
        Text(
            text = "Esta tela foi integrada ao Jetpack Compose com sucesso.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant // Cor suavizada para contraste legível
        )

        // Espaçador para isolar o bloco de texto do botão de ação principal
        Spacer(modifier = Modifier.height(24.dp))

        // Botão padrão do Material Design que dispara o callback de encerramento da tela
        Button(onClick = onVoltar) {
            Text("Voltar para o Início")
        }
    }
}