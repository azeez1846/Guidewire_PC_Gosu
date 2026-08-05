#!/usr/bin/env zsh
# =============================================================================
# stop-server.sh — Guidewire PolicyCenter Jetty Server Stop Script
#
# Usage:
#   ./stop-server.sh           # graceful stop via PID file
#   ./stop-server.sh --force   # SIGKILL if graceful stop fails
# =============================================================================

set -euo pipefail

SCRIPT_DIR="${0:a:h}"
PID_FILE="$SCRIPT_DIR/logs/server.pid"
FORCE="${1:-}"

if [[ ! -f "$PID_FILE" ]]; then
  echo "ℹ️   No PID file found. Server may not be running."
  # Also kill any stray java processes on port 8085 just in case
  STRAY=$(lsof -ti :8085 2>/dev/null || true)
  if [[ -n "$STRAY" ]]; then
    echo "⚠️   Found stray process on port 8085 (PID $STRAY). Killing..."
    kill "$STRAY" 2>/dev/null || true
    echo "✅  Killed."
  fi
  exit 0
fi

PID=$(cat "$PID_FILE")

if ! kill -0 "$PID" 2>/dev/null; then
  echo "⚠️   Process $PID is not running. Cleaning up stale PID file."
  rm -f "$PID_FILE"
  exit 0
fi

echo "🛑  Stopping Guidewire PolicyCenter (PID $PID)..."

if [[ "$FORCE" == "--force" ]]; then
  kill -9 "$PID" && echo "✅  Force-killed PID $PID."
else
  kill -15 "$PID"
  # Wait up to 10s for graceful shutdown
  for i in {1..10}; do
    if ! kill -0 "$PID" 2>/dev/null; then
      echo "✅  Server stopped gracefully."
      rm -f "$PID_FILE"
      exit 0
    fi
    sleep 1
    printf "."
  done
  echo ""
  echo "⚠️   Graceful stop timed out. Force-killing..."
  kill -9 "$PID" 2>/dev/null || true
  echo "✅  Force-killed."
fi

rm -f "$PID_FILE"
