# Infraestrutura Interna do Sistema Operacional TeamOS 2.0

Este diretório contém a estrutura de arquivos e o sistema de compilação personalizado para simular e gerar uma imagem do sistema operacional **TeamOS 2.0**, baseado na arquitetura interna do **Android/AOSP** (Android Open Source Project).

Esta solução de infraestrutura do sistema operacional foca no desenvolvimento interno do núcleo do sistema (sistema de build, partições de armazenamento, inicialização, kernel e drivers) de forma isolada, **sem alterar o design visual, os layouts, os aplicativos ou componentes de interface gráfica** existentes no repositório.

---

## 📂 Estrutura de Diretórios Criada

```
sys_infra/
├── boot/                        # Arquivos da partição de boot (Kernel e Ramdisk)
│   └── kernel.config            # Configuração do Kernel Linux para o TeamOS
├── system/                      # Configurações essenciais da partição de sistema (/system)
│   ├── init.rc                  # Script de inicialização oficial (Sintaxe de Init Android)
│   ├── fstab.teamos             # Tabela de montagem de partições
│   └── build.prop               # Propriedades de sistema e variáveis globais
├── vendor/                      # Arquivos da partição do fabricante (/vendor)
│   └── drivers/                 # Módulos de Kernel e scripts para gerenciamento de hardware
│       ├── teamos_display.c     # Mock de driver de tela C do TeamOS
│       ├── teamos_display.h     # Estrutura e cabeçalhos do driver de tela
│       └── load_drivers.sh      # Script simulador para carregar módulos do kernel (insmod)
├── product/                     # Aplicativos de sistema e customizações do produto (/product)
│   └── app/                     # Destino do launcher do TeamOS pré-instalado (copiado automaticamente)
├── out/                         # Artefatos e imagens de sistema gerados pelo build (.img)
├── build_system.py              # Script automatizado em Python de compilação e empacotamento
└── build.sh                     # Script shell de gatilho rápido para o processo de build
```

---

## 📄 Detalhamento Técnico dos Arquivos Criados

### 1. Partição `boot/`
*   **`boot/kernel.config`**:
    Contém a configuração de Kernel Linux (arquiteturas ARM64/x86\_64) customizada para o TeamOS. Ela ativa explicitamente funcionalidades críticas exigidas pela infraestrutura do Android, incluindo:
    *   `CONFIG_ANDROID_BINDER_IPC` / `CONFIG_ANDROID_BINDERFS`: Comunicação de IPC (inter-process communication) nativa essencial do Android.
    *   `CONFIG_ASHMEM` e `CONFIG_ION`: Subsistemas do kernel para alocação e compartilhamento eficiente de memória física.
    *   `CONFIG_PREEMPT_RT` e suporte para touch/tela e sistemas de arquivos ext4/f2fs.

### 2. Partição `system/`
*   **`system/init.rc`**:
    Arquivo com sintaxe oficial do Android Init Language (`init.rc`). Ele é responsável por:
    *   Orquestrar as fases de inicialização do sistema (`early-init`, `init`, `fs`, `post-fs`, `boot`).
    *   Exportar variáveis de ambiente fundamentais (ex: `ANDROID_ROOT`, `ANDROID_DATA`, `TEAMOS_VERSION`).
    *   Configurar a montagem das partições de armazenamento.
    *   Declarar e gerenciar serviços vitais de core/HALs do Android, como `servicemanager`, `surfaceflinger`, `vold` e o serviço nativo `teamos_launcher` (que executa a `MainActivity` principal do TeamOS).
*   **`system/fstab.teamos`**:
    Tabela de partições do sistema que define as regras e sinalizadores (flags) para montagem segura e otimizada das partições `/boot`, `/system`, `/vendor`, `/product` e `/data` (usando F2FS e encriptação AES-256-XTS para dados do usuário).
*   **`system/build.prop`**:
    Propriedades de compilação globais do sistema. Configura variáveis de build como nível de SDK da API (API 35/Android 15), impressão digital da compilação, propriedades do heap da máquina virtual ART (dalvik.vm) e configurações personalizadas para assistentes IA (`ro.teamos.assistant.default=ChatGPT`) e idiomas do sistema.

### 3. Partição `vendor/`
*   **`vendor/drivers/teamos_display.c` e `teamos_display.h`**:
    Um mock de desenvolvimento de baixo nível escrito em C representando um driver do Kernel Linux para displays móveis do TeamOS. Ele simula o ponto de entrada (`module_init`) e saída (`module_exit`) de drivers do sistema operacional e define parâmetros de hardware (resolução, taxa de atualização e nome do painel físico).
*   **`vendor/drivers/load_drivers.sh`**:
    Script executável simulador de HAL (Hardware Abstraction Layer). Ele faz o papel de gerenciador e carregador de drivers de baixo nível no sistema operacional utilizando comandos `insmod` para acoplar os módulos `.ko` do fabricante (vídeo, áudio e Wi-Fi) e configura canais de IPC de Binder com o sistema.

### 4. Partição `product/`
*   **`product/app/`**:
    Diretório de destino projetado para aplicativos nativos e launchers instalados diretamente no sistema de fábrica. O script de compilação copia automaticamente o APK construído do Launcher TeamOS para `product/app/TeamOSLauncher/TeamOSLauncher.apk` durante a fase de build.

---

## 🛠️ O Sistema de Build Personalizado (`build_system.py`)

A criação das imagens foi facilitada de forma elegante e independente sem requerer a compilação completa de gigabytes do AOSP convencional.
O script desenvolvido em **Python 3** (`sys_infra/build_system.py`) executa as seguintes tarefas sequencialmente:

1.  **Verificação de Pré-requisitos**: Garante que o interpretador do sistema possui acesso a ferramentas de montagem/formatação de disco EXT4 como o `mke2fs` (incluindo busca dinâmica no diretório do Android SDK instalado `/opt/android-sdk/platform-tools/`).
2.  **Compilação do Launcher Android**: Executa o Gradle Wrapper `./gradlew assembleDebug` de forma limpa a partir da raiz do projeto para gerar o pacote do launcher original do TeamOS.
3.  **Montagem da Árvore de Partições**: Sincroniza todos os arquivos de configuração que você projetou para uma estrutura temporária de compilação (`temp_build/`).
4.  **Cópia do APK**: Encontra o APK compilado pelo Gradle em `app/build/outputs/` e o instala automaticamente na árvore de diretórios `/product/app/TeamOSLauncher/`.
5.  **Geração das Imagens do Sistema**:
    *   Gera as imagens de bloco reais (`system.img`, `vendor.img`, `product.img`) formatadas em **EXT4** usando a ferramenta `mke2fs` do ecossistema Android.
    *   Para garantir que todos os arquivos, metadados e árvores de diretórios permaneçam legíveis sem necessitar de privilégios `sudo mount` (o que causaria falhas em ambientes CI/CD), o script simultaneamente empacota as partições em arquivos compactados oficiais (`system_root.tar.gz`, `vendor_root.tar.gz`, `product_root.tar.gz`).
    *   Gera de forma consolidada a imagem de inicialização `boot.img` com tamanho fixo, junto ao seu ramdisk compactado `boot_root.tar.gz`.
6.  **Limpeza**: Remove todos os resíduos temporários criados mantendo apenas os artefatos de saída limpos.

### Como Executar o Build

A partir da raiz do repositório, você pode disparar todo o ecossistema de build executando o atalho `build.sh`:

```bash
chmod +x sys_infra/build.sh
./sys_infra/build.sh
```

Os artefatos compilados e imagens do sistema serão salvos no diretório de saída: `sys_infra/out/`.
