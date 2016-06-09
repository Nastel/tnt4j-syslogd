@echo off
setlocal
set RUNDIR=%~p0

set CLPATH=%RUNDIR%../tnt4j-syslogd.jar;%RUNDIR%../lib/*
set MAINCL=com.jkoolcloud.jesl.net.syslogd.Syslogd
set TNT4JOPTS=-Dorg.slf4j.simpleLogger.defaultLogLevel=debug -Dtnt4j.dump.on.vm.shutdown=true -Dtnt4j.dump.provider.default=true -Dtnt4j.config=%RUNDIR%../config/tnt4j.properties
java %TNT4JOPTS% -classpath %CLPATH% %MAINCL% -h 0.0.0.0 -p 5140 tcp
endlocal