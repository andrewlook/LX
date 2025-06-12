#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"

npx canopy "${SCRIPT_DIR}/expression.peg" --lang java

EXPRESSION_PKG_DIR="${SCRIPT_DIR}/src/main/java/heronarts/lx/structure/expression"

if [[ -f "${EXPRESSION_PKG_DIR}" ]]; then
    rm -rf "${EXPRESSION_PKG_DIR}"
fi

mv "${SCRIPT_DIR}/expression" "${EXPRESSION_PKG_DIR}"

echo "saved to: ${EXPRESSION_PKG_DIR}"
