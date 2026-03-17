#!/usr/bin/env sh

set -e
DIR="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
GRADLE_VERSION=8.7
WRAPPER_JAR="$DIR/gradle/wrapper/gradle-wrapper.jar"
if [ ! -f "$WRAPPER_JAR" ]; then
  echo "Missing gradle wrapper jar. Generate it locally with: gradle wrapper --gradle-version $GRADLE_VERSION"
  exit 1
fi
exec java -Dorg.gradle.appname=gradlew -classpath "$WRAPPER_JAR" org.gradle.wrapper.GradleWrapperMain "$@"
