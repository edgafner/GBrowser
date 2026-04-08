#!/bin/bash
# Warn if pushing without recent build
project_dir="${CLAUDE_PROJECT_DIR:-.}"
recent_build=false

# Check for recent build artifacts
if [ -d "$project_dir/build" ]; then
    recent=$(find "$project_dir/build" -name "*.class" -mmin -30 -print -quit 2>/dev/null)
    [ -n "$recent" ] && recent_build=true
fi

# Check Gradle activity as fallback (covers UP-TO-DATE builds)
if [ "$recent_build" = false ] && [ -d "$project_dir/.gradle" ]; then
    recent=$(find "$project_dir/.gradle" -name "file-system.probe" -mmin -30 -print -quit 2>/dev/null)
    [ -n "$recent" ] && recent_build=true
fi

if [ "$recent_build" = false ]; then
    echo "WARNING: No recent build detected. Run ./gradlew compileKotlin --no-scan before pushing." >&2
fi
exit 0
