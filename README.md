# ⚡ TrendVibes - Android Native App (Kotlin + Jetpack Compose)

Aplicativo Android nativo completo para o **TrendVibes | Augusta Streetwear System**, construído em **Kotlin** com **Jetpack Compose**, seguindo a estética visual **Neo-Brutalist** do site original (bordas sólidas pretas, sombras duras projetadas, tipografia marcante e paleta de cores Vermelho Crimson `#CC0000`, Amarelo `#FACC15` e Preto `#0A0A0A`).

---

## 📱 Módulos do Aplicativo

### 1. Experiência do Cliente (Loja / Drop)
- **Hero Banner Interativo:** Destaque para o catálogo `FALL / WINTER DROP - 2026: SANGUE-FRIO`.
- **Filtro por Categorias:** Navegação rápida entre Moletons, Jaquetas, Camisetas, Calças e Acessórios.
- **Barra de Busca Dinâmica:** Pesquisa instantânea por nome ou descrição da peça.
- **Vitrine em Grade Neo-Brutalist:** Cards de produtos com imagem via Coil, tag de *Novo Drop*, categoria e preço.
- **Sacola de Compras (Bottom Sheet):** Controle de quantidades (+/-), remoção e cálculo de total em tempo real.
- **Checkout com Pix:** Modal com QR Code e botão de cópia de chave Pix ("Copia e Cola") estilizado.
- **Painel de Acessibilidade:**
  - Ajuste de tamanho de fonte (A-, Normal, A+);
  - Modo Alto Contraste;
  - Fonte adaptada para dislexia;
  - Leitor de tela / Narrador (TTS).

### 2. Portal Operacional do Staff
- **Login Neo-Brutalist:**
  - Autenticação por e-mail e senha;
  - Seletor de cargo (**Vendedor**, **Estoquista**, **Gerente**, **RH**);
  - Seletor de IP / URL do servidor backend C# (com fallback offline inteligente para testes imediatos).
- **Painel do Vendedor (`VendedorScreen.kt`):**
  - Controle de turno em tempo real (**ATIVO** / **PAUSA**);
  - Lançamento de vendas por SKU e valor;
  - **Cálculo automático de comissão de 5%** (regra de negócio da API C#);
  - Histórico de vendas lançadas na sessão.
- **Painel do Estoquista (`EstoquistaScreen.kt`):**
  - Listagem completa da grade de estoque por SKU e tamanho;
  - Botões rápidos de incremento e decremento (+ / -);
  - Alerta visual em vermelho para **Estoque Baixo** (≤ 5 unidades);
  - Cadastro rápido de novo SKU no estoque.
- **Painel do Gerente (`GerenteScreen.kt`):**
  - Indicadores executivos (KPIs): Faturamento total, comissões pagas, equipe ativa e volume de estoque;
  - Ranking de desempenho dos vendedores e status de turno.
- **Painel de Recursos Humanos (`RhScreen.kt`):**
  - Relação de todos os colaboradores e cargos;
  - Monitoramento de estado de turno (Ativo / Pausa / Ausente);
  - Cadastro de novos funcionários no sistema.

---

## 🛠️ Tecnologias Utilizadas
* **Linguagem:** Kotlin 1.9.23
* **Interface Gráfica:** Jetpack Compose + Material 3
* **Arquitetura:** MVVM (Model-View-ViewModel) + Repository Pattern + Clean Architecture
* **Comunicação de Rede:** Retrofit 2 + OkHttp 3 + Gson Converter
* **Assincronia:** Kotlin Coroutines + StateFlow
* **Carregamento de Imagens:** Coil Compose
* **Navegação:** Jetpack Navigation Compose

---

## 🚀 Como Abrir e Rodar no Android Studio

1. Abra o **Android Studio**.
2. Clique em **File > Open** (ou *Open Project* na tela inicial).
3. Selecione a pasta:
   ```
   C:\Users\eduardo\.gemini\antigravity\scratch\trendvibes-android
   ```
4. O Android Studio sincronizará o projeto automaticamente via Gradle.
5. Selecione um dispositivo (Emulador Android ou celular conectado via USB com depuração ativada).
6. Clique no botão verde de **Run** (`Shift + F10`) para instalar e executar o app!

---

## 🔗 Conectando ao Backend C# (.NET 8)

O app está configurado por padrão para conectar em:
* **No Emulador Android:** `http://10.0.2.2:5208/` (o emulador mapeia o IP `10.0.2.2` direto para o `localhost` do seu computador).
* **Em Celular Físico:** Clique no ícone de engrenagem (⚙️) na tela de login do staff e digite o IP local da sua máquina (ex: `http://192.168.1.100:5208/`).
* **Modo Offline / Demonstração:** Se o servidor C# não estiver rodando no momento, o app entra automaticamente no modo simulado local para que todas as telas possam ser testadas imediatamente!
