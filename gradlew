#!/bin/sh
# Gradle wrapper bootstrap script (Unix)
# If gradle-wrapper.jar is missing, download it automatically.

set -e

APP_NAME="Gradle"
APP_BASE_NAME=`basename "$0"`
APP_HOME=`dirname "$0"`
APP_HOME=`cd "$APP_HOME" && pwd`

WRAPPER_JAR="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"

if [ ! -f "$WRAPPER_JAR" ]; then
    echo "Downloading Gradle wrapper JAR..."
    mkdir -p "$APP_HOME/gradle/wrapper"
    curl -L -o "$WRAPPER_JAR" \
        "https://github.com/gradle/gradle/raw/v8.1.1/gradle/wrapper/gradle-wrapper.jar" 2>/dev/null \
    || wget -O "$WRAPPER_JAR" \
        "https://github.com/gradle/gradle/raw/v8.1.1/gradle/wrapper/gradle-wrapper.jar"
    echo "Gradle wrapper JAR downloaded."
fi

exec java -jar "$WRAPPER_JAR" "$@"
