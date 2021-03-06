/**
 * Copyright (C) 2011 (nick @ objectdefinitions.com)
 *
 * This file is part of JTimeseries.
 *
 * JTimeseries is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JTimeseries is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with JTimeseries.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.od.jtimeseries.scheduling;

import com.od.jtimeseries.identifiable.Identifiable;
import com.od.jtimeseries.util.time.TimePeriod;

/**
 * Created by IntelliJ IDEA.
 * User: nick
 * Date: 27-Nov-2009
 * Time: 21:49:25
 *
 * Triggerable interface is used to define a task which will be periodically
 * triggered by a jTimeSeries Scheduler
 *
 * Implementations of the  trigger() method must execute and return very quickly,
 * since there are a limited number of Scheduler threads, and a delay might affect
 * the timeliness of scheduling for subsequent tasks.
 *
 * For this reason, any significant work performed during the trigger() callback should
 * be offloaded to a worker thread
 */
public interface Triggerable extends Identifiable {

    TimePeriod getTimePeriod();

    void trigger(long timestamp);
}
