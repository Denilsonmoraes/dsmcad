# 📐 DSMCAD Pro v2.0 — Motor CAD & BIM de Alvenaria Estrutural

> **Acesse Online via GitHub Pages:**  
> 🌐 **[https://denilsonmoraes.github.io/dsmcad/](https://denilsonmoraes.github.io/dsmcad/)**

O **DSMCAD** une o poder clássico das ferramentas de precisão do **AutoCAD** (TRIM, EXTEND, FILLET, SPLIT, OFFSET, Grips, Terminal CAD) com a inteligência construtiva de **alvenaria estrutural e paginação modular BIM**.

---

## 🚀 Principais Funcionalidades

### 1. Ferramentas Clássicas de Geometria CAD
* **✂️ TRIM / Aparar (`TR` ou `TRIM`):** Corta com 1 clique o excesso de qualquer parede que cruza ou ultrapassa outra parede.
* **📐 EXTEND / Estender (`EX` ou `EXTEND`):** Estica a parede automaticamente até a próxima parede limite.
* **🔗 FILLET / Concordância 90° (`F` ou `FILLET`):** Selecione 2 paredes para fechar o Canto 90° com amarração automática de blocos.
* **⚡ SPLIT / Dividir (`BR` ou `SPLIT`):** Quebra uma parede em duas no ponto exato do clique.
* **↔️ OFFSET / Deslocamento Paralelo (`O` ou `OFFSET`):** Cria parede paralela na distância desejada (ex: `0.15m`, corredor `1.20m`, cômodo `3.00m`).
* **📋 COPY (`CO`), 🪞 MIRROR (`MI`), ❌ ERASE (`E`, `DEL`):** Comandos rápidos de manipulação.
* **📏 DIST / Medir Dinâmico (`DI`):** Trena dinâmica mostrando distância, $\Delta X$, $\Delta Y$ e ângulo em tempo real.

### 2. Grips AutoCAD & Esticamento Interativo (Stretch)
* Clique em qualquer parede para exibir os **Grips**:
  * 🟩 **Quadrados Ciano nas Pontas:** Clique e arraste para esticar o comprimento com cota dinâmica em tempo real.
  * 🟦 **Quadrado Azul no Centro:** Arraste para mover a parede inteira.

### 3. Seleção por Janela AutoCAD (Window vs. Crossing)
* Arrastar da esquerda para a direita (Azul): seleciona apenas o que está 100% contido.
* Arrastar da direita para a esquerda (Verde): seleciona qualquer elemento cruzado ou tocado.

### 4. Object Snap (OSNAP F3) & Modo Orto (F8)
* 🟩 **Ponto Final (Endpoint)**
* 🔺 **Ponto Médio (Midpoint)**
* ❌ **Interseção (Intersection)**
* `[F8]` ativa trava ortogonal 90° e `[F3]` ativa o snap magnético.

### 5. Barra de Comandos DSMCAD (Terminal CAD)
* Digite comandos clássicos (`TR`, `EX`, `F`, `BR`, `O`, `E`, `CO`, `MI`, `DI`, `W`, `REC`, `P`, `Z`, `U`).
* **Entrada Direta de Distância:** Digite diretamente um número (ex: `4.5` ou `450`) e aperte `Enter` para ajustar a parede selecionada imediatamente.

### 6. Alvenaria Inteligente & BIM (Superior ao AutoCAD)
* **Tijolo Baiano 6 Furos (9×14×19 cm):** Espessura 9 cm, altura modular de fiada 15 cm ($14\text{ cm} + 1\text{ cm}$ de massa), meio-tijolo 9×14×9 cm.
* **Vigas / Cintas de Concreto em Qualquer Fiada:** Inserção de canaletas U armadas com aço corrido no respaldo ou em qualquer altura.
* **Pilares de Concreto Armado (20×alt×9 cm):** Integrados nas paredes com amarração nos cantos.
* **Visualização 3D 360° e Elevação SVG Completa:** Com quantitativo de materiais instantâneo.

---

## 🛠️ Tecnologias Utilizadas
* HTML5 Canvas & SVG
* Three.js (Renderização 3D WebGL)
* Tailwind CSS
* Vanilla JavaScript Modular (Zero dependências pesadas, 100% offline e rápido)
