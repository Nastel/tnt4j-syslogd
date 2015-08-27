#! /bin/bash
RUNDIR=`pwd`

CLPATH="$RUNDIR/../../tnt4j-syslogd.jar:$RUNDIR/../lib/*"
MAINCL=com.jkool.jesl.net.syslogd.SyslogSend
java -classpath $CLPATH $MAINCL $*
