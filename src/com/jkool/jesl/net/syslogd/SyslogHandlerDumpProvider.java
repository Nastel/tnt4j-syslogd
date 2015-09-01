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
import java.util.Map.Entry;

import com.nastel.jkool.tnt4j.dump.DefaultDumpProvider;
import com.nastel.jkool.tnt4j.dump.Dump;
import com.nastel.jkool.tnt4j.dump.DumpCollection;

/**
 * This class implements a dump handler for syslog event handler {@link SyslogTNT4JEventHandler}.
 * It dumps the contents of a timing table maintained by the syslog handler. 
 * The timings maintain the number of nanoseconds since last event for a specific server/application
 * combo.
 *
 * @version $Revision: 1$
 */
public class SyslogHandlerDumpProvider extends DefaultDumpProvider{
	private Map<String, SyslogStats> map;
	
	public SyslogHandlerDumpProvider(String name, Map<String, SyslogStats> m) {
	    super(name, "SyslogTimings");
	    this.map = m;
    }

	@Override
    public DumpCollection getDump() {
		Dump dump = new Dump("TimerTable", this);	
		for (Entry<String, SyslogStats> entry: map.entrySet()) {
			dump.add(entry.getKey() + "/hits", entry.getValue().getHits());
			dump.add(entry.getKey() + "/age.nano", entry.getValue().getAgeNanos());
		}
	    return dump;
    }
}
