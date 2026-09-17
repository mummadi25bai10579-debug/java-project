#!/usr/bin/env bash
set -e

echo "=================================================="
echo "Compiling Smart Library Management System..."
echo "=================================================="

mkdir -p bin

find src -name "*.java" > sources.txt

javac -encoding UTF-8 -cp "lib/sqlite-jdbc.jar" -d bin @sources.txt

rm -f sources.txt

echo "[SUCCESS] Compilation completed successfully into bin/"
