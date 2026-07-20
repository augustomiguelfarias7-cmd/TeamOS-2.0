9# TeamOS 2.0 — Arquitetura & Roadmap

Este documento descreve a arquitetura pretendida e o plano por fases. Um "OS completo" (kernel + init + drivers) não é construído do zero — o TeamOS é uma **camada de experiência** (shell/launcher + runtime de apps WebView) que roda **sobre um núcleo Linux/Android existente**. Isso é como Chrome OS e Android funcionam na prática.

TeamOS Shell (launcher, loja, assistente.

**Por que reutilizar Android/Linux como base?** Escrever kernel, drivers e init do zero levaria anos e não roda em hardware real de celular sem enorme esforço. Reutilizar o núcleo Linux (via Android) nos dá drivers, gerenciamento de arquivos, energia e touch de graça — e o TeamOS vira o launcher + suíte de apps do sistema. Um app *launcher* no Android substitui a tela inicial e é, na prática, "o sistema" do ponto de vista do usuário.

## Opções de plataforma para o Shell


## Linguagens

- **Kotlin/Java** — shell, launcher, serviços de sistema (Android).
- **JavaScript/TypeScript** — apps web e o assistente de IA (WebView).
- **Python** — pipelines/ferramentas de apoio quando necessário.
- **C++** — módulos de performance via JNI/NDK quando necessário.

## Componentes

### 1. Setup inicial (sem login)
Tela "Olá, tudo bem com você?" → botão **Ir** → seleção de idioma (7 locales, padrão pt-BR) → escolha de assistente → conclusão. Sem conta/login. Textos em `data/i18n/`.

### 2. Launcher (tela inicial)
Grade de apps, dock e barra de busca (Google). Apps iniciais em destaque: YouTube, Reddit, Navegador. Pré-instalados do sistema listados no catálogo (`system: true`).

### 3. Loja de Apps
Lê `data/apps.json`. "Instalar" cria um exibidor WebView (atalho) para a URL do app. Categorias: Google, OpenAI, Microsoft, Dev, Social, Produtividade, IA, Sistema.

### 4. Runtime WebView
Cada app abre numa janela WebView isolada (user-agent mobile, sem barra de endereço). Downloads e permissões controlados pelo sistema.

### 5. IA do sistema (assistente)
O recurso de IA do TeamOS é o **assistente padrão**: um overlay compacto do **ChatGPT** (ou Google Gemini) por cima dos apps, acessível pelo dock e projetado para o gesto de segurar o botão de energia. Alternável em Configurações.

### 6. Navegador
Busca padrão Google; IA (ChatGPT) na barra lateral; configurável (motor de busca).

### 7. Configurações
Idioma, assistente padrão, navegador/motor de busca, Bem-estar Digital. Objetivo: bem completa.


