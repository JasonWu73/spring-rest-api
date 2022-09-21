@echo off

set JAVA_EXE=D:\Test\jdk-11.0.14\bin\javaw.exe
set JAR_PATH=D:\Test\spring-rest-api.jar
set LOG_CFG=D:\Test\config\logback-spring.xml

start %JAVA_EXE% -jar %JAR_PATH% --logging.config=file:%LOG_CFG%

exit
