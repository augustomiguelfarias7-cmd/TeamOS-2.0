   # TeamOS 2.0

**TeamOS** é um sistema operacional mobile focado em **web apps** — pense num "Chrome OS para celular". Em vez de instalar aplicativos nativos pesados, a **Loja de Apps** instala *exibidores* leves (WebView) que abrem a interface web de cada serviço numa janela dedicada e sem barra de navegação.

> Instalar um app **não** instala o site — instala um invólucro WebView que exibe aquele site como se fosse um app.

## Visão

- **Loja de Apps** com 200+ web apps (Google, OpenAI, Microsoft, GitHub, Replit, Lovable, e muito mais).
- **IA do sistema** — o recurso de inteligência artificial é o **assistente padrão** (ChatGPT, com Google Gemini como alternativa), acessível pelo dock e projetado para o gesto de segurar o botão de energia.
- **Assistente padrão** — ChatGPT (padrão) ou Google Gemini, alternável nas Configurações.
- **Navegador** próprio com busca padrão Google e IA (ChatGPT) na barra lateral.
- **Setup inicial** multilíngue (sem login): pt-BR (padrão), pt-PT, en-US, en-GB, coreano, japonês e hindi.
- Apps pré-instalados: Câmera, Galeria, Configurações, Bem-estar Digital, ChatGPT, Navegador, Bloco de Notas, Loja de Apps.

## Estrutura do repositório

```
data/
  apps.json            # Catálogo da Loja de Apps (WebView)
  i18n/                # Textos do sistema em 7 idiomas
docs/
  ARCHITECTURE.md      # Arquitetura e roadmap por fases
```

A base técnica (plataforma de execução, launcher, runtime de WebView) está descrita em [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md). 
