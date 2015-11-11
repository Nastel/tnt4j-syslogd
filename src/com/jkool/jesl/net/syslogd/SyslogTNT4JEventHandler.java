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

package com.jkool.jesl.net.syslogd;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.productivity.java.syslog4j.impl.message.structured.StructuredSyslogMessage;
import org.productivity.java.syslog4j.server.SyslogServerEventIF;
import org.productivity.java.syslog4j.server.SyslogServerIF;
import org.productivity.java.syslog4j.server.SyslogServerSessionEventHandlerIF;
import org.productivity.java.syslog4j.server.impl.event.structured.StructuredSyslogServerEvent;

import com.nastel.jkool.tnt4j.TrackingLogger;
import com.nastel.jkool.tnt4j.core.OpLevel;
import com.nastel.jkool.tnt4j.core.OpType;
import com.nastel.jkool.tnt4j.core.PropertySnapshot;
import com.nastel.jkool.tnt4j.dump.SimpleDumpListener;
import com.nastel.jkool.tnt4j.dump.TimeTrackerDumpProvider;
import com.nastel.jkool.tnt4j.logger.AppenderConstants;
import com.nastel.jkool.tnt4j.source.Source;
import com.nastel.jkool.tnt4j.source.SourceFactory;
import com.nastel.jkool.tnt4j.source.SourceType;
import com.nastel.jkool.tnt4j.tracker.TimeTracker;
import com.nastel.jkool.tnt4j.tracker.TrackingEvent;

/**
 * This class implements syslog event handler that outputs syslog
 * messages to TNT4J logging framework. 
 * <p>Syslog messages are mapped to TNT4J event structure as follows:</p>
 * <table>
 * <tr><td><b>timestamp</b></td>		<td>stop/time</td></tr>
 * <tr><td><b>level</b></td>			<td>severity</td></tr>
 * <tr><td><b>applname/facility</b></td><td>event/operation name</td></tr>
 * <tr><td><b>host</b></td>				<td>location</td></tr>
 * <tr><td><b>applname</b></td>			<td>resource name</td></tr>
 * <tr><td><b>pid</b></td>				<td>process ID & thread ID</td></tr>
 * <tr><td><b>message</b></td>			<td>message</td></tr>
 * <tr><td><b>RFC5424 map</b></td>		<td>SyslogMap snapshot</td></tr>
 * <tr><td><b>name=value pairs</b></td>	<td>SyslogVars snapshot</td></tr>
 * </table>
 * 
 * Event elapsed time is computed based on time since last event from the same
 * source (source is host/application combo).
 * 
 * <p>The following (name=value) pairs have special meaning and mapped to TNT4J tracking events when included in syslog message:</p>
 * <table>
 * <tr><td><b>usr</b></td>				<td>User name</td></tr>
 * <tr><td><b>cid</b></td>				<td>Correlator for relating events across threads, applications, servers</td></tr>
 * <tr><td><b>tag</b></td>				<td>User defined tag</td></tr>
 * <tr><td><b>loc</b></td>				<td>Location specifier</td></tr>
 * <tr><td><b>exc</b></td>				<td>Exception string</td></tr>
 * <tr><td><b>opn</b></td>			    <td>Event/Operation name</td></tr>
 * <tr><td><b>opt</b></td>			    <td>Event/Operation Type - Value must be either a member of {@link OpType} or the equivalent numeric value</td></tr>
 * <tr><td><b>rsn</b></td>				<td>Resource name on which operation/event took place</td></tr>
 * </table>
 * 
 * 
 * @version $Revision: 1$
 */
public class SyslogTNT4JEventHandler implements SyslogServerSessionEventHandlerIF, SyslogConstants {
    private static final long serialVersionUID = -3115399425996955812L;

    protected static String SNAPSHOT_CAT_SYSLOG_MAP = "SyslogMap";
    protected static String SNAPSHOT_CAT_SYSLOG_VARS = "SyslogVars";
    

    /*
     * Timing map maintains the number of nanoseconds since last event for a specific server/application
     * combo.
     */
 	private static final TimeTracker timeTracker;

 	static {
		// add a custom dump provider
		timeTracker = TimeTracker.newTracker(10000, TimeUnit.DAYS.toMillis(30));
		TrackingLogger.addDumpProvider(new TimeTrackerDumpProvider(SyslogTNT4JEventHandler.class.getName(), timeTracker));		
		TrackingLogger.addDumpListener(new SimpleDumpListener(System.out));
	}
	
    /*
     * Tracking logger instance where all syslog messages are recorded.
     */
    private TrackingLogger logger;
    
    /*
     * Regular expression pattern to detect name=value pairs.
     */
	private Pattern pattern = Pattern.compile("(\\w+)=\"*((?<=\")[^\"]+(?=\")|([^\\s]+))\"*");
	
	public SyslogTNT4JEventHandler(String source) {
		logger = TrackingLogger.getInstance(source);
	}
	
	@Override
    public void destroy(SyslogServerIF arg0) {
		logger.close();
    }

	@Override
    public void initialize(SyslogServerIF arg0) {
		try {
	        logger.open();
        } catch (IOException e) {
        	throw new RuntimeException(e);
        }
	}

	@Override
    public void event(Object arg0, SyslogServerIF config, SocketAddress arg2, SyslogServerEventIF event) {
		Date date = (event.getDate() == null ? new Date() : event.getDate());
		String facility = getFacilityString(event.getFacility());
		OpLevel level = getOpLevel(event.getLevel());	

		TrackingEvent tevent = logger.newEvent(facility, event.getMessage());
		tevent.getOperation().setSeverity(level);
		tevent.setLocation(event.getHost());
		tevent.setCharset(config.getConfig().getCharSet());
		
		if (event instanceof StructuredSyslogServerEvent) {
			processRFC5424(facility, (StructuredSyslogServerEvent)event, tevent);
		} else {
			processRFC3164(facility, event, tevent);
		}
		
		// extract name=value pairs if available
		extractVariables(event, tevent);
		String locationKey = tevent.getLocation() + "/" + tevent.getOperation().getResource();
		tevent.stop(date.getTime()*1000, getUsecSinceLastEvent(locationKey));
		logger.tnt(tevent);
	}

	/**
	 * Process syslog message based on RFC5424
	 *
	 * @param facility syslog facility name
	 * @param event syslog message
	 * @param tevent tracking event
	 * 
	 * @return tracking event
	 */
	protected TrackingEvent processRFC3164(String facility, SyslogServerEventIF event, TrackingEvent tevent) {
		Map<String, Object> map = parseAttributes(event);
		String appName = map.get("appl.name").toString();
		String serverName = map.get("server.name").toString();
		Long pid = (Long) map.get("appl.pid");
		
		tevent.setTag(serverName, appName);
		tevent.getOperation().setPID(pid);
		tevent.getOperation().setTID(pid);
		tevent.getOperation().setResource(appName);
		tevent.getOperation().setName(appName + "/" + facility);

		// set the appropriate source
		SourceFactory factory = logger.getConfiguration().getSourceFactory();
		Source rootSource = factory.getRootSource().getSource(SourceType.DATACENTER);
		tevent.setSource(factory.newSource(appName, 
				SourceType.APPL, 
				factory.newSource(serverName, SourceType.SERVER, rootSource)));						
		return tevent;
	}
	
	/**
	 * Process syslog message based on RFC5424
	 *
	 * @param facility syslog facility name
	 * @param sevent syslog structured message
	 * @param tevent tracking event
	 * 
	 * @return tracking event
	 */
	protected TrackingEvent processRFC5424(String facility, StructuredSyslogServerEvent sevent, TrackingEvent tevent) {
		// RFC 5424 
		tevent.getOperation().setResource(sevent.getApplicationName());
		tevent.getOperation().setName(sevent.getApplicationName() + "/" + facility);
		tevent.setTag(sevent.getHost(), sevent.getApplicationName(), sevent.getStructuredMessage().getMessageId());
		assignPid(sevent, tevent);			
		
		// set the appropriate source
		SourceFactory factory = logger.getConfiguration().getSourceFactory();
		Source rootSource = factory.getRootSource().getSource(SourceType.DATACENTER);
		tevent.setSource(factory.newSource(sevent.getApplicationName(), 
				SourceType.APPL, 
				factory.newSource(sevent.getHost(), SourceType.SERVER, rootSource)));
		
		// process structured event attributes into snapshot
		extractStructuredData(sevent, tevent);	
		return tevent;
	}
	
	/**
	 * Process a given map of key/value pairs into a TNT4J event object {@link TrackingEvent}.
	 *
	 * @param attrs a set of name/value pairs
	 * @param tevent tracking event
	 *
	 * @return tnt4j tracking event object
	 */
	private TrackingEvent extractSpecialKeys(Map<String, Object> attrs, TrackingEvent tevent) {
		for (Map.Entry<String, Object> entry: attrs.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue().toString();
			if (key.equals(AppenderConstants.PARAM_CORRELATOR_LABEL)) {
				tevent.setCorrelator(value);
			} else if (key.equals(AppenderConstants.PARAM_TAG_LABEL)) {
				tevent.setTag(value);
			} else if (key.equals(AppenderConstants.PARAM_LOCATION_LABEL)) {
				tevent.setLocation(value);
			} else if (key.equals(AppenderConstants.PARAM_RESOURCE_LABEL)) {
				tevent.getOperation().setResource(value);
			} else if (key.equals(AppenderConstants.PARAM_USER_LABEL)) {
				tevent.getOperation().setUser(value);
			} else if (key.equals(AppenderConstants.PARAM_OP_TYPE_LABEL)) {
				tevent.getOperation().setType(OpType.valueOf(value));
			} else if (key.equals(AppenderConstants.PARAM_OP_NAME_LABEL)) {
				tevent.getOperation().setName(value);
			} else if (key.equals(AppenderConstants.PARAM_EXCEPTION_LABEL)) {
				tevent.getOperation().setException(value);
			} 
		}
		return tevent;
	}

	/**
	 * Extract syslog name/value pairs if available in within the message
	 *
	 * @param event syslog event message
	 * @param tevent tracking event
	 * @return map of parsed out event attributes (name=value pairs)
	 */
	protected Map<String, Object> extractVariables(SyslogServerEventIF event, TrackingEvent tevent) {
		Map<String, Object> attr = parseVariables(event.getMessage());
		if (attr != null && attr.size() > 0) {
			extractSpecialKeys(attr, tevent);
			PropertySnapshot snap = new PropertySnapshot(SNAPSHOT_CAT_SYSLOG_VARS, tevent.getOperation().getResource(), tevent.getSeverity());
			snap.addAll(attr);
			tevent.getOperation().addSnapshot(snap);			
		}	
		return attr;
	}
	
	/**
	 * Extract syslog structured data if available (part of RFC 5424)
	 *
	 * @param sevent syslog structured message
	 * @param tevent tracking event
	 */
	protected void extractStructuredData(StructuredSyslogServerEvent sevent, TrackingEvent tevent) {
		StructuredSyslogMessage sm = sevent.getStructuredMessage();
		Map<?, ?> map = sm.getStructuredData();
		if (map != null && !map.isEmpty()) {
			PropertySnapshot snap = new PropertySnapshot(SNAPSHOT_CAT_SYSLOG_MAP, sevent.getApplicationName(), tevent.getSeverity());
			snap.addAll(map);
			tevent.getOperation().addSnapshot(snap);
		}		
	}
	
	/**
	 * Extract and assign process id
	 *
	 * @param sevent syslog structured message
	 * @param tevent tracking event
	 */
	protected void assignPid(StructuredSyslogServerEvent sevent, TrackingEvent tevent) {
		tevent.getOperation().setPID(0);
		String pid = sevent.getProcessId();
		if (pid != null && !pid.isEmpty()) {
			try {
				tevent.getOperation().setPID(Long.parseLong(sevent.getProcessId()));	
			} catch (NumberFormatException e) {}
		}
		tevent.getOperation().setTID(tevent.getOperation().getPID());		
	}
	
	/**
	 * Parse syslog name=value variables
	 *
	 * @param message syslog message
	 * @return syslog name=value variables
	 */
	protected Map<String, Object> parseVariables(String message) {
		Map<String, Object> map = new HashMap<String, Object>();
		StringTokenizer tokens = new StringTokenizer(message, "[](){}");

		while (tokens.hasMoreTokens()) {
			String pair = tokens.nextToken();
			Matcher matcher = pattern.matcher(pair);
			while (matcher.find()) {
				mapToTyped(map, matcher.group(1), matcher.group(2));
			}
		}
		return map;
	}

	/**
	 * Test key value pair for numeric, convert and store in map
	 *
	 * @param map collection of name, value pairs
	 * @param key associated with key, value pair
	 * @param value associated with key, value pair
	 * 
	 */
	private void mapToTyped(Map<String, Object> map, String key, String value) {
		try {
			if (Character.isDigit(value.charAt(0))) {
				map.put(key, Long.valueOf(value));
				return;
			}
		} catch (Throwable e) {
			try {
				map.put(key, Double.valueOf(value));
				return;
			} catch (Throwable ex) {
			}
		}
		map.put(key, value);
	}
	
	/**
	 * Parse syslog header attributes into a map.
	 * Message structure: <server> <appl-part>:<message>
	 *
	 * @param event syslog event
	 * @return syslog attributes such as host, application, pid
	 */
	private Map<String, Object> parseAttributes(SyslogServerEventIF event) {
		HashMap<String, Object> map = new HashMap<String, Object>();
		String message = event.getMessage();
		
		if (message.indexOf(":") > 0) {
			String[] tokens = message.split(":| ");
			map.put("server.name", tokens[0]);
			map.put("appl.part", tokens[1]);

			try {
				int first = tokens[1].indexOf("[");
				int last = tokens[1].indexOf("]");
				String applName = first >= 0 ? tokens[1].substring(0, first) : tokens[1];
				long pid = ((last >= first && (first >= 0)) ? Long.parseLong(tokens[1].substring(first + 1, last)) : 0);
				map.put("appl.pid", pid);
				map.put("appl.name", applName);
			} catch (Throwable ex) {
				map.put("appl.pid", 0L);
				map.put("appl.name", tokens[1]);
			}
		} else {
			map.put("server.name", event.getHost());
			map.put("appl.pid", 0L);
			map.put("appl.name", "unknown");
		}
		return map;
	}
	
	/**
	 * Obtain string representation of syslog facility
	 *
	 * @param facility syslog facility
	 * @return string representation of syslog facility
	 */
	public static String getFacilityString(int facility) {
	    return ((facility >= 0) && (facility < FACILITY.length))? FACILITY[facility]: FACILITY[FACILITY.length-1];
    }

	/**
	 * Obtain syslog level to {@link OpLevel} mapping
	 *
	 * @param level syslog level
	 * @return {@link OpLevel} mapping
	 */
	public static OpLevel getOpLevel(int level) {
	    return ((level >= 0) && (level < LEVELS.length))? LEVELS[level]: LEVELS[LEVELS.length-1];
    }

	/**
	 * Obtain elapsed microseconds since last event
	 * 
	 * @param key timer key
	 * @return elapsed microseconds since last event
	 */
	protected long getUsecSinceLastEvent(String key) {
		return TimeUnit.NANOSECONDS.toMicros(timeTracker.hitAndGet(key));
	}

	@Override
    public void exception(Object arg0, SyslogServerIF arg1, SocketAddress arg2, Exception arg3) {
		logger.log(OpLevel.ERROR, "Syslog exception: obj={0}, syslog.server.if={1}, socket={2}", arg0, arg1, arg2, arg3);
		if (arg3 != null) {
			arg3.printStackTrace();
		}
	}

	@Override
    public void sessionClosed(Object arg0, SyslogServerIF arg1, SocketAddress arg2, boolean arg3) {
		logger.log(OpLevel.DEBUG, "Session closed: obj={0}, syslog.server.if={1}, socket={2}, timeout={3}", arg0, arg1, arg2, arg3);
    }

	@Override
    public Object sessionOpened(SyslogServerIF arg0, SocketAddress arg1) {
		logger.log(OpLevel.DEBUG, "Session opened: syslog.server.if={0}, socket={1}", arg0, arg1);
		return null;
    }
}
