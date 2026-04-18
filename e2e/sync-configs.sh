#!/bin/bash
# Sync shared config files from common module to all E2E platform configs
# Run this when common config files are updated

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
COMMON_RESOURCES="$SCRIPT_DIR/../common/src/main/resources"

# Only sync files that are truly shared and don't contain test-specific settings.
# Most E2E config files (webhooks.yml, schedules.yml, etc.) are maintained
# per-platform with test-specific values (e.g. webhook-sink URLs, timing).
SHARED_FILES=(
)

# Platform config directories
PLATFORM_CONFIGS=(
    "$SCRIPT_DIR/platforms/bukkit/configs"
    "$SCRIPT_DIR/platforms/fabric/configs"
    "$SCRIPT_DIR/platforms/sponge/configs/banmanager"
    "$SCRIPT_DIR/platforms/velocity/configs/banmanager"
    "$SCRIPT_DIR/platforms/bungee/configs/banmanager"
)

echo "Syncing shared config files from common module..."

for config_dir in "${PLATFORM_CONFIGS[@]}"; do
    if [ -d "$config_dir" ]; then
        echo "  -> $config_dir"
        for file in "${SHARED_FILES[@]}"; do
            if [ -f "$COMMON_RESOURCES/$file" ]; then
                cp "$COMMON_RESOURCES/$file" "$config_dir/"
            fi
        done

        if [ -d "$COMMON_RESOURCES/messages" ]; then
            mkdir -p "$config_dir/messages"
            cp "$COMMON_RESOURCES/messages/"*.yml "$config_dir/messages/"
        fi
    fi
done

echo "Done! Synced messages/ directory to ${#PLATFORM_CONFIGS[@]} locations."
echo ""
echo "Note: Only the messages/ directory is synced automatically."
echo "      Other config files are maintained per-platform with test-specific settings."
