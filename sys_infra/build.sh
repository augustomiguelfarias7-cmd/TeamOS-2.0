#!/bin/bash
# Script facilitador para executar o sistema de build personalizado do TeamOS

# Sair imediatamente caso ocorra algum erro
set -e

# Caminho absoluto da pasta deste script
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

echo "Iniciando build de infraestrutura do TeamOS..."
python3 "$DIR/build_system.py"
