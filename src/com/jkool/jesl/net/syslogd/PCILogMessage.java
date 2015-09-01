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

import java.util.Map;

import org.productivity.java.syslog4j.impl.message.pci.PCISyslogMessage;

import com.nastel.jkool.tnt4j.logger.AppenderConstants;

/**
 * This class implements PCI DSS log message which contains
 * elements such as event id, event types, date, time, status,
 * affected resource and origination as per PCI DSS sections
 * 10.3.1-10.3.6.
 *
 * @see SyslogTNT4JEventHandler
 * @version $Revision: 1$
 */
public class PCILogMessage extends PCISyslogMessage implements AppenderConstants {
    private static final long serialVersionUID = -3498112733678615480L;

	public PCILogMessage(Map<String, String> map) {
		super(map);
	}
	
	public String createMessage() {
		StringBuffer buffer = new StringBuffer();
		
		char delimiter = getDelimiter();
		String replaceDelimiter = getReplaceDelimiter();
		
		buffer.append(replaceDelimiter(PARAM_RESOURCE_LABEL,getAffectedResource(),delimiter,replaceDelimiter)).append(":");
		buffer.append(delimiter);
		buffer.append("PCI[");
		buffer.append(DATE).append("=\"").append(replaceDelimiter(DATE,getDate(),delimiter,replaceDelimiter)).append("\"");
		buffer.append(delimiter);
		buffer.append(TIME).append("=\"").append(replaceDelimiter(TIME,getTime(),delimiter,replaceDelimiter)).append("\"");
		buffer.append(delimiter);
		buffer.append(PARAM_OP_NAME_LABEL).append("=\"").append(replaceDelimiter(PARAM_OP_NAME_LABEL,getEventType(),delimiter,replaceDelimiter)).append("\"");
		buffer.append(delimiter);
		buffer.append(PARAM_USER_LABEL).append("=\"").append(replaceDelimiter(PARAM_USER_LABEL,getUserId(),delimiter,replaceDelimiter)).append("\"");
		buffer.append(delimiter);
		buffer.append(PARAM_EXCEPTION_LABEL).append("=\"").append(replaceDelimiter(PARAM_EXCEPTION_LABEL,getStatus(),delimiter,replaceDelimiter)).append("\"");
		buffer.append(delimiter);
		buffer.append(PARAM_LOCATION_LABEL).append("=\"").append(replaceDelimiter(PARAM_LOCATION_LABEL,getOrigination(),delimiter,replaceDelimiter)).append("\"");
		buffer.append("]");
		
		return buffer.toString();
	}
	
}