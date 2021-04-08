# tnt4j-syslogd
Syslogd over TNT4J implementation

Use Maven dependency:
```xml
    <dependency>
        <groupId>com.jkoolcloud.jesl.net</groupId>
        <artifactId>tnt4j-syslogd</artifactId>
        <version>0.3.4</version>
    </dependency>
```

Streaming Syslog to jKoolCloud 
===============================
JESL includes Syslog Daemon implementation. Please follow these steps to stream syslog to `jkoolcloud.com`:

* Obtain jKoolCloud account. Edit `config/tnt4j.properties`, 
	* Locate `com.jkoolcloud.jesl.net.syslogd` stanza and provide your API access token.
* Run JESL syslogd `<home>/bin/jksysd > jksysd.json`. 
	* By default JESL `jksysd` binds to TCP port `5140` and writes JSON formatted syslog messages.
	* JSON output can be played back using `<home>/bin/jksys` utility.
* Configure `syslog/rsyslog` to forward to JESL syslog daemon over TCP (`hostname` is where JESL `jksysd` is running)
	* RFC 3164 (e.g. `*.* @@hostname:5140`)
	* RFC 5424 (e.g. `*.* @@hostname:5140;RSYSLOG_SyslogProtocol23Format`)
* Sending syslog messages from command line (`<home>/bin/jksys`):
```
$ jksys -h localhost -p 5140 -l error -f user tcp "appl-name[883]: my syslog mesasge about appl-name pid=883"
```
* Sending PCI messages from command line (`<home>/bin/jksys`):
```
$ jksys -h localhost -p 5140 -l error -f user tcp "#pci(userId=john,eventType=audit,status=success,origination=CreditCards,affectedResource=Payment)"
```
* Playback syslog JSON messages from command line (`<home>/bin/jksys`):
```
$ jksys -h localhost -p 5140 -f jksysd.json tcp
```
where `jksysd.json` is JSON output of JESL syslog daemon.

That should do it.

**NOTE:** Currently supports (RFC 3164) and the Structured Syslog protocol (RFC 5424).

# Project Dependencies
* JDK 1.8+
* TNT4J (https://github.com/Nastel/TNT4J)
* Gralog2 Syslog4j (https://github.com/Graylog2/syslog4j-graylog2)
* Joda Time (http://www.joda.org/joda-time/)
* GSON (https://github.com/google/gson)

# Integrations
* jKoolCloud (https://www.jkoolcloud.com)

Please use JCenter or Maven and dependencies will be downloaded automatically.
