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

import com.jidesoft.grid.BeanTableModel;

import java.beans.IntrospectionException;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Created by IntelliJ IDEA.
 * User: nick
 * Date: 28-Feb-2010
 * Time: 18:17:43
 * To change this template use File | Settings | File Templates.
 */
public class FixedColumnsBeanModel<E> extends BeanTableModel<E> {

    public FixedColumnsBeanModel(ArrayList<E> arrayList, Class<E> remoteChartingTimeSeriesClass) throws IntrospectionException {
        super(arrayList, remoteChartingTimeSeriesClass, generateColumnConfigStringForBeanTableModel());
    }

      //jide BeanTableModel requires the propertyNames and column display names as a String[]
    private static String[] generateColumnConfigStringForBeanTableModel() {
        java.util.List<String> colConfigStrings = new LinkedList<String>();
        for(FixedColumn c : FixedColumn.values()) {
            colConfigStrings.add(c.getPropertyName());
            colConfigStrings.add(c.getColumnName());
        }
        String[] colConfigString = colConfigStrings.toArray(new String[colConfigStrings.size()]);
        return colConfigString;
    }

    public String getColumnDescription(int colIndex) {
        return FixedColumn.values()[colIndex].getDescription();
    }
}
