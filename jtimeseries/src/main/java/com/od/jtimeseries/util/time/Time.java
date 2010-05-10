/**
 * Copyright (C) 2009 (nick @ objectdefinitions.com)
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
package com.od.jtimeseries.util.time;

/**
 * Created by IntelliJ IDEA.
 * User: Nick Ebbutt
 * Date: 09-Dec-2008
 * Time: 10:39:25
 */
public class Time {

    public static TimePeriod milliseconds(long millis) {
        return new Millisecond(millis);
    }

    public static TimePeriod seconds(long seconds) {
        return new Second(seconds);
    }

    public static TimePeriod minutes(long minutes) {
        return new Minute(minutes);
    }

    public static TimePeriod hours(long hours) {
        return new Hour(hours);
    }

    public static TimePeriod days(long days) {
        return new Day(days);
    }

}
