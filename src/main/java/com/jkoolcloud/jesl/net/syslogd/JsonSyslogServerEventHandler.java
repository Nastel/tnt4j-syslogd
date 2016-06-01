/*    
 *    Copyright (C) 2015, JKOOL LLC.
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package com.jkoolcloud.jesl.net.syslogd;

import java.io.PrintStream;
import java.net.SocketAddress;
import java.util.Date;
import java.util.Map;

import org.apache.commons.lang3.StringEscapeUtils;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.productivity.java.syslog4j.impl.message.structured.StructuredSyslogMessage;
import org.productivity.java.syslog4j.server.SyslogServerEventIF;
import org.productivity.java.syslog4j.server.SyslogServerIF;
import org.productivity.java.syslog4j.server.impl.event.printstream.PrintStreamSyslogServerEventHandler;
import org.productivity.java.syslog4j.server.impl.event.structured.StructuredSyslogServerEvent;
import org.productivity.java.syslog4j.util.SyslogUtility;

/**
 * This class implements simple syslog event handler that outputs
 * formatted syslog messages to a print stream in JSON format.
 * The output contains offset.usec which is time since last event in microseconds.
 * This field can be used to play back events in exact same time sequence. 
 * Event timestamp is formatted in ISO8601 format with UTC timezone.
 *
 * @version $Revision $
 */
class JsonSyslogServerEventHandler extends PrintStreamSyslogServerEventHandler {
    private static final long serialVersionUID = 8964244723777923472L;

    private long lastEvent = 0;
	DateTimeFormatter fmt = ISODateTimeFormat.dateTime().withZoneUTC();
    
	public JsonSyslogServerEventHandler(PrintStream out) {
		super(out);
	}
	
	@Override
	public void event(Object session, SyslogServerIF syslogServer, SocketAddress socketAddress, SyslogServerEventIF event) {
		long now = System.nanoTime();
		long offset = lastEvent == 0? 0: (System.nanoTime() - lastEvent)/1000;
		lastEvent = now;
		
		Date date = (event.getDate() == null ? new Date() : event.getDate());
		String timestamp = fmt.print(date.getTime());
		
		String facility = SyslogTNT4JEventHandler.getFacilityString(event.getFacility());
		String level = SyslogUtility.getLevelString(event.getLevel());
		String host = event.getHost();
		if (!(event instanceof StructuredSyslogServerEvent)) {
			this.stream.println("{\"offset.usec\":" + offset
					+ ", \"host\":\"" + host
					+ "\", \"facility\":\"" + facility
					+ "\", \"timestamp\":\"" + timestamp
					+ "\", \"level\":\"" + level
					+ "\", \"msg\":\"" + StringEscapeUtils.escapeJson(event.getMessage()) 
					+ "\"}");
		} else {
			StructuredSyslogServerEvent sevent = (StructuredSyslogServerEvent) event;
			StructuredSyslogMessage sm = sevent.getStructuredMessage();
			Map<?, ?> arttrs = sm.getStructuredData();
			this.stream.println("{\"offset.usec\":" + offset
					+ ", \"host\":\"" + host
					+ "\", \"facility\":\"" + facility
					+ "\", \"timestamp\":\"" + timestamp
					+ "\", \"level\":\"" + level
					+ "\", \"appl\":\"" + sevent.getApplicationName()
					+ "\", \"mid\":\"" + (sm.getMessageId() != null? sm.getMessageId(): "")
					+ "\", \"pid\":" + (sevent.getProcessId() != null && sevent.getProcessId().isEmpty()? 0: sevent.getProcessId())
					+ ", \"map.size\":" + ((arttrs != null)? arttrs.size(): 0)
					+ ", \"msg\":\"" + StringEscapeUtils.escapeJson(event.getMessage()) 
					+ "\"}");
		}
	}	
}