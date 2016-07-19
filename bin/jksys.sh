#! /bin/bash
RUNDIR=`pwd`

CLPATH="$RUNDIR/../../tnt4j-syslogd.jar:$RUNDIR/../lib/*"
MAINCL=com.jkoolcloud.jesl.net.syslogd.SyslogSend
java -classpath $CLPATH $MAINCL $*
