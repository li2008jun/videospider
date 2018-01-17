@echo off & setlocal enabledelayedexpansion

set LIB_JARS=""
cd ..\lib
for %%i in (*) do set LIB_JARS=!LIB_JARS!;..\lib\%%i
cd ..\bin
echo %LIB_JARS%
java -classpath %LIB_JARS% com.aioff.spider.videospider.SpiderMain