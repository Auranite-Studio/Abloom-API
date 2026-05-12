#!/bin/sh

# Gradle wrapper script for Linux/Unix

GRADLE_USER_HOME="${GRADLE_USER_HOME:-$HOME/.gradle}"
GRADLE_WRAPPER_JAR="$(dirname "$0")/gradle/wrapper/gradle-wrapper.jar"

if [ ! -f "$GRADLE_WRAPPER_JAR" ]; then
    echo "ERROR: Gradle wrapper JAR not found at $GRADLE_WRAPPER_JAR"
    exit 1
fi

exec java -jar "$GRADLE_WRAPPER_JAR" "$@"
