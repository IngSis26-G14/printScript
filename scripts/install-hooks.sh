#!/bin/sh

set -eu

script_dir=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
repo_root=$(git -C "$script_dir" rev-parse --show-toplevel)

echo "Installing Git hooks..."

chmod +x "$repo_root/.githooks/pre-commit"
git -C "$repo_root" config --local core.hooksPath .githooks

echo "Git hooks installed successfully."
echo "Pre-commit hook: .githooks/pre-commit"

# Run from any directory: /bin/sh /path/to/printScript/scripts/install-hooks.sh
