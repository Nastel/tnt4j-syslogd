@echo off
setlocal
set RUNDIR=%~p0

set CLPATH=%RUNDIR%../jkool-jesl.jar;%RUNDIR%../lib/*;%RUNDIR%../lib/slf4j-simple/*
set MAINCL=com.jkool.jesl.net.syslogd.Syslogd
set TNT4JOPTS=-Dorg.slf4j.simpleLogger.defaultLogLevel=debug -Dtnt4j.dump.on.vm.shutdown=true -Dtnt4j.dump.provider.default=true -Dtnt4j.config=%RUNDIR%../config/tnt4j.properties
java %TNT4JOPTS% -classpath %CLPATH% %MAINCL% -h 0.0.0.0 -p 5140 tcp
endlocal