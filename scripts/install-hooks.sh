#!/bin/sh

set -e

echo "Installing Git hooks..."

git config core.hooksPath .githooks

chmod +x .githooks/pre-commit

echo "Git hooks installed successfully."
echo "Pre-commit hook: .githooks/pre-commit"

#Para instalar el pre-commit corre este comando:  ./scripts/install-hooks.sh