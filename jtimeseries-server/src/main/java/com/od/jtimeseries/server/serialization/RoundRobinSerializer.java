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
package com.od.jtimeseries.server.serialization;

import com.od.jtimeseries.server.serialization.RoundRobinTimeSeries;
import com.od.jtimeseries.server.util.ShutdownHandlerFactory;
import com.od.jtimeseries.timeseries.TimeSeriesItem;
import com.od.jtimeseries.util.logging.LogUtils;
import com.od.jtimeseries.util.logging.LogMethods;
import com.od.jtimeseries.util.numeric.DoubleNumeric;

import java.io.*;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: nick
 * Date: 17-May-2009
 * Time: 22:14:28
 *
 * Handles reading and writing time series data to/from the filesystem
 *
 * There is currently a single lock for both read and write operations
 * This does effectively hamstring performance by limiting both read/write to a single thread, but performance
 * is 'sufficient' at the moment. (our current production server is managing 12000 time series with 30s intervals and
 * there are no problems currently). Memory caching and delayed write for time series appends does help here.
 * Development time allowing it would be better to allow concurrent reads and concurrent writes on different files,
 * or perhaps make use of ReentrantReadWriteLock
 */
public class RoundRobinSerializer implements ShutdownHandlerFactory.ShutdownListener {

    private final File rootDirectory;
    private final String timeSeriesFileSuffix;
    private final int BYTES_IN_HEADER_START = 20;
    private final LogMethods logMethods = LogUtils.getLogMethods(RoundRobinSerializer.class);
    private final Object writeLock = new Object();
    private volatile boolean shutdown;

    public RoundRobinSerializer(File rootDirectory, String timeSeriesFileSuffix) {
        this.rootDirectory = rootDirectory;
        this.timeSeriesFileSuffix = timeSeriesFileSuffix;
    }

    public void serialize(FileHeader fileHeader, RoundRobinTimeSeries t) throws SerializationException {
        synchronized (writeLock) {
            if ( ! shutdown ) {
                byte[] properties = getBytesForProperties(fileHeader);
                int requiredHeaderLength = properties.length + BYTES_IN_HEADER_START;
                fileHeader.calculateHeaderLength(requiredHeaderLength);
        
                //head == -1 is a special convention to indicate the time series is empty
                int head = t.size() == 0 ? -1 : 0;
                fileHeader.setCurrentHead(head);
                fileHeader.setCurrentTail(t.size());
                fileHeader.setSeriesLength(t.getMaxSize());

                File f = getFile(fileHeader);
                DataOutputStream b = null;
                try {
                    b = new DataOutputStream(new FileOutputStream(f));
                    //BYTES_IN_HEADER_START  (20 bytes)
                    b.writeInt(fileHeader.getHeaderLength());  //offset where data will start
                    b.writeInt(fileHeader.getSeriesLength());
                    b.writeInt(fileHeader.getCurrentHead());  //start index in rr structure
                    b.writeInt(fileHeader.getCurrentTail());
                    b.writeInt(properties.length);
                    //Header Properties
                    b.write(properties);
                    byte[] padding = new byte[fileHeader.getHeaderLength() - requiredHeaderLength];
                    b.write(padding);
                    for ( TimeSeriesItem i : t) {
                        b.writeLong(i.getTimestamp());
                        b.writeDouble(i.getValue().doubleValue());
                    }
                } catch (Exception e) {
                    logMethods.logError("Failed to write time series file " + f);
                    throw new SerializationException("Failed to write time series file " + f, e);
                } finally {
                    if ( b != null) {
                        try {
                            b.flush();
                            b.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    public RoundRobinTimeSeries deserialize(FileHeader fileHeader) throws SerializationException {
        synchronized (writeLock) {
            File f = getFile(fileHeader);
            DataInputStream d = null;
            try {
                d = new DataInputStream(new FileInputStream(f));
                readHeader(fileHeader, d);
                return readSeriesData(fileHeader, d);
            } catch (Exception e) {
                throw new SerializationException("Failed to deserialize file " + fileHeader, e);
            } finally {
                if ( d != null) {
                    try {
                        d.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public FileHeader readHeader(File f) throws SerializationException {
        synchronized (writeLock) {
            FileHeader h = new FileHeader();
            doUpdateHeader(h, f);
            return h;
        }
    }

    public boolean fileExists(FileHeader fileHeader) {
        synchronized (writeLock) {
            boolean result = false;
            try {
                File f = getFile(fileHeader);
                result = f.exists();
            } catch (SerializationException e) {
                e.printStackTrace();
            }
            return result;
        }
    }

    public void updateHeader(FileHeader fileHeader) throws SerializationException {
        synchronized (writeLock) {
            File f = getFile(fileHeader);
            if ( ! f.exists()) {
                throw new SerializationException("File for header " + fileHeader + " does not exist");
            } else if ( ! f.canRead()) {
                throw new SerializationException("File for header " + fileHeader + " is not readable");
            }
            doUpdateHeader(fileHeader, f);
        }
    }

    public void append(FileHeader f, List<TimeSeriesItem> l) throws SerializationException {
        synchronized (writeLock) {
            if ( ! shutdown ) {
                File file = getFile(f);
                checkFileWriteable(file);
                RandomAccessFile r = null;
                try {
                    r = new RandomAccessFile(file, "rw");
                    int headerLength = r.readInt();
                    int seriesLength = r.readInt();
                    int currentHead = r.readInt();
                    int currentTail = r.readInt();

                    int currentSize = getCurrentSize(seriesLength, currentHead, currentTail);

                    currentHead = Math.max(currentHead, 0); //manage empty file (head==-1)

                    int newSize = Math.min(currentSize + l.size(), seriesLength);
                    int headAdjust = l.size() - (newSize - currentSize);
                    int newHead = (currentHead + headAdjust) % seriesLength;
                    int newTail = (currentTail + l.size()) % seriesLength;

                    r.seek(8);
                    r.writeInt(newHead);
                    r.writeInt(newTail);
                    f.setCurrentHead(newHead);
                    f.setCurrentTail(newTail);

                    r.seek(headerLength + (currentTail * 16));
                    int currentIndex = currentTail;
                    for ( TimeSeriesItem i : l) {
                        if ( currentIndex == seriesLength ) {
                            currentIndex = 0;
                            r.seek(headerLength);
                        }
                        r.writeLong(i.getTimestamp());
                        r.writeDouble(i.getValue().doubleValue());
                        currentIndex ++;
                    }
                } catch ( Exception e) {
                    throw new SerializationException("Failed to append items to file " + f, e);
                } finally {
                    try {
                        if ( r != null) {
                            r.close();
                        }
                    } catch(IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public File getFile(FileHeader f) throws SerializationException {
        synchronized (writeLock) {
            if ( f.getContextPath() == null) {
                throw new SerializationException("Cannot get File for FileHeader with null context path");
            }

            try {
                String fileName = URLEncoder.encode(f.getContextPath(), "UTF-8") + timeSeriesFileSuffix;
                return new File(rootDirectory, fileName);
            } catch (UnsupportedEncodingException e) {
                throw new SerializationException("Failed to encode file name", e);
            }
        }
    }

    public File createFile(FileHeader fileHeader) throws SerializationException {
        synchronized (writeLock) {
            RoundRobinTimeSeries r = new RoundRobinTimeSeries(fileHeader.getSeriesLength());
            serialize(fileHeader, r);
            return getFile(fileHeader);
        }
    }

    public void shutdownNow() {
        synchronized (writeLock) {
            shutdown = true;
        }
    }

    public File getRootDirectory() {
        return rootDirectory;
    }

    private void doUpdateHeader(FileHeader fileHeader, File f) throws SerializationException {
        DataInputStream d = null;
        try {
            d = new DataInputStream(new FileInputStream(f));
            readHeader(fileHeader, d);
        } catch (Exception e) {
            throw new SerializationException("Failed to deserialize header " + fileHeader, e);
        } finally {
            if ( d != null) {
                try {
                    d.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void checkFileWriteable(File file) throws SerializationException {
        if ( ! file.canWrite()) {
            String error = "Cannot write to file " + file + ". This file no longer exists or is not writeable";
            logMethods.logError(error);
            throw new SerializationException(error);
        }
    }


    private RoundRobinTimeSeries readSeriesData(FileHeader fileHeader, DataInputStream d) throws IOException {
        RoundRobinTimeSeries series = new RoundRobinTimeSeries(fileHeader.getSeriesLength());
        if ( fileHeader.getCurrentHead() != -1) {  //file is not empty
            int itemsRead = 0;
            List<TimeSeriesItem> tailItems = new ArrayList<TimeSeriesItem>();
            if ( fileHeader.getCurrentTail() <= fileHeader.getCurrentHead()) {
                for ( int loop=0; loop < fileHeader.getCurrentTail(); loop++) {
                    tailItems.add(new TimeSeriesItem(d.readLong(), new DoubleNumeric(d.readDouble())));
                }
                itemsRead = fileHeader.getCurrentTail();
            }

            int itemsToSkip = fileHeader.getCurrentHead() - itemsRead;
            skipItems(d, itemsToSkip, fileHeader);

            int itemsToRead = fileHeader.getCurrentTail() > fileHeader.getCurrentHead() ?
                    fileHeader.getCurrentTail() - fileHeader.getCurrentHead() :
                    fileHeader.getSeriesLength() - fileHeader.getCurrentHead();

            //here we read the items into a local list first, then add them all at once
            //this is to avoid triggering an insert event for each time series item when we add them to the series
            List<TimeSeriesItem> itemsToAdd = new ArrayList<TimeSeriesItem>();
            for ( int loop=0; loop < itemsToRead; loop++) {
                itemsToAdd.add(new TimeSeriesItem(d.readLong(), new DoubleNumeric(d.readDouble())));
            }

            for ( TimeSeriesItem i : tailItems) {
                itemsToAdd.add(i);
            }

            series.addAll(itemsToAdd);
        }
        return series;
    }


    private void readHeader(FileHeader fileHeader, DataInputStream d) throws IOException {
        fileHeader.setHeaderLength(d.readInt());
        fileHeader.setSeriesLength(d.readInt());
        fileHeader.setCurrentHead(d.readInt());
        fileHeader.setCurrentTail(d.readInt());
        int propertiesLength = d.readInt();
        fileHeader.setFileProperties(readProperties(fileHeader, d, propertiesLength));

        //skip to end of header section
        skipBytes(d, fileHeader, fileHeader.getHeaderLength() - (propertiesLength + BYTES_IN_HEADER_START));
    }

    private void skipItems(DataInputStream d, int itemsToSkip, FileHeader fileHeader) throws IOException {
        int bytesToSkip = itemsToSkip * 16;
        skipBytes(d, fileHeader, bytesToSkip);
    }

    private void skipBytes(DataInputStream d, FileHeader fileHeader, int bytesToSkip) throws IOException {
        // Read in the bytes
        int offset = 0;
        long numRead = 0;
        while (offset < bytesToSkip
               && (numRead=d.skip(bytesToSkip-offset)) >= 0) {
            offset += numRead;
        }

        if (offset < bytesToSkip) {
            throw new IOException("Failed to skip " + bytesToSkip + " bytes in file " + fileHeader);
        }
    }

    private Properties readProperties(FileHeader fileHeader, DataInputStream d, int propertiesLength) throws IOException {
        byte[] bytes = new byte[propertiesLength];
        // Read in the bytes
        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length
               && (numRead=d.read(bytes, offset, bytes.length-offset)) >= 0) {
            offset += numRead;
        }

        // Ensure all the bytes have been read in
        if (offset < bytes.length) {
            throw new IOException("Could not completely read file " + fileHeader);
        }

        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        Properties p = new Properties();
        p.load(bis);
        return p;
    }

    private byte[] getBytesForProperties(FileHeader f) throws SerializationException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream(1000);
        try {
            f.getFileProperties().store(bos, "TimeSeries");
        } catch (IOException ioe) {
            throw new SerializationException("Failed to serialize properties", ioe);
        }
        return bos.toByteArray();
    }

    public static int getCurrentSize(int seriesLength, int currentHead, int currentTail) {
        return currentHead == -1 ? 0 :
                currentTail > currentHead ?
                    currentTail - currentHead :
                    currentTail + (seriesLength - currentHead);
    }
}
