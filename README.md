# tnt4j-syslogd
Syslogd over TNT4J implementation

Streaming Syslog over TNT4J 
===============================
Please follow these steps to stream syslog over TNT4J:

* Configure `config/tnt4j.properties`, 
	* Default configuration is under `com.jkool.jesl.net.syslogd` stanza.
* Run JESL syslogd `<jesl-home>/bin/jksysd > jksysd.json`. 
	* By default JESL `jksysd` binds to TCP port `5140` and writes out JSON formatted syslog messages.
	* JSON output can be played back using `<jesl-home>/bin/jksys` utility.
* Configure `syslog/rsyslog` to forward to JESL syslog daemon over TCP (`hostname` is where JESL `jksysd` is running)
	* RFC 3164 (e.g. `*.* @@hostname:5140`)
	* RFC 5424 (e.g. `*.* @@hostname:5140;RSYSLOG_SyslogProtocol23Format`)
* Send syslog messages from command line (`<jesl-home>/bin/jksys`):
```
$ jksys -h localhost -p 5140 -l error -f user tcp "appl-name[883]: my syslog mesasge about appl-name pid=883"
```
* Playback syslog JSON messages from command line (`<jesl-home>/bin/jksys`):
```
$ jksys -h localhost -p 5140 -f jksysd.json tcp
```
where `jksysd.json` is JSON output of JESL syslog daemon.
	
That should do it.

<b>NOTE:</b> Currently supports (RFC 3164) and the Structured Syslog protocol (RFC 5424).
