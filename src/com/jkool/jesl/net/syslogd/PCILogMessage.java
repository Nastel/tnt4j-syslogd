/*
 * Copyright 2015 JKOOL, LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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