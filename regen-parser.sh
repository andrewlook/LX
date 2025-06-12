#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"

JAVA_MAIN_DIR="${SCRIPT_DIR}/src/main/java"
EXPRESSION_PKG_DIR="${JAVA_MAIN_DIR}/heronarts/lx/structure/expression"

if [ -d "${EXPRESSION_PKG_DIR}" ]; then
    echo "found ${EXPRESSION_PKG_DIR}; deleting"
    rm -rf "${EXPRESSION_PKG_DIR}"
fi

pushd "$JAVA_MAIN_DIR"
npx canopy "${SCRIPT_DIR}/expression.peg" --lang java --out heronarts/lx/structure/expression

echo "saved to: ${EXPRESSION_PKG_DIR}"
