@echo off
echo ==================================================
echo Launching Smart Library Management System...
echo ==================================================

java -Dfile.encoding=UTF-8 -cp "bin;lib/sqlite-jdbc.jar" com.library.Main
