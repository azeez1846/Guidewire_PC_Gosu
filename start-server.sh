#!/usr/bin/env zsh
# =============================================================================
# start-server.sh — Guidewire PolicyCenter Jetty Server Startup Script
#
# Usage:
#   ./start-server.sh           # start on default port 8085
#   ./start-server.sh 9090      # start on custom port
#
# Behaviour:
#   - Refuses to start if server is already running on the target port
#   - Writes PID to ./logs/server.pid for clean shutdown via stop-server.sh
#   - Writes stdout+stderr to ./logs/server.log (rotated on each start)
#   - Uses Maven-resolved classpath so no manual jar management needed
#   - Rebuilds stale classes automatically before starting
# =============================================================================

set -euo pipefail

SCRIPT_DIR="${0:a:h}"
cd "$SCRIPT_DIR"

PORT="${1:-8085}"
LOG_DIR="$SCRIPT_DIR/logs"
LOG_FILE="$LOG_DIR/server.log"
PID_FILE="$LOG_DIR/server.pid"

mkdir -p "$LOG_DIR"

# ── 1. Guard: refuse to double-start ─────────────────────────────────────────
if [[ -f "$PID_FILE" ]]; then
  EXISTING_PID=$(cat "$PID_FILE")
  if kill -0 "$EXISTING_PID" 2>/dev/null; then
    echo "✅  Server is already running (PID $EXISTING_PID) on port $PORT"
    echo "    Logs: $LOG_FILE"
    echo "    Stop: ./stop-server.sh"
    exit 0
  else
    echo "⚠️   Stale PID file found (PID $EXISTING_PID is dead). Cleaning up."
    rm -f "$PID_FILE"
  fi
fi

# ── 2. Compile if needed ─────────────────────────────────────────────────────
echo "🔨  Compiling (incremental)..."
mvn compile -q

# ── 3. Resolve classpath once ────────────────────────────────────────────────
echo "📦  Resolving classpath..."
mvn dependency:build-classpath -Dmdep.outputFile=target/classpath.txt -q
CP="target/classes:$(cat target/classpath.txt):lib/*"

# ── 4. Launch server in the background ───────────────────────────────────────
echo "🚀  Starting Guidewire PolicyCenter on port $PORT ..."

nohup java \
  -XX:+UseZGC \
  --enable-preview \
  -XX:+UseStringDeduplication \
  -cp "$CP" \
  com.guidewire.pc.App \
  "$PORT" \
  >> "$LOG_FILE" 2>&1 &

SERVER_PID=$!
echo "$SERVER_PID" > "$PID_FILE"

# ── 5. Health-check loop (wait up to 30s for server to bind) ────────────────
echo "⏳  Waiting for server to bind on port $PORT ..."
for i in {1..30}; do
  if lsof -i :"$PORT" -sTCP:LISTEN >/dev/null 2>&1; then
    echo ""
    echo "==============================================================="
    echo "  ✅  Guidewire PolicyCenter is UP"
    echo "  URL   : http://localhost:$PORT"
    echo "  Login : su / gw"
    echo "  Logs  : $LOG_FILE"
    echo "  PID   : $SERVER_PID  (saved to $PID_FILE)"
    echo "  Stop  : ./stop-server.sh"
    echo "==============================================================="
    exit 0
  fi
  sleep 1
  printf "."
done

echo ""
echo "❌  Server did not bind on port $PORT within 30s. Check logs:"
echo "    tail -50 $LOG_FILE"
exit 1
