# TeamOS 2.0

**TeamOS** é um sistema operacional mobile focado em **web apps** — pense num "Chrome OS para celular". Em vez de instalar aplicativos nativos pesados, a **Loja de Apps** instala *exibidores* leves (WebView) que abrem a interface web de cada serviço numa janela dedicada e sem barra de navegação.

> Instalar um app **não** instala o site — instala um invólucro WebView que exibe aquele site como se fosse um app.

## Visão

- **Loja de Apps** com 200+ web apps (Google, OpenAI, Microsoft, GitHub, Replit, Lovable, e muito mais).
- **Team AI** — pacote de IA do sistema: edição de imagem, melhoria de qualidade de vídeo (upscale quadro a quadro), desenho sobre imagens no touch e legendas ao vivo (tradução local).
- **Assistente padrão** chamado ao segurar o botão de energia — ChatGPT (padrão) ou Google Gemini, alternável.
- **Navegador** próprio com busca padrão Google e IA (ChatGPT) na barra lateral.
- **Setup inicial** multilíngue (sem login): pt-BR (padrão), pt-PT, en-US, en-GB, coreano, japonês e hindi.
- Apps pré-instalados: Galeria, Configurações, Bem-estar Digital, Team AI, ChatGPT, Navegador, Bloco de Notas, Loja de Apps.

## Estrutura do repositório

```
data/
  apps.json            # Catálogo da Loja de Apps (WebView)
  i18n/                # Textos do sistema em 7 idiomas
docs/
  ARCHITECTURE.md      # Arquitetura e roadmap por fases
```

A base técnica (plataforma de execução, launcher, runtime de WebView) está descrita em [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md). Este repositório está sendo construído em **fases** — veja o roadmap.

## Status

🚧 Em construção. Fase 1 (fundação): catálogo de apps, i18n e roadmap. A plataforma de UI (Android/Kotlin ou PWA web) está sendo definida com o mantenedor antes da Fase 2.
