package com.od.jtimeseries.context.impl;

import com.od.jtimeseries.capture.Capture;
import com.od.jtimeseries.capture.CaptureFactory;
import com.od.jtimeseries.capture.TimedCapture;
import com.od.jtimeseries.capture.function.CaptureFunction;
import com.od.jtimeseries.context.ContextFactory;
import com.od.jtimeseries.context.TimeSeriesContext;
import com.od.jtimeseries.scheduling.Scheduler;
import com.od.jtimeseries.scheduling.Triggerable;
import com.od.jtimeseries.source.*;
import com.od.jtimeseries.timeseries.IdentifiableTimeSeries;
import com.od.jtimeseries.timeseries.TimeSeriesFactory;
import com.od.jtimeseries.util.identifiable.Identifiable;
import com.od.jtimeseries.util.identifiable.IdentifiableBase;
import com.od.jtimeseries.util.time.TimePeriod;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: nick
 * Date: 02-Jun-2010
 * Time: 08:45:19
 *
 * Lock all the interface methods here
 *
 * Really, I suspect this is a job for aspectj, but for the time being, this keeps the boilerplate out of the main
 * implementation, at least
 */
public abstract class LockingTimeSeriesContext extends IdentifiableBase implements TimeSeriesContext {

    public LockingTimeSeriesContext(Identifiable parent, String id, String description) {
        super(parent, id, description);
    }

    public LockingTimeSeriesContext(String id, String description) {
        super(id, description);
    }

    public TimeSeriesContext getRoot() {
        try {
            getContextLock().readLock().lock();
            return isRoot() ? this : getParent().getRoot();
        } finally {
            getContextLock().readLock().unlock();
        }
    }

    public TimeSeriesContext getParent() {
        return (TimeSeriesContext) super.getParent();
    }

    public final List<ValueSource> getSources() {
        try {
            getContextLock().readLock().lock();
            return getSources_Locked();
        } finally {
            getContextLock().readLock().unlock();
        }
    }

    protected abstract List<ValueSource> getSources_Locked();

    public final List<Capture> getCaptures() {
        try {
            getContextLock().readLock().lock();
            return getCaptures_Locked();
        } finally {
            getContextLock().readLock().unlock();
        }
    }

    protected abstract List<Capture> getCaptures_Locked();


    public final List<TimeSeriesContext> getChildContexts() {
        try {
            getContextLock().readLock().lock();
            return getChildContexts_Locked();
        } finally {
            getContextLock().readLock().unlock();
        }
    }

    protected abstract List<TimeSeriesContext> getChildContexts_Locked();


    public final List<IdentifiableTimeSeries> getTimeSeries() {
        try {
            getContextLock().readLock().lock();
            return getTimeSeries_Locked();
        } finally {
            getContextLock().readLock().unlock();
        }
    }

    protected abstract List<IdentifiableTimeSeries> getTimeSeries_Locked();

    public final TimeSeriesContext addChild(Identifiable... identifiables) {
        try {
            getContextLock().writeLock().lock();
            return addChild_Locked(identifiables);
        } finally {
            getContextLock().writeLock().unlock();
        }
    }

    protected abstract TimeSeriesContext addChild_Locked(Identifiable... identifiables);

    public final IdentifiableTimeSeries getTimeSeries(String id) {
        try {
            getContextLock().readLock().lock();
            return getTimeSeries_Locked(id);
        } finally {
            getContextLock().readLock().unlock();
        }
    }


    protected abstract IdentifiableTimeSeries getTimeSeries_Locked(String id);

    public final ValueSource getSource(String id) {
        try {
            getContextLock().readLock().lock();
            return getSource_Locked(id);
        } finally {
            getContextLock().readLock().unlock();
        }
    }

    protected abstract ValueSource getSource_Locked(String id);

    public final TimeSeriesContext getChildContext(String id) {
        try {
            getContextLock().readLock().lock();
            return getChildContext_Locked(id);
        } finally {
            getContextLock().readLock().unlock();
        }
    }

    protected abstract TimeSeriesContext getChildContext_Locked(String id);

    public final Capture getCapture(String id) {
        try {
            getContextLock().readLock().lock();
            return getCapture_Locked(id);
        } finally {
            getContextLock().readLock().unlock();
        }
    }

    protected abstract Capture getCapture_Locked(String id);

    public final Scheduler getScheduler() {
        try {
            getContextLock().readLock().lock();
            return getScheduler_Locked();
        } finally {
            getContextLock().readLock().unlock();
        }
    }

    protected abstract Scheduler getScheduler_Locked();

    public final TimeSeriesContext setScheduler(Scheduler scheduler) {
        try {
            getContextLock().writeLock().lock();
            return setScheduler_Locked(scheduler);
        } finally {
            getContextLock().writeLock().unlock();
        }
    }

    protected abstract TimeSeriesContext setScheduler_Locked(Scheduler scheduler);

    public final boolean isSchedulerStarted() {
        try {
            getContextLock().readLock().lock();
            return isSchedulerStarted_Locked();
        } finally {
            getContextLock().readLock().unlock();
        }
    }


    protected abstract boolean isSchedulerStarted_Locked();

    public final TimeSeriesContext startScheduling() {
        try {
            getContextLock().writeLock().lock();
            return startScheduling_Locked();
        } finally {
            getContextLock().writeLock().unlock();
        }
    }


    protected abstract TimeSeriesContext startScheduling_Locked();

    public final TimeSeriesContext stopScheduling() {
        try {
            getContextLock().writeLock().lock();
            return stopScheduling_Locked();
        } finally {
            getContextLock().writeLock().unlock();
        }
    }


    protected abstract TimeSeriesContext stopScheduling_Locked();

    public final TimeSeriesContext startDataCapture() {
        try {
            getContextLock().writeLock().lock();
            return startDataCapture_Locked();
        } finally {
            getContextLock().writeLock().unlock();
        }
    }


    protected abstract TimeSeriesContext startDataCapture_Locked();

    public final TimeSeriesContext stopDataCapture() {
        try {
            getContextLock().writeLock().lock();
            return stopDataCapture_Locked();
        } finally {
            getContextLock().writeLock().unlock();
        }
    }

    protected abstract TimeSeriesContext stopDataCapture_Locked();

    public final TimeSeriesContext setValueSourceFactory(ValueSourceFactory sourceFactory) {
        try {
            getContextLock().writeLock().lock();
            return setValueSourceFactory_Locked(sourceFactory);
        } finally {
            getContextLock().writeLock().unlock();
        }
    }


    protected abstract TimeSeriesContext setValueSourceFactory_Locked(ValueSourceFactory sourceFactory);

    public final ValueSourceFactory getValueSourceFactory() {
        try {
            getContextLock().readLock().lock();
            return getValueSourceFactory_Locked();
        } finally {
            getContextLock().readLock().unlock();
        }
    }


    protected abstract ValueSourceFactory getValueSourceFactory_Locked();

    public final TimeSeriesContext setTimeSeriesFactory(TimeSeriesFactory seriesFactory) {
        try {
            getContextLock().writeLock().lock();
            return setTimeSeriesFactory_Locked(seriesFactory);
        } finally {
            getContextLock().writeLock().unlock();
        }
    }

    protected abstract TimeSeriesContext setTimeSeriesFactory_Locked(TimeSeriesFactory seriesFactory);

    public final TimeSeriesFactory getTimeSeriesFactory() {
        try {
            getContextLock().readLock().lock();
            return getTimeSeriesFactory_Locked();
        } finally {
            getContextLock().readLock().unlock();
        }
    }

    protected abstract TimeSeriesFactory getTimeSeriesFactory_Locked();

    public final TimeSeriesContext setCaptureFactory(CaptureFactory captureFactory) {
        try {
            getContextLock().writeLock().lock();
            return setCaptureFactory_Locked(captureFactory);
        } finally {
            getContextLock().writeLock().unlock();
        }
    }

    protected abstract TimeSeriesContext setCaptureFactory_Locked(CaptureFactory captureFactory);

    public final CaptureFactory getCaptureFactory() {
        try {
            getContextLock().readLock().lock();
            return getCaptureFactory_Locked();
        } finally {
            getContextLock().readLock().unlock();
        }
    }

    protected abstract CaptureFactory getCaptureFactory_Locked();

    public final TimeSeriesContext setContextFactory(ContextFactory contextFactory) {
        try {
            getContextLock().writeLock().lock();
            return setContextFactory_Locked(contextFactory);
        } finally {
            getContextLock().writeLock().unlock();
        }
    }

    protected abstract TimeSeriesContext setContextFactory_Locked(ContextFactory contextFactory);

    public final ContextFactory getContextFactory() {
        try {
            getContextLock().readLock().lock();
            return getContextFactory_Locked();
        } finally {
            getContextLock().readLock().unlock();
        }
    }

    protected abstract ContextFactory getContextFactory_Locked();

    public final TimeSeriesContext createContextForPath(String path) {
        try {
            getContextLock().writeLock().lock();
            return createContextForPath_Locked(path);
        } finally {
            getContextLock().writeLock().unlock();
        }
    }

    protected abstract TimeSeriesContext createContextForPath_Locked(String path);

    public final IdentifiableTimeSeries createTimeSeriesForPath(String path, String description) {
        try {
            getContextLock().writeLock().lock();
            return createTimeSeriesForPath_Locked(path, description);
        } finally {
            getContextLock().writeLock().unlock();
        }
    }

    protected abstract IdentifiableTimeSeries createTimeSeriesForPath_Locked(String path, String description);

    public final TimeSeriesContext createChildContext(String id) {
        try {
            getContextLock().writeLock().lock();
            return createChildContext_Locked(id);
        } finally {
            getContextLock().writeLock().unlock();
        }
    }

    protected abstract TimeSeriesContext createChildContext_Locked(String id);

    public final TimeSeriesContext createChildContext(String id, String description) {
        try {
            getContextLock().writeLock().lock();
            return createChildContext_Locked(id, description);
        } finally {
            getContextLock().writeLock().unlock();
        }
    }

    protected abstract TimeSeriesContext createChildContext_Locked(String id, String description);

    public final IdentifiableTimeSeries createTimeSeries(String id, String description) {
        try {
            getContextLock().writeLock().lock();
            return createTimeSeries_Locked(id, description);
        } finally {
            getContextLock().writeLock().unlock();
        }
    }

    protected abstract IdentifiableTimeSeries createTimeSeries_Locked(String id, String description);

    public final Capture createCapture(String id, ValueSource source, IdentifiableTimeSeries series) {
        try {
            getContextLock().writeLock().lock();
            return createCapture_Locked(id, source, series);
        } finally {
            getContextLock().writeLock().unlock();
        }
    }

    protected abstract Capture createCapture_Locked(String id, ValueSource source, IdentifiableTimeSeries series);

    public final TimedCapture createTimedCapture(String id, ValueSource source, IdentifiableTimeSeries series, CaptureFunction captureFunction) {
        try {
            getContextLock().writeLock().lock();
            return createTimedCapture_Locked(id, source, series, captureFunction);
        } finally {
            getContextLock().writeLock().unlock();
        }
    }

    protected abstract TimedCapture createTimedCapture_Locked(String id, ValueSource source, IdentifiableTimeSeries series, CaptureFunction captureFunction);

    public final ValueRecorder createValueRecorder(String id, String description) {
        try {
            getContextLock().writeLock().lock();
            return createValueRecorder_Locked(id, description);
        } finally {
            getContextLock().writeLock().unlock();
        }
    }

    protected abstract ValueRecorder createValueRecorder_Locked(String id, String description);

    public final QueueTimer createQueueTimer(String id, String description) {
        try {
            getContextLock().writeLock().lock();
            return createQueueTimer_Locked(id, description);
        } finally {
            getContextLock().writeLock().unlock();
        }
    }

    protected abstract QueueTimer createQueueTimer_Locked(String id, String description);

    public final Counter createCounter(String id, String description) {
        try {
            getContextLock().writeLock().lock();
            return createCounter_Locked(id, description);
        } finally {
            getContextLock().writeLock().unlock();
        }
    }

    protected abstract Counter createCounter_Locked(String id, String description);

    public final EventTimer createEventTimer(String id, String description) {
        try {
            getContextLock().writeLock().lock();
            return createEventTimer_Locked(id, description);
        } finally {
            getContextLock().writeLock().unlock();
        }
    }

    protected abstract EventTimer createEventTimer_Locked(String id, String description);

    public final TimedValueSource createTimedValueSource(String id, String description, ValueSupplier valueSupplier, TimePeriod timePeriod) {
        try {
            getContextLock().writeLock().lock();
            return createTimedValueSource_Locked(id, description, valueSupplier, timePeriod);
        } finally {
            getContextLock().writeLock().unlock();
        }
    }

    protected abstract TimedValueSource createTimedValueSource_Locked(String id, String description, ValueSupplier valueSupplier, TimePeriod timePeriod);

    public final ValueRecorder createValueRecorderSeries(String id, String description, CaptureFunction... captureFunctions) {
        try {
            getContextLock().writeLock().lock();
            return createValueRecorderSeries_Locked(id, description, captureFunctions);
        } finally {
            getContextLock().writeLock().unlock();
        }
    }

    protected abstract ValueRecorder createValueRecorderSeries_Locked(String id, String description, CaptureFunction... captureFunctions);

    public final QueueTimer createQueueTimerSeries(String id, String description, CaptureFunction... captureFunctions) {
        try {
            getContextLock().writeLock().lock();
            return createQueueTimerSeries_Locked(id, description, captureFunctions);
        } finally {
            getContextLock().writeLock().unlock();
        }
    }

    protected abstract QueueTimer createQueueTimerSeries_Locked(String id, String description, CaptureFunction... captureFunctions);


    public final Counter createCounterSeries(String id, String description, CaptureFunction... captureFunctions) {
        try {
            getContextLock().writeLock().lock();
            return createCounterSeries_Locked(id, description, captureFunctions);
        } finally {
            getContextLock().writeLock().unlock();
        }
    }

    protected abstract Counter createCounterSeries_Locked(String id, String description, CaptureFunction... captureFunctions);

    public final EventTimer createEventTimerSeries(String id, String description, CaptureFunction... captureFunctions) {
        try {
            getContextLock().writeLock().lock();
            return createEventTimerSeries_Locked(id, description, captureFunctions);
        } finally {
            getContextLock().writeLock().unlock();
        }
    }

    protected abstract EventTimer createEventTimerSeries_Locked(String id, String description, CaptureFunction... captureFunctions);

    public final TimedValueSource createValueSupplierSeries(String id, String description, ValueSupplier valueSupplier, TimePeriod timePeriod) {
        try {
            getContextLock().writeLock().lock();
            return createValueSupplierSeries_Locked(id, description, valueSupplier, timePeriod);
        } finally {
            getContextLock().writeLock().unlock();
        }
    }

    protected abstract TimedValueSource createValueSupplierSeries_Locked(String id, String description, ValueSupplier valueSupplier, TimePeriod timePeriod);

    public final QueryResult<IdentifiableTimeSeries> findTimeSeries(CaptureCriteria criteria) {
        try {
            getContextLock().readLock().lock();
            return findTimeSeries_Locked(criteria);
        } finally {
            getContextLock().readLock().unlock();
        }
    }

    protected abstract QueryResult<IdentifiableTimeSeries> findTimeSeries_Locked(CaptureCriteria criteria);

    public final QueryResult<IdentifiableTimeSeries> findTimeSeries(ValueSource source) {
        try {
            getContextLock().readLock().lock();
            return findTimeSeries_Locked(source);
        } finally {
            getContextLock().readLock().unlock();
        }
    }

    protected abstract QueryResult<IdentifiableTimeSeries> findTimeSeries_Locked(ValueSource source);

    public final QueryResult<IdentifiableTimeSeries> findTimeSeries(String searchPattern) {
        try {
            getContextLock().readLock().lock();
            return findTimeSeries_Locked(searchPattern);
        } finally {
            getContextLock().readLock().unlock();
        }
    }

    protected abstract QueryResult<IdentifiableTimeSeries> findTimeSeries_Locked(String searchPattern);

    public final QueryResult<IdentifiableTimeSeries> findAllTimeSeries() {
        try {
            getContextLock().readLock().lock();
            return findAllTimeSeries_Locked();
        } finally {
            getContextLock().readLock().unlock();
        }
    }

    protected abstract QueryResult<IdentifiableTimeSeries> findAllTimeSeries_Locked();

    public final QueryResult<Capture> findCaptures(String searchPattern) {
        try {
            getContextLock().readLock().lock();
            return findCaptures_Locked(searchPattern);
        } finally {
            getContextLock().readLock().unlock();
        }
    }

    protected abstract QueryResult<Capture> findCaptures_Locked(String searchPattern);

    public final QueryResult<Capture> findCaptures(CaptureCriteria criteria) {
        try {
            getContextLock().readLock().lock();
            return findCaptures_Locked(criteria);
        } finally {
            getContextLock().readLock().unlock();
        }
    }

    protected abstract QueryResult<Capture> findCaptures_Locked(CaptureCriteria criteria);

    public final QueryResult<Capture> findCaptures(ValueSource valueSource) {
        try {
            getContextLock().readLock().lock();
            return findCaptures_Locked(valueSource);
        } finally {
            getContextLock().readLock().unlock();
        }
    }

    protected abstract QueryResult<Capture> findCaptures_Locked(ValueSource valueSource);

    public final QueryResult<Capture> findCaptures(IdentifiableTimeSeries timeSeries) {
        try {
            getContextLock().readLock().lock();
            return findCaptures_Locked(timeSeries);
        } finally {
            getContextLock().readLock().unlock();
        }
    }

    protected abstract QueryResult<Capture> findCaptures_Locked(IdentifiableTimeSeries timeSeries);

    public final QueryResult<Capture> findAllCaptures() {
        try {
            getContextLock().readLock().lock();
            return findAllCaptures_Locked();
        } finally {
            getContextLock().readLock().unlock();
        }
    }

    protected abstract QueryResult<Capture> findAllCaptures_Locked();

    public final QueryResult<ValueSource> findValueSources(CaptureCriteria criteria) {
        try {
            getContextLock().readLock().lock();
            return findValueSources_Locked(criteria);
        } finally {
            getContextLock().readLock().unlock();
        }
    }

    protected abstract QueryResult<ValueSource> findValueSources_Locked(CaptureCriteria criteria);

    public final QueryResult<ValueSource> findValueSources(IdentifiableTimeSeries timeSeries) {
        try {
            getContextLock().readLock().lock();
            return findValueSources_Locked(timeSeries);
        } finally {
            getContextLock().readLock().unlock();
        }
    }

    protected abstract QueryResult<ValueSource> findValueSources_Locked(IdentifiableTimeSeries timeSeries);

    public final QueryResult<ValueSource> findValueSources(String searchPattern) {
        try {
            getContextLock().readLock().lock();
            return findValueSources_Locked(searchPattern);
        } finally {
            getContextLock().readLock().unlock();
        }
    }

    protected abstract QueryResult<ValueSource> findValueSources_Locked(String searchPattern);

    public final QueryResult<ValueSource> findAllValueSources() {
        try {
            getContextLock().readLock().lock();
            return findAllValueSources_Locked();
        } finally {
            getContextLock().readLock().unlock();
        }
    }

    protected abstract QueryResult<ValueSource> findAllValueSources_Locked();

    public final QueryResult<Scheduler> findAllSchedulers() {
        try {
            getContextLock().readLock().lock();
            return findAllSchedulers_Locked();
        } finally {
            getContextLock().readLock().unlock();
        }
    }

    protected abstract QueryResult<Scheduler> findAllSchedulers_Locked();

    public final QueryResult<Scheduler> findSchedulers(String searchPattern) {
        try {
            getContextLock().readLock().lock();
            return findSchedulers_Locked(searchPattern);
        } finally {
            getContextLock().readLock().unlock();
        }
    }

    protected abstract QueryResult<Scheduler> findSchedulers_Locked(String searchPattern);

    public final QueryResult<Scheduler> findSchedulers(Triggerable triggerable) {
        try {
            getContextLock().readLock().lock();
            return findSchedulers_Locked(triggerable);
        } finally {
            getContextLock().readLock().unlock();
        }
    }

    protected abstract QueryResult<Scheduler> findSchedulers_Locked(Triggerable triggerable);

    public final <E> QueryResult<E> findAll(Class<E> assignableToClass) {
        try {
            getContextLock().readLock().lock();
            return findAllChildren_Locked(assignableToClass);
        } finally {
            getContextLock().readLock().unlock();
        }
    }

    protected abstract <E> QueryResult<E> findAllChildren_Locked(Class<E> assignableToClass);

    public final <E> QueryResult<E> findAll(String searchPattern, Class<E> assignableToClass) {
        try {
            getContextLock().readLock().lock();
            return findAllChildren_Locked(searchPattern, assignableToClass);
        } finally {
            getContextLock().readLock().unlock();
        }
    }

    protected abstract <E> QueryResult<E> findAllChildren_Locked(String searchPattern, Class<E> assignableToClass);
}