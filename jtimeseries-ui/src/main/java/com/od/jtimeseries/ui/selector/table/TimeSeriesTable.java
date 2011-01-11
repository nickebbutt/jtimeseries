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

import com.jidesoft.grid.AutoFilterTableHeader;
import com.jidesoft.grid.SortableTable;
import com.jidesoft.grid.TableModelWrapperUtils;
import com.od.jtimeseries.ui.timeseries.UIPropertiesTimeSeries;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
* User: nick
* Date: 27-Feb-2010
* Time: 15:57:14
* To change this template use File | Settings | File Templates.
*/
class TimeSeriesTable<E extends UIPropertiesTimeSeries> extends SortableTable {

    private static final Color STALE_SERIES_COLOR = new Color(248,165,169);
    private BeanPerRowModel<E> tableModel;

    public TimeSeriesTable(BeanPerRowModel<E> tableModel, TableColumnManager<E> columnManager) {
        super(tableModel);
        setColumnModel(columnManager.getColumnModel());
        this.tableModel = tableModel;
        setClearSelectionOnTableDataChanges(false);
        setRowResizable(true);
        setVariousRowHeights(true);
        setSelectInsertedRows(false);
        setAutoSelectTextWhenStartsEditing(true);
        setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        setRowResizable(false);
        AutoFilterTableHeader header = new AutoFilterTableHeader(this);
        header.setAutoFilterEnabled(true);
        header.setShowFilterName(true);
        header.setAllowMultipleValues(true);
        header.setShowFilterNameAsToolTip(true);
        setAutoCreateColumnsFromModel(false);
        setTableHeader(header);
        setModel(tableModel);
    }

    public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
        Component c = super.prepareRenderer(renderer, row, column);

        int actualRow = TableModelWrapperUtils.getActualRowAt(getModel(), row);
        E object = tableModel.getObject(actualRow);
        if (object.isStale()) {
            c.setBackground(STALE_SERIES_COLOR);
        } else {
            if (isCellSelected(row, column)) {
                c.setBackground(getSelectionBackground());
            } else {
                c.setBackground(getBackground());
            }
        }
        return c;
    }

    //stop the creation of initial columns
    public void createDefaultColumnsFromModel() {
    }
}
