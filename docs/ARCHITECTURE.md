# TeamOS 2.0 — Arquitetura & Roadmap

Este documento descreve a arquitetura pretendida e o plano por fases. Um "OS completo" (kernel + init + drivers) não é construído do zero — o TeamOS é uma **camada de experiência** (shell/launcher + runtime de apps WebView) que roda **sobre um núcleo Linux/Android existente**. Isso é como Chrome OS e Android funcionam na prática.

## Camadas

```
┌──────────────────────────────────────────────┐
│  TeamOS Shell (launcher, loja, assistente...) │  ← o que construímos
├──────────────────────────────────────────────┤
│  Runtime de apps: WebView / iframe sandbox    │  ← exibidores dos web apps
├──────────────────────────────────────────────┤
│  Serviços de sistema (i18n, settings, IA)     │
├──────────────────────────────────────────────┤
│  Núcleo: Android (Linux kernel, init, HAL,    │  ← base reutilizada
│  drivers, gerenciador de arquivos)            │
└──────────────────────────────────────────────┘
```

**Por que reutilizar Android/Linux como base?** Escrever kernel, drivers e init do zero levaria anos e não roda em hardware real de celular sem enorme esforço. Reutilizar o núcleo Linux (via Android) nos dá drivers, gerenciamento de arquivos, energia e touch de graça — e o TeamOS vira o launcher + suíte de apps do sistema. Um app *launcher* no Android substitui a tela inicial e é, na prática, "o sistema" do ponto de vista do usuário.

## Opções de plataforma para o Shell

| Opção | Prós | Contras |
|---|---|---|
| **A. Android nativo (Kotlin)** — launcher + WebView | Vira o "OS" real no aparelho; acesso a botão de energia, arquivos, galeria; instalável em celular | Build/toolchain mais pesados; teste em emulador |
| **B. PWA/Web (JS/TS)** — protótipo no navegador | Demonstra tudo rápido; roda em qualquer lugar; fácil de iterar | Não vira um "OS" real; sem botão de energia/arquivos nativos |

A **Opção A (Android/Kotlin)** é a mais fiel à visão. A **Opção B** é ótima para prototipar a UX antes.

## Linguagens (conforme pedido)

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
O recurso de IA do TeamOS é o **assistente padrão**: um overlay compacto do **ChatGPT** (ou Google Gemini) por cima dos apps, acessível pelo dock e projetado para o gesto de segurar o botão de energia. Alternável em Configurações. (Não há mais um pacote "Team AI" separado.)

### 6. Navegador
Busca padrão Google; IA (ChatGPT) na barra lateral; configurável (motor de busca).

### 7. Configurações
Idioma, assistente padrão, navegador/motor de busca, Bem-estar Digital. Objetivo: bem completa.

## Roadmap por fases

- **Fase 1 — Fundação (atual):** catálogo de apps, i18n (7 idiomas), roadmap. ✅
- **Fase 2 — Shell MVP:** setup wizard + launcher + loja + runtime WebView na plataforma escolhida.
- **Fase 3 — Sistema:** Configurações, Navegador, Bloco de Notas, Galeria.
- **Fase 4 — Assistente & IA:** overlay do assistente (ChatGPT/Gemini), IA na barra lateral do navegador.
- **Fase 5 — Polimento & empacotamento.**
