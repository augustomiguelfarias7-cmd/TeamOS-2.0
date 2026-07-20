#!/usr/bin/env python3
# -*- coding: utf-8 -*-

"""
TeamOS 2.0 - Sistema de Build Personalizado
Este script simula a compilação e empacotamento das partições do sistema operacional Android/AOSP para o TeamOS.
Ele automatiza:
1. Compilação do aplicativo launcher do TeamOS via Gradle (Kotlin)
2. Criação das árvores de diretórios de partições (system, vendor, product, boot)
3. Cópia dos artefatos (incluindo o APK recém-construído) para a árvore de partição correta
4. Criação das imagens ext4 de partições reais para system.img, vendor.img, product.img
5. Geração de um boot.img mock consolidado
6. Empacotamento de todos os artefatos de saída em out/
"""

import os
import sys
import shutil
import subprocess

# Definição de cores para console
GREEN = "\033[92m"
BLUE = "\033[94m"
YELLOW = "\033[93m"
RED = "\033[91m"
RESET = "\033[0m"

def log_info(msg):
    print(f"{BLUE}[INFO]{RESET} {msg}")

def log_success(msg):
    print(f"{GREEN}[SUCCESS]{RESET} {msg}")

def log_warn(msg):
    print(f"{YELLOW}[WARN]{RESET} {msg}")

def log_error(msg):
    print(f"{RED}[ERROR]{RESET} {msg}")

# Caminhos do projeto
REPO_ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
SYS_INFRA = os.path.join(REPO_ROOT, "sys_infra")
OUT_DIR = os.path.join(SYS_INFRA, "out")
TEMP_BUILD_DIR = os.path.join(SYS_INFRA, "temp_build")

def check_requirements():
    log_info("Verificando ferramentas de build disponíveis...")
    mke2fs_path = shutil.which("mke2fs")
    if mke2fs_path:
        log_success(f"Ferramenta mke2fs encontrada em: {mke2fs_path}")
    else:
        # Se mke2fs não estiver no PATH global, tentar caminhos conhecidos do Android SDK
        sdk_mke2fs = "/opt/android-sdk/platform-tools/mke2fs"
        if os.path.exists(sdk_mke2fs):
            os.environ["PATH"] += os.pathsep + "/opt/android-sdk/platform-tools"
            log_success(f"mke2fs do Android SDK encontrado e adicionado ao PATH: {sdk_mke2fs}")
        else:
            log_warn("mke2fs não encontrado. O script usará um fallback de simulação para gerar os arquivos .img vazios caso necessário.")

def build_launcher_apk():
    log_info("Iniciando compilação do launcher do TeamOS via Gradle...")
    gradlew_path = os.path.join(REPO_ROOT, "gradlew")

    if not os.path.exists(gradlew_path):
        log_error("Script gradlew não encontrado no diretório raiz do repositório.")
        sys.exit(1)

    try:
        # Executar comando gradle para gerar APK de debug
        cmd = [gradlew_path, "assembleDebug"]
        log_info(f"Executando: {' '.join(cmd)}")
        result = subprocess.run(cmd, cwd=REPO_ROOT, stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True)

        if result.returncode != 0:
            log_error("Erro ao compilar o aplicativo com Gradle:")
            print(result.stderr)
            sys.exit(1)

        log_success("Launcher TeamOS compilado com sucesso pelo Gradle!")
    except Exception as e:
        log_error(f"Ocorreu uma exceção inesperada durante a compilação Gradle: {str(e)}")
        sys.exit(1)

def locate_and_copy_apk(product_app_dir):
    log_info("Localizando o APK compilado...")
    apk_search_dir = os.path.join(REPO_ROOT, "app", "build", "outputs", "apk", "debug")
    apk_file = os.path.join(apk_search_dir, "app-debug.apk")

    if not os.path.exists(apk_file):
        log_error(f"Arquivo APK não encontrado em {apk_file}. Certifique-se de que a compilação Gradle foi bem-sucedida.")
        sys.exit(1)

    dest_apk_dir = os.path.join(product_app_dir, "TeamOSLauncher")
    os.makedirs(dest_apk_dir, exist_ok=True)
    dest_apk_file = os.path.join(dest_apk_dir, "TeamOSLauncher.apk")

    log_info(f"Copiando APK de {apk_file} para {dest_apk_file}...")
    shutil.copy2(apk_file, dest_apk_file)
    log_success("Launcher APK copiado para a partição product.")

def create_partition_images():
    log_info("Criando imagens de partições do sistema...")

    # Criar subdiretórios temporários da árvore de partições para o empacotamento
    partitions = ["system", "vendor", "product", "boot"]
    for part in partitions:
        part_temp_path = os.path.join(TEMP_BUILD_DIR, part)
        os.makedirs(part_temp_path, exist_ok=True)

        # Copiar todos os arquivos da infraestrutura correspondente do código fonte para a pasta temporária
        src_part_dir = os.path.join(SYS_INFRA, part)
        if os.path.exists(src_part_dir):
            log_info(f"Sincronizando arquivos para a partição temporária: {part}")
            for item in os.listdir(src_part_dir):
                s = os.path.join(src_part_dir, item)
                d = os.path.join(part_temp_path, item)
                if os.path.isdir(s):
                    shutil.copytree(s, d, dirs_exist_ok=True)
                else:
                    shutil.copy2(s, d)

    # Caminho do APK em product
    product_app_dir = os.path.join(TEMP_BUILD_DIR, "product", "app")
    os.makedirs(product_app_dir, exist_ok=True)
    locate_and_copy_apk(product_app_dir)

    # Limpar pasta de saída antes de recriar as imagens
    if os.path.exists(OUT_DIR):
        shutil.rmtree(OUT_DIR)
    os.makedirs(OUT_DIR, exist_ok=True)

    # Criando os arquivos .img reais para system, vendor e product usando mke2fs
    # Se mke2fs falhar ou não estiver disponível, faremos fallback gerando mock binários (.img vazios/simulados)
    mke2fs_bin = shutil.which("mke2fs")
    if not mke2fs_bin:
        # Tenta de novo no SDK Android caso não tenha sido pego no PATH global
        sdk_mke2fs = "/opt/android-sdk/platform-tools/mke2fs"
        if os.path.exists(sdk_mke2fs):
            mke2fs_bin = sdk_mke2fs

    for part in ["system", "vendor", "product"]:
        img_output = os.path.join(OUT_DIR, f"{part}.img")
        part_src_dir = os.path.join(TEMP_BUILD_DIR, part)

        # Tamanho das partições para a imagem de sistema leve (ex: 20MB para caber os testes facilmente)
        part_size_mb = "24M" if part == "product" else "16M"

        if mke2fs_bin:
            try:
                log_info(f"Construindo imagem de partição EXT4 real: {part}.img ({part_size_mb})...")
                # Primeiro, criamos um arquivo vazio do tamanho desejado
                size_bytes = 24 * 1024 * 1024 if part == "product" else 16 * 1024 * 1024
                with open(img_output, "wb") as f:
                    f.truncate(size_bytes)

                # Executa mke2fs para formatar como ext4
                cmd = [mke2fs_bin, "-t", "ext4", "-b", "4096", "-F", img_output]
                subprocess.run(cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE, check=True)

                log_success(f"Imagem EXT4 {part}.img inicializada com sucesso!")

                # Para uma simulação completa sem a necessidade de privilégios de ROOT para mount,
                # nós criamos adicionalmente um arquivo tar gzip (.tgz) contendo a estrutura real dos arquivos.
                # Isso garante que a árvore completa de diretórios permaneça preservada no arquivo de entrega.
                archive_output = os.path.join(OUT_DIR, f"{part}_root.tar.gz")
                log_info(f"Gerando arquivo compactado contendo a estrutura da partição {part}: {archive_output}")
                subprocess.run(["tar", "-czf", archive_output, "-C", part_src_dir, "."], check=True)

            except Exception as e:
                log_warn(f"Falha ao gerar imagem ext4 real para {part} via mke2fs: {str(e)}. Usando fallback de simulação.")
                create_fallback_img(img_output, part_src_dir, part_size_mb)
        else:
            create_fallback_img(img_output, part_src_dir, part_size_mb)

    # Criação do boot.img consolidado simulado
    boot_img_output = os.path.join(OUT_DIR, "boot.img")
    boot_src_dir = os.path.join(TEMP_BUILD_DIR, "boot")
    log_info("Construindo imagem consolidada de inicialização: boot.img...")

    # O boot.img no Android geralmente contém o kernel (zImage) e a ramdisk (initramfs).
    # Vamos empacotar esses itens de forma consolidada e gerar o boot.img simulado.
    boot_archive = os.path.join(OUT_DIR, "boot_root.tar.gz")
    try:
        subprocess.run(["tar", "-czf", boot_archive, "-C", boot_src_dir, "."], check=True)
        # Cria arquivo boot.img mock
        with open(boot_img_output, "wb") as f:
            f.truncate(8 * 1024 * 1024) # 8MB boot image mock
        log_success("boot.img e boot_root.tar.gz gerados com sucesso!")
    except Exception as e:
        log_error(f"Erro ao empacotar boot.img: {str(e)}")

def create_fallback_img(img_output, src_dir, size_str):
    log_info(f"Construindo imagem simulada (fallback): {os.path.basename(img_output)} ({size_str})...")
    # Gerar um arquivo .img vazio
    size_num = int(size_str.replace("M", ""))
    size_bytes = size_num * 1024 * 1024
    with open(img_output, "wb") as f:
        f.truncate(size_bytes)

    # Também gera a estrutura tar.gz para preservação completa
    archive_output = img_output.replace(".img", "_root.tar.gz")
    subprocess.run(["tar", "-czf", archive_output, "-C", src_dir, "."], check=True)
    log_success(f"Imagem simulada e estrutura tar {os.path.basename(img_output)} gerada com sucesso!")

def cleanup():
    log_info("Limpando diretórios temporários de compilação...")
    if os.path.exists(TEMP_BUILD_DIR):
        shutil.rmtree(TEMP_BUILD_DIR)
    log_success("Limpeza concluída!")

def main():
    print("=================================================================")
    print("      SISTEMA DE BUILD PERSONALIZADO - TEAMOS CORE INFRA         ")
    print("=================================================================")

    # Criar diretório temporário para processamento de build
    if os.path.exists(TEMP_BUILD_DIR):
        shutil.rmtree(TEMP_BUILD_DIR)
    os.makedirs(TEMP_BUILD_DIR, exist_ok=True)

    try:
        check_requirements()
        build_launcher_apk()
        create_partition_images()
        cleanup()

        print("\n=================================================================")
        log_success("PROCESSO DE COMPILAÇÃO E GERAÇÃO DE IMAGENS CONCLUÍDO!")
        log_info(f"Todos os artefatos de saída estão disponíveis em: {OUT_DIR}")
        print("Artefatos gerados:")
        for f in sorted(os.listdir(OUT_DIR)):
            file_path = os.path.join(OUT_DIR, f)
            size_kb = os.path.getsize(file_path) / 1024
            print(f" -> {f:<20} ({size_kb:.2f} KB)")
        print("=================================================================")

    except Exception as e:
        log_error(f"Erro catastrófico no sistema de build: {str(e)}")
        cleanup()
        sys.exit(1)

if __name__ == "__main__":
    main()
