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
package com.od.jtimeseries.server.servermetrics;

import com.od.jtimeseries.component.managedmetric.AbstractManagedMetric;
import com.od.jtimeseries.context.TimeSeriesContext;
import com.od.jtimeseries.identifiable.Identifiable;
import com.od.jtimeseries.source.Counter;
import com.od.jtimeseries.timeseries.impl.RoundRobinTimeSeries;
import com.od.jtimeseries.util.time.Time;
import com.od.jtimeseries.util.time.TimePeriod;

import static com.od.jtimeseries.capture.function.CaptureFunctions.MEAN_COUNT_OVER;
import static com.od.jtimeseries.capture.function.CaptureFunctions.LATEST;

/**
 * Created by IntelliJ IDEA.
 * User: nick
 * Date: 23-Nov-2009
 * Time: 21:43:01
 * To change this template use File | Settings | File Templates.
 */
public class GarbageCollectedSeriesMetric extends AbstractManagedMetric {

    private TimePeriod countPeriod;
    private static final String id = "SeriesGarbageCollected";
    private String parentContextPath;

    public GarbageCollectedSeriesMetric(String parentContextPath) {
        this(parentContextPath, DEFAULT_TIME_PERIOD_FOR_SERVER_METRICS);
    }

    public GarbageCollectedSeriesMetric(String parentContextPath, TimePeriod countPeriod) {
        this.parentContextPath = parentContextPath;
        this.countPeriod = countPeriod;
    }

   protected String getSeriesPath() {
        return parentContextPath + Identifiable.NAMESPACE_SEPARATOR + id;
    }

    public void doInitializeMetric(TimeSeriesContext rootContext, String path) {
        Counter counter = rootContext.createCounterSeries(
                path,
                "A count of the series deallocated for memory efficiency, we would expect a heavily loaded server to " +
                "regularly deallocate series data once it is no longer possible to maintain all series data in RAM",
                MEAN_COUNT_OVER(Time.seconds(1), countPeriod),
                LATEST(countPeriod));
        RoundRobinTimeSeries.setGarbageCollectionCounter(counter);
    }

}
