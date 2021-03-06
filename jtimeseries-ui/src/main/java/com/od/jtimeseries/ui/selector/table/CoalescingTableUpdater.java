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
package com.od.jtimeseries.ui.selector.table;

import com.od.jtimeseries.identifiable.Identifiable;
import com.od.jtimeseries.identifiable.IdentifiableTreeEvent;
import com.od.jtimeseries.identifiable.IdentifiableTreeListener;
import com.od.jtimeseries.ui.selector.shared.SelectorComponent;
import com.od.jtimeseries.util.NamedExecutors;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
* Created by IntelliJ IDEA.
* User: nick
* Date: 20/12/10
*
* Individual inserts into jide table are extremely inefficiently handled when we have a large number of separate inserts
* This class coalesces events from the context tree so that the table is only updated once
*
* It also ensures we don't insert duplicates into the table, since jide bean table model allows the same bean to appear twice at different rows
*
*/
class CoalescingTableUpdater<E extends Identifiable> implements IdentifiableTreeListener {

    private static ScheduledExecutorService coalescingEventExceutor = NamedExecutors.newSingleThreadScheduledExecutor("Selector-CoalescingEventExecutor");

    private final LinkedList<TableChange> changeList = new LinkedList<TableChange>();
    private ScheduledFuture updateEvent;
    private Class seriesClass;
    private BeanPerRowModel<E> tableModel;
    private Component tableComponent;
    private LinkedHashSet tableContents = new LinkedHashSet();

    CoalescingTableUpdater(Class seriesClass, BeanPerRowModel<E> tableModel, Component tableComponent) {
        this.seriesClass = seriesClass;
        this.tableModel = tableModel;
        this.tableComponent = tableComponent;
    }

    public void nodeChanged(Identifiable node, Object changeDescription) {
    }

    public void descendantChanged(IdentifiableTreeEvent contextTreeEvent) {
        tableComponent.repaint();
    }

    public void descendantAdded(IdentifiableTreeEvent contextTreeEvent) {
        synchronized (changeList) {

            contextTreeEvent.processNodesAndDescendants(new IdentifiableTreeEvent.IdentifiableProcessor<E>() {
                public void process(E timeSeries) {
                    if (changeList.size() > 0 && changeList.getLast().isAdd()) {
                        changeList.getLast().addBean(timeSeries);
                    } else {
                        LinkedHashSet beans = new LinkedHashSet();
                        beans.add(timeSeries);
                        changeList.add(new TableChange(true, beans));
                    }
                    scheduleUpdate();
                }
            }, seriesClass);
        }
    }

    public void descendantRemoved(IdentifiableTreeEvent contextTreeEvent) {
        synchronized (changeList) {
            contextTreeEvent.processNodesAndDescendants(new IdentifiableTreeEvent.IdentifiableProcessor<E>() {
                public void process(E timeSeries) {
                    if (changeList.size() > 0 && !changeList.getLast().isAdd()) {
                        changeList.getLast().addBean(timeSeries);
                    } else {
                        LinkedHashSet beans = new LinkedHashSet();
                        beans.add(timeSeries);
                        changeList.add(new TableChange(false, beans));
                    }
                    scheduleUpdate();
                }
            }, seriesClass);
        }
    }

    private void scheduleUpdate() {
        if ( updateEvent == null || updateEvent.isDone()) {
            updateEvent = coalescingEventExceutor.schedule(new TableUpdateTask<E>(tableModel), 150, TimeUnit.MILLISECONDS);
        }
    }

    private static class TableChange {
        private boolean isAdd;
        private LinkedHashSet beans;

        private TableChange(boolean add, LinkedHashSet beans) {
            isAdd = add;
            this.beans = beans;
        }

        public void addBean(Object bean) {
            this.beans.add(bean);
        }

        public boolean isAdd() {
            return isAdd;
        }

        public LinkedHashSet getBeans() {
            return beans;
        }
    }

    private class TableUpdateTask<E> implements Runnable {

        private BeanPerRowModel<E> tableModel;

        private TableUpdateTask(BeanPerRowModel<E> tableModel) {
            this.tableModel = tableModel;
        }

        public void run() {
            final LinkedList<TableChange> changeListSnapshot;
            synchronized (changeList) {
                changeListSnapshot = new LinkedList<TableChange>(changeList);
                changeList.clear();
            }

            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    for ( TableChange c : changeListSnapshot ) {
                        if ( c.isAdd() ) {
                            //jide bean model allows the same object to appear twice! Not generally helpful
                            //we have to eliminate duplicates here
                            LinkedHashSet s = c.getBeans();
                            s.removeAll(tableContents);
                            List toAdd = new LinkedList(s);
                            tableModel.addObjects(toAdd);
                            tableContents.addAll(toAdd);
                        } else {
                            //Since there's no jide table model method to remove multiple beans the only way is
                            //to clear and re-add everything!
                            //however, only do this for large selections, since we are more likely to affect
                            //aspects of the JTable view such as user selections by firing bulk clear and add events
                            if ( c.getBeans().size() > 10) {
                                tableModel.clear();
                                tableContents.removeAll(c.getBeans());
                                tableModel.addObjects(new ArrayList(tableContents));
                            }  else {
                                for ( Object o : c.getBeans()) {
                                    tableModel.removeObject((E)o);
                                }
                                tableContents.removeAll(c.getBeans());
                            }


                        }
                    }
                }
            });


        }
    }
}
