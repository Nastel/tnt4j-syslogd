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

import java.util.concurrent.atomic.AtomicLong;

/**
 * This class maintains syslog target timing and performance
 * statistics.
 *
 * @see SyslogTNT4JEventHandler
 * @version $Revision: 1$
 */
class SyslogStats {
	AtomicLong nanoStamp = new AtomicLong(0);
	AtomicLong hitCount = new AtomicLong(0);
	
	SyslogStats() {
		nanoStamp.set(System.nanoTime());
	}
	
	SyslogStats(long nanotime) {
		nanoStamp.set(nanotime);
	}

	long getAgeNanos() {
		return (System.nanoTime() - nanoStamp.get());
	}
	
	long getAgeNanos(long nanos) {
		return (nanos - nanoStamp.get());
	}
	
	long getNanoTime() {
		return nanoStamp.get();
	}
	
	boolean updateNanoTime(long expected, long update) {
		return nanoStamp.compareAndSet(expected, update);		
	}
	
	long hit() {
		return hitCount.addAndGet(+1);
	}
	
	long hit(long count) {
		return hitCount.addAndGet(count);
	}
	
	long getHits() {
		return hitCount.get();
	}
}