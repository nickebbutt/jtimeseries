package com.od.jtimeseries.server.timeseries;

import com.od.jtimeseries.timeseries.impl.AbstractListTimeSeriesTest;
import com.od.jtimeseries.server.serialization.RoundRobinSerializer;
import com.od.jtimeseries.server.serialization.TestRoundRobinSerializer;
import com.od.jtimeseries.server.serialization.SerializationException;
import com.od.jtimeseries.timeseries.impl.TimeSeriesTestUtils;
import com.od.jtimeseries.util.time.Time;
import com.od.jtimeseries.context.TimeSeriesContext;
import com.od.jtimeseries.context.impl.DefaultTimeSeriesContext;

import java.io.File;

import org.junit.Test;

/**
 * Created by IntelliJ IDEA.
 * User: nick
 * Date: 19-May-2009
 * Time: 10:11:54
 * To change this template use File | Settings | File Templates.
 */
public class TestFilesystemTimeSeries extends AbstractListTimeSeriesTest<FilesystemTimeSeries> {

    private RoundRobinSerializer roundRobinSerializer;
    private final String TEST_CONTEXT = "test";

    public void setUp() throws Exception {
        RoundRobinSerializer.setShutdownHandlingDisabled(true);
        roundRobinSerializer = TestRoundRobinSerializer.createTestSerializer();
        super.setUp();
    }

    public void tearDown() {
        super.tearDown();
        roundRobinSerializer = null;
    }

    public FilesystemTimeSeries getTimeSeriesInstance() throws Exception {
        TimeSeriesContext context = new DefaultTimeSeriesContext().createContext(TEST_CONTEXT);
        FilesystemTimeSeries s = new FilesystemTimeSeries(context.getPath(), "id" + (int)(Math.random() * 100000000), "description", roundRobinSerializer, 10000, Time.seconds(10), Time.seconds(10));
        context.addChild(s);
        File file = roundRobinSerializer.getFile(s.getFileHeader());
        file.deleteOnExit();
        return s;
    }


    @Test
    public void testWriteBehindCache() {
        FilesystemTimeSeries series = getTimeSeries();
        assertEquals(0, series.getFileHeader().getCurrentSize());

        //size taken from the soft reference round robin series in memory, but series on disk and file header not yet updated
        series.add(TimeSeriesTestUtils.createItemWithTimestamp(1));
        assertEquals(1, series.size());
        assertEquals(0, series.getFileHeader().getCurrentSize());

        //we have only appended an item, this can go in the append list, no need to reserialize the whole series
        assertEquals(1, series.getCacheAppendListSize());
        assertFalse(series.isSeriesInWriteCache());

        //trigger gc of the main round robin series in memory, the write behind cache values still remain
        series.triggerGarbageCollection();
        assertTrue(series.isSeriesCollected());
        assertEquals(0, series.getFileHeader().getCurrentSize()); //file header not yet update, file not yet changed
        //the appended item still in the append list
        assertEquals(1, series.getCacheAppendListSize());

        //size taken from the values in the write behind cache since the round robin series collected
        //file header not yet updated, no need to load the series back into memory to calc the size
        series.append(TimeSeriesTestUtils.createItemWithTimestamp(2));
        assertEquals(2, series.getCacheAppendListSize());
        assertEquals(2, series.size());
        assertEquals(0, series.getFileHeader().getCurrentSize());
        assertTrue(series.isSeriesCollected());

        //flush the write behind cache
        series.flush();
        assertTrue(series.isCacheFlushed());
        assertTrue(series.isSeriesCollected());
        assertEquals(2, series.getFileHeader().getCurrentSize());  //now the header has been updated on the flush/append, so size is updated in the header
        assertEquals(2, series.size());

        series.add(TimeSeriesTestUtils.createItemWithTimestamp(4));  //after this we have one item in our write behind cache
        assertEquals(3, series.size());

        //collect the main series
        series.triggerGarbageCollection();
        assertTrue(series.isSeriesCollected());
        assertEquals(1, series.getCacheAppendListSize());

        //doing an insert should trigger derserialization because we can't use the in memory append list in the cache
        //any values in the local append cache should be appended to the deserialized series and the cache cleared
        //before the insert takes place, so we end up with an up to date series in memory
        series.add(2, TimeSeriesTestUtils.createItemWithTimestamp(3));
        assertFalse(series.isSeriesCollected());
        assertTrue(series.isSeriesInWriteCache());

        series.triggerGarbageCollection();  //series soft ref is now collected, but since it was changed, still referenced from write behind cache
        assertTrue(series.isSeriesCollected());
        assertTrue(series.isSeriesInWriteCache());
        assertEquals(4, series.size());
        assertEquals(1, series.get(0).getTimestamp());
        assertEquals(2, series.get(1).getTimestamp());
        assertEquals(3, series.get(2).getTimestamp());
        assertEquals(4, series.get(3).getTimestamp());
        assertTrue(series.isSeriesCollected()); //to read these values we could still access the series via the cache

        series.flush();
        assertTrue(series.isSeriesCollected());
        assertFalse(series.isSeriesInWriteCache()); //now series neither soft referenced nor in cache, but header updated
        assertEquals(4, series.getFileHeader().getCurrentSize());
        assertEquals(4, series.size());
        assertTrue(series.isSeriesCollected());  //size read from the header, no need to deserialize

        assertEquals(1, series.get(0).getTimestamp());
        assertEquals(2, series.get(1).getTimestamp());
        assertEquals(3, series.get(2).getTimestamp());
        assertEquals(4, series.get(3).getTimestamp());
        assertFalse(series.isSeriesCollected());  //to read these values again we had to deserialize

    }

    @Test
    public void testMaximumSize() throws SerializationException {
        TimeSeriesContext c = new DefaultTimeSeriesContext().createContext("test");
        int maxSize = 3;
        FilesystemTimeSeries series = new FilesystemTimeSeries(c.getPath(), "id" + (int)(Math.random() * 100000000), "description", roundRobinSerializer, maxSize, Time.seconds(10), Time.seconds(10));
        c.addChild(series);

        //after creation, we have just read the header, not deserialized the series yet
        assertTrue(series.isSeriesCollected());

        series.append(TimeSeriesTestUtils.createItemWithTimestamp(1));
        series.append(TimeSeriesTestUtils.createItemWithTimestamp(2));
        series.append(TimeSeriesTestUtils.createItemWithTimestamp(3));
        series.append(TimeSeriesTestUtils.createItemWithTimestamp(4));

        assertEquals(3, series.size());
        assertEquals(2, series.get(0).getTimestamp());

        series.triggerGarbageCollection();
        series.flush();

        series.append(TimeSeriesTestUtils.createItemWithTimestamp(5));
        assertEquals(3, series.size());
        assertEquals(1, series.getCacheAppendListSize());
        assertTrue(series.isSeriesCollected());

        assertEquals(3, series.get(0).getTimestamp());
        assertFalse(series.isSeriesCollected());

        series.append(TimeSeriesTestUtils.createItemWithTimestamp(6));
        series.append(TimeSeriesTestUtils.createItemWithTimestamp(7));
        series.append(TimeSeriesTestUtils.createItemWithTimestamp(8));
        series.append(TimeSeriesTestUtils.createItemWithTimestamp(9));

        series.triggerGarbageCollection();
        assertEquals(3, series.size());
        //cached append items cannot grow above 3 since that is the max series size, should contain 3 most recent items
        assertEquals(3, series.getCacheAppendListSize());

        series.flush();
        assertTrue(series.isSeriesCollected());
        assertEquals(7, series.get(0).getTimestamp());
        assertEquals(8, series.get(1).getTimestamp());
        assertEquals(9, series.get(2).getTimestamp());
        assertFalse(series.isSeriesCollected());
    }

    @Test
    public void testLastModifiedTimestamp() throws SerializationException {
        FilesystemTimeSeries series = getTimeSeries();
        assertEquals(-1, series.getLatestTimestamp());

        series.append(TimeSeriesTestUtils.createItemWithTimestamp(1));
        assertEquals(1, series.getLatestTimestamp());

        series.triggerGarbageCollection();
        series.flush();

        assertEquals(1, series.getLatestTimestamp());
        series.add(TimeSeriesTestUtils.createItemWithTimestamp(3));
        assertEquals(3, series.getLatestTimestamp());

        series.add(1, TimeSeriesTestUtils.createItemWithTimestamp(2));
        assertEquals(3, series.getLatestTimestamp());

        //the file header is out of date because we have local changes, until we flush
        assertEquals(1, series.getFileHeader().getMostRecentItemTimestamp());
        series.flush();
        assertEquals(3, series.getFileHeader().getMostRecentItemTimestamp());

        TimeSeriesContext context = new DefaultTimeSeriesContext().createContext(TEST_CONTEXT);
        FilesystemTimeSeries s = new FilesystemTimeSeries(context.getPath(), series.getId(), "description", roundRobinSerializer, 10000, Time.seconds(10), Time.seconds(10));
        context.addChild(s);
        assertEquals(3, s.getLatestTimestamp());
    }

}
