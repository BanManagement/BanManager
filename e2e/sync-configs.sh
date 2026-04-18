#!/bin/bash
# Sync shared config files from common module to all E2E platform configs
# Run this when common config files are updated

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
COMMON_RESOURCES="$SCRIPT_DIR/../common/src/main/resources"

# Files that are shared across platforms (don't need test-specific modifications)
SHARED_FILES=(
    "console.yml"
    "webhooks.yml"
    "exemptions.yml"
    "geoip.yml"
    "messages.yml"
    "reasons.yml"
    "schedules.yml"
)

# Platform config directories
PLATFORM_CONFIGS=(
    "$SCRIPT_DIR/platforms/bukkit/configs"
    "$SCRIPT_DIR/platforms/fabric/configs"
    "$SCRIPT_DIR/platforms/sponge/configs/banmanager"
    "$SCRIPT_DIR/platforms/sponge7/configs/banmanager"
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
    fi
done

echo "Done! Synced ${#SHARED_FILES[@]} files to ${#PLATFORM_CONFIGS[@]} locations."
echo ""
echo "Note: config.yml files are NOT synced as they contain test-specific settings"
echo "      (database host, chatPriority, etc.)"
