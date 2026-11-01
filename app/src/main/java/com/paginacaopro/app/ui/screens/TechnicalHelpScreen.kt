package com.paginacaopro.app.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paginacaopro.app.ui.theme.PrimaryBlue
import com.paginacaopro.app.ui.theme.Slate900

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TechnicalHelpScreen(
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ajuda Técnica & Normas ABNT", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Slate900,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Seção: Normas ABNT Vigentes
            HelpCard(
                title = "1. Normas Técnicas ABNT de Referência",
                content = """
                    • ABNT NBR 6136: Blocos vazados de concreto simples para alvenaria — Requisitos e métodos de ensaio.
                    • ABNT NBR 15270: Componentes cerâmicos — Blocos e tijolos para alvenaria (vedação e estrutural).
                    • ABNT NBR 16868: Alvenaria estrutural — Partes 1 (Projeto), 2 (Execução e controle) e 3 (Métodos de ensaio).
                    • ABNT NBR 8545: Execução de alvenaria sem função estrutural de componentes cerâmicos.
                """.trimIndent()
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Seção: Juntas de Argamassa
            HelpCard(
                title = "2. Espessura das Juntas de Argamassa",
                content = """
                    As juntas horizontais e verticais desempenham papel fundamental na acomodação de tensões e na estanqueidade.
                    • O valor inicial padrão adotado no aplicativo é de 1,0 cm (10 mm), compatível com a modulação comercial típica de 40x20 cm.
                    • As normas ABNT recomendam espessuras nominais usualmente entre 1,0 cm e 1,5 cm (10 a 15 mm) para blocos convencionais.
                    • Juntas inferiores a 0,5 cm ou superiores a 2,0 cm podem comprometer a aderência e gerar consumo excessivo ou fissuras.
                """.trimIndent()
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Seção: Regras de Amarração
            HelpCard(
                title = "3. Modulação e Amarração dos Blocos",
                content = """
                    • Amarração Alternada (1/2): As juntas verticais devem ficar desencontradas em no mínimo 1/3 a 1/2 do comprimento do bloco (trespasse mínimo).
                    • Meios Blocos: Devem ser utilizados prioritariamente nas extremidades e cantos para evitar quebras e desperdício de material.
                    • Junta a Prumo: Só deve ser utilizada quando expressamente prevista em projeto e acompanhada de armaduras horizontais de amarração nas juntas.
                """.trimIndent()
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Seção: Vergas e Contravergas
            HelpCard(
                title = "4. Vergas e Contravergas em Vãos",
                content = """
                    • Portas e janelas geram concentração de tensões nos cantos dos vãos.
                    • Vergas (acima de portas/janelas) e contravergas (abaixo de peitoris) devem transpassar pelo menos 20 cm a 30 cm além das laterais do vão para distribuição de cargas.
                    • Podem ser executadas com blocos canaleta grauteados e armados ou vigotas pré-moldadas.
                """.trimIndent()
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Aviso de Engenharia
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("⚠️ RESPONSABILIDADE TÉCNICA", fontWeight = FontWeight.Bold, color = Slate900, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "O Paginação Pro é uma ferramenta de apoio geométrico, modulação e levantamento quantitativo para canteiros de obras e projetos. " +
                                "A geração deste desenho não substitui o projeto executivo e o dimensionamento estrutural emitido por engenheiro civil habilitado com respectiva ART.",
                        fontSize = 12.sp,
                        color = Color(0xFF475569)
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
private fun HelpCard(title: String, content: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = PrimaryBlue)
            Spacer(modifier = Modifier.height(8.dp))
            Text(content, fontSize = 12.sp, color = Slate900, lineHeight = 18.sp)
        }
    }
}
