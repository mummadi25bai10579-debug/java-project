#!/usr/bin/env bash
set -e

echo "=================================================="
echo "Running Smart Library Management System Tests..."
echo "=================================================="

# Linux/macOS uses colon as classpath separator
java -Dfile.encoding=UTF-8 -cp "bin:lib/sqlite-jdbc.jar" com.library.TestRunner
