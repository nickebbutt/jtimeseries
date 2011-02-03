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
package com.od.jtimeseries.ui.visualizer.chart;

import com.jidesoft.combobox.ColorComboBox;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Enumeration;

/**
 * Created by IntelliJ IDEA.
* User: nick
* Date: 26-Feb-2010
* Time: 23:25:15
* To change this template use File | Settings | File Templates.
*/
public class ChartControlPanel extends JPanel {

    private TimeSeriesChart timeSeriesChart;
    private Box radioButtonBox = Box.createHorizontalBox();
    private ColorComboBox colorComboBox = new ColorComboBox();
    private ButtonGroup radioButtonGroup;
    private JCheckBox showLegendCheckbox = new JCheckBox("Legend");
    private RangeSelectorComponent rangeSelectorComponent = new RangeSelectorComponent();

    public ChartControlPanel(TimeSeriesChart timeSeriesChart) {
        this.timeSeriesChart = timeSeriesChart;
        createColorCombo();
        createShowLegendCheckbox();
        createRangeModeRadioButtons();
        layoutPanel();
        refreshStateFromChart();
        addRangeSelectorListener();
    }

    public void refreshStateFromChart() {
        updateSelectedRangeMode();
        updateDomainSelection();
        showLegendCheckbox.setSelected(timeSeriesChart.isShowLegend());
        colorComboBox.setSelectedColor(timeSeriesChart.getChartBackgroundColor());
    }

    private void updateDomainSelection() {
        DomainTimeSelection d = timeSeriesChart.getDomainStartTimeSelection();
        rangeSelectorComponent.setDomainSelection(d);
    }

    private void updateSelectedRangeMode() {
        Enumeration<AbstractButton> e = radioButtonGroup.getElements();
        while(e.hasMoreElements()) {
            AbstractButton b = e.nextElement();
            if (timeSeriesChart.getChartRangeMode().toString().equals(b.getActionCommand())) {
                b.setSelected(true);
                break;
            }
        }
    }

    private void addRangeSelectorListener() {
        rangeSelectorComponent.addPropertyChangeListener(
            RangeSelectorComponent.DOMAIN_SELECTION_PROPERTY,
            new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    timeSeriesChart.setDomainStartTimeSelection((DomainTimeSelection) evt.getNewValue());
                }
            }
        );
    }

    private void createShowLegendCheckbox() {
        showLegendCheckbox = new JCheckBox("Legend");
        showLegendCheckbox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                timeSeriesChart.setShowLegend(showLegendCheckbox.isSelected());
            }
        });
    }

    private void createColorCombo() {
        colorComboBox.setPreferredSize(new Dimension(65, colorComboBox.getPreferredSize().height));
        colorComboBox.setMaximumSize(colorComboBox.getPreferredSize());
        colorComboBox.setColorValueVisible(false);
        colorComboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                timeSeriesChart.setChartBackgroundColor(colorComboBox.getSelectedColor());
            }
        });
    }

    private void createRangeModeRadioButtons() {
        radioButtonGroup = new ButtonGroup();
        for ( ChartRangeMode m : ChartRangeMode.values()) {
            JRadioButton b = new JRadioButton(m.toString());
            radioButtonGroup.add(b);
            b.setActionCommand(m.toString());
            addActionListener(b, m);
            radioButtonBox.add(b);
        }
    }

    private void addActionListener(JRadioButton b, final ChartRangeMode m) {
        b.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ChartControlPanel.this.timeSeriesChart.setChartRangeMode(m);
            }
        });
    }

    private void layoutPanel() {
        Box b = Box.createHorizontalBox();
        b.add(Box.createHorizontalGlue());
        b.add(colorComboBox);
        b.add(Box.createHorizontalStrut(5));
        b.add(showLegendCheckbox);
        b.add(Box.createHorizontalStrut(5));
        b.add(radioButtonBox);
        b.add(Box.createHorizontalStrut(5));
        b.add(rangeSelectorComponent);

        setPreferredSize(new Dimension(getPreferredSize().width, getPreferredSize().height + 30));
        setLayout(new BorderLayout());
        JScrollPane scrollPane = new JScrollPane(b);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        add(scrollPane, BorderLayout.CENTER);
    }

}
