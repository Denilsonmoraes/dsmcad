package com.paginacaopro.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Architecture
import com.paginacaopro.app.ui.theme.PrimaryBlue
import com.paginacaopro.app.ui.theme.Slate800
import com.paginacaopro.app.ui.theme.Slate900

@Composable
fun HomeScreen(
    onNavigateToFreePlanStudio: () -> Unit = {},
    onNavigateToNewTiling: () -> Unit,
    onNavigateToFloorPlan: () -> Unit,
    onNavigateToPhotoTiling: () -> Unit,
    onNavigateToCatalog: () -> Unit,
    onNavigateToMyTilings: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToTechnicalHelp: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Cabeçalho da Aplicação
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Slate900),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Engineering,
                        contentDescription = "Ícone Engenharia",
                        tint = PrimaryBlue,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "PAGINAÇÃO PRO",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 1.2.sp
                    )
                    Text(
                        text = "Gerador Inteligente de Paginação de Alvenaria",
                        fontSize = 13.sp,
                        color = Color(0xFF94A3B8),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Desenho Técnico Vetorial SVG • Quantitativos • Campo & Projeto",
                        fontSize = 11.sp,
                        color = Color(0xFF38BDF8),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // NOVO: Estúdio de Planta Livre & Paginação Automática (Modo 1 Desenho Visual + Modo 2 Execução)
            HomeMenuButton(
                title = "DESENHAR PLANTA LIVRE (MODO VISUAL & EXECUÇÃO)",
                subtitle = "Desenhe paredes e linhas 90° com o dedo, insira portas/janelas, cotas reais, blocos (9, 11.5, 14, 19 cm) e gere SVG/PDF executivo",
                icon = Icons.Default.Architecture,
                accentColor = Color(0xFF10B981),
                onClick = onNavigateToFreePlanStudio
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Botão Principal: Planta Baixa da Obra Multi-Paredes
            HomeMenuButton(
                title = "PLANTA BAIXA DA OBRA (PAREDES A, B, C...)",
                subtitle = "Planta sem móveis, nomeação A, B, C, vãos, SVGs individuais e quantitativo geral",
                icon = Icons.Default.GridView,
                accentColor = Color(0xFF0284C7),
                onClick = onNavigateToFloorPlan
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 6 Botões Tradicionais da Tela Inicial
            HomeMenuButton(
                title = "NOVA PAGINAÇÃO INDIVIDUAL",
                subtitle = "Inserir dimensões de parede única, vãos e gerar desenho",
                icon = Icons.Default.AddCircle,
                accentColor = PrimaryBlue,
                onClick = onNavigateToNewTiling
            )


            Spacer(modifier = Modifier.height(14.dp))

            HomeMenuButton(
                title = "PAGINAÇÃO POR FOTOGRAFIA",
                subtitle = "Importar imagem de parede/vão com calibração métrica",
                icon = Icons.Default.CameraAlt,
                accentColor = Color(0xFF0D9488),
                onClick = onNavigateToPhotoTiling
            )

            Spacer(modifier = Modifier.height(14.dp))

            HomeMenuButton(
                title = "CATÁLOGO DE BLOCOS",
                subtitle = "Blocos cerâmicos, concreto, tijolos baianos e peças",
                icon = Icons.Default.GridView,
                accentColor = Color(0xFFEA580C),
                onClick = onNavigateToCatalog
            )

            Spacer(modifier = Modifier.height(14.dp))

            HomeMenuButton(
                title = "MINHAS PAGINAÇÕES",
                subtitle = "Histórico de paredes calculadas e projetos salvos",
                icon = Icons.Default.Folder,
                accentColor = Color(0xFF6366F1),
                onClick = onNavigateToMyTilings
            )

            Spacer(modifier = Modifier.height(14.dp))

            HomeMenuButton(
                title = "CONFIGURAÇÕES",
                subtitle = "Padrões de juntas, margem de perda e parâmetros",
                icon = Icons.Default.Settings,
                accentColor = Color(0xFF64748B),
                onClick = onNavigateToSettings
            )

            Spacer(modifier = Modifier.height(14.dp))

            HomeMenuButton(
                title = "AJUDA TÉCNICA",
                subtitle = "Normas ABNT (NBR 6136, 15270, 16868), amarração e vergas",
                icon = Icons.Default.HelpOutline,
                accentColor = Color(0xFF8B5CF6),
                onClick = onNavigateToTechnicalHelp
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun HomeMenuButton(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = accentColor.copy(alpha = 0.15f),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.size(46.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier
                        .padding(10.dp)
                        .size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
