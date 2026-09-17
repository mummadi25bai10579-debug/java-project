@echo off
echo ==================================================
echo Running Smart Library Management System Tests...
echo ==================================================

java -Dfile.encoding=UTF-8 -cp "bin;lib/sqlite-jdbc.jar" com.library.TestRunner
