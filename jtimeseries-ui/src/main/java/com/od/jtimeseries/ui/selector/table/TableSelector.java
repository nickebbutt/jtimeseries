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
package com.od.jtimeseries.ui.selector.table;

import com.jidesoft.grid.SortableTable;
import com.jidesoft.grid.TableModelWrapperUtils;
import com.od.jtimeseries.context.TimeSeriesContext;
import com.od.jtimeseries.ui.selector.shared.SelectorPanel;
import com.od.jtimeseries.ui.timeseries.UIPropertiesTimeSeries;
import com.od.jtimeseries.ui.util.PopupTriggerMouseAdapter;
import com.od.jtimeseries.util.identifiable.Identifiable;
import com.od.jtimeseries.util.identifiable.IdentifiableTreeEvent;
import com.od.jtimeseries.util.identifiable.IdentifiableTreeListener;
import com.od.swing.action.ListSelectionActionModel;
import com.od.swing.util.AwtSafeListener;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: nick
 * Date: 25-May-2009
 * Time: 11:37:55
 * To change this template use File | Settings | File Templates.
 */
public class TableSelector<E extends UIPropertiesTimeSeries> extends SelectorPanel<E> {

    private TimeSeriesContext rootContext;
    private java.util.List<Action> seriesActions;
    private String selectionText;
    private Class<E> seriesClass;
    private BeanPerRowModel<E> tableModel;
    private SortableTable timeSeriesTable;
    private JPopupMenu tablePopupMenu;
    private TableColumnManager<E> tableColumnManager;

    public TableSelector(ListSelectionActionModel<E> seriesActionModel,
                         TimeSeriesContext rootContext,
                         java.util.List<Action> seriesActions,
                         String selectionText,
                         Class<E> seriesClass) {
        super(seriesActionModel);
        this.rootContext = rootContext;
        this.seriesActions = seriesActions;
        this.selectionText = selectionText;
        this.seriesClass = seriesClass;
        createTable();
        refreshSeries();
        createPopupMenu();

        setLayout(new BorderLayout());
        add(new JScrollPane(timeSeriesTable), BorderLayout.CENTER);
        addSeriesSelectionListener();
        addContextListener();
    }

    private void addContextListener() {
        rootContext.addTreeListener(
            AwtSafeListener.getAwtSafeListener(
                new IdentifiableTreeListener() {
                    public void nodeChanged(Identifiable node, Object changeDescription) {
                    }

                    public void descendantChanged(IdentifiableTreeEvent contextTreeEvent) {
                        repaint();
                    }

                    public void descendantAdded(IdentifiableTreeEvent contextTreeEvent) {
                        List<E> timeSeries = getAffectedSeries(seriesClass, contextTreeEvent);
                        tableModel.addObjects(timeSeries);
                    }

                    public void descendantRemoved(IdentifiableTreeEvent contextTreeEvent) {
                        List<E> timeSeries = getAffectedSeries(seriesClass, contextTreeEvent);
                        for ( E series : timeSeries) {
                            tableModel.removeObject(series);
                        }
                    }
                },
                IdentifiableTreeListener.class
            )
        );
    }

    public TableColumnManager getTableColumnManager() {
        return tableColumnManager;
    }

    public void setColumns(List<ColumnSettings> columnSettings) {
        tableColumnManager.setColumns(columnSettings);
    }

    public List<ColumnSettings> getColumnSettings() {
        return tableColumnManager.getColumnSettings();
    }

    private void createPopupMenu() {
        tablePopupMenu = new JPopupMenu("Series Actions");
        for ( Action a : seriesActions) {
            tablePopupMenu.add(a);
        }

        timeSeriesTable.addMouseListener(
            new PopupTriggerMouseAdapter(tablePopupMenu, timeSeriesTable)
        );
    }

    private void createTable() {
        tableModel = new TableModelCreator().createTableModel(seriesClass);
        tableColumnManager = new TableColumnManager<E>(tableModel, selectionText);
        timeSeriesTable = new TimeSeriesTable<E>(tableModel, tableColumnManager);
    }

    private void addSeriesSelectionListener() {
        timeSeriesTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        timeSeriesTable.getSelectionModel().addListSelectionListener(
            new ListSelectionListener() {
                public void valueChanged(ListSelectionEvent e) {
                    if ( ! e.getValueIsAdjusting() && timeSeriesTable.getSelectedRow() > -1 ) {
                        int modelRow = TableModelWrapperUtils.getActualRowAt(timeSeriesTable.getModel(),timeSeriesTable.getSelectedRow());
                        E series = tableModel.getObject(modelRow);
                        getSeriesActionModel().setSelected(series);
                        fireSelectedForDescription(series);
                    }
                }
            }
        );
    }

    public void refreshSeries() {
        tableModel.clear();
        List<E> l = rootContext.findAll(seriesClass).getAllMatches();
        List<E> timeSeries = new ArrayList<E>();
        for ( E i : l) {
            timeSeries.add(i);
        }
        tableModel.addObjects(timeSeries);
    }

    public void addAllDynamicColumns() {
        tableColumnManager.addAllDynamicColumns();    
    }
}
