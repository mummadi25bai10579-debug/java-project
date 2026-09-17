#!/usr/bin/env bash
set -e

echo "=================================================="
echo "Launching Smart Library Management System..."
echo "=================================================="

# Linux/macOS uses colon as classpath separator
java -Dfile.encoding=UTF-8 -cp "bin:lib/sqlite-jdbc.jar" com.library.Main
