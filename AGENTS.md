# Fluxo Padrão de Testes e Desenvolvimento (Regra Obrigatória do Projeto)

Em **TODOS os projetos**, siga obrigatoriamente este fluxo de testes em 2 etapas na lateral direita do Antigravity:

1. **Etapa 1 — Simulador Interativo Instantâneo na Lateral Direita (Opção 3 - Primeiro):**
   - Sempre criar e atualizar o Simulador Interativo (`simulador_paginacao_pro.html`) no painel lateral direito do Antigravity para que o usuário teste as funções em tempo real com o mouse sem aguardar o Gradle.
2. **Etapa 2 — Aparelho Físico via ADB (Opção 1 - Depois):**
   - Após aprovar no simulador lateral, rodar `testDebugUnitTest assembleDebug` e instalar via ADB no celular conectado (USB ou Wi-Fi).
