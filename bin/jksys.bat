@echo off
setlocal
set RUNDIR=%~p0

set CLPATH=%RUNDIR%../tnt4j-syslogd.jar;%RUNDIR%../lib/*
set MAINCL=com.jkool.jesl.net.syslogd.SyslogSend
java -classpath %CLPATH% %MAINCL% %*
endlocal