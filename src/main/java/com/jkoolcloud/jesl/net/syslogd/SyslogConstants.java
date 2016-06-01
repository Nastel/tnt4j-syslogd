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

import com.jkoolcloud.tnt4j.core.OpLevel;

/**
 * This interface defines basic syslog constants and mappings.
 *
 * @version $Revision: 1$
 */
public interface SyslogConstants {
	static final int FACILITY_KERN = 0;
	static final int FACILITY_USER = 1;
	static final int FACILITY_MAIL = 2;
	static final int FACILITY_DAEMON = 3;
	static final int FACILITY_AUTH = 4;
	static final int FACILITY_SYSLOG = 5;
	static final int FACILITY_LPR = 6;
	static final int FACILITY_NEWS = 7;
	static final int FACILITY_UUCP = 8;
	static final int FACILITY_CRON = 9;
	static final int FACILITY_AUTHPRIV = 10;
	static final int FACILITY_FTP = 11;
	static final int FACILITY_NTP = 12;
	static final int FACILITY_AUDIT = 13;
	static final int FACILITY_ALERT = 14;
	static final int FACILITY_CLOCK = 15;
	static final int FACILITY_LOCAL0 = 16;
	static final int FACILITY_LOCAL1 = 17;
	static final int FACILITY_LOCAL2 = 18;
	static final int FACILITY_LOCAL3 = 19;
	static final int FACILITY_LOCAL4 = 20;
	static final int FACILITY_LOCAL5 = 21;
	static final int FACILITY_LOCAL6 = 22;
	static final int FACILITY_LOCAL7 = 23;

	static final String[] FACILITY = { "kern", "user", "mail", "daemon", "auth", "syslog", "lpr", "news", "uucp",
	        "cron", "authpriv", "ftp", "ntp", "logaudit", "logalert", "clock", "local0", "local1", "local2", "local3",
	        "local4", "local5", "local6", "local7", "unknown" };

	static final OpLevel[] LEVELS = { OpLevel.HALT, OpLevel.FATAL, OpLevel.CRITICAL, OpLevel.ERROR, OpLevel.WARNING,
	        OpLevel.WARNING, OpLevel.INFO, OpLevel.DEBUG, OpLevel.NONE };
}
