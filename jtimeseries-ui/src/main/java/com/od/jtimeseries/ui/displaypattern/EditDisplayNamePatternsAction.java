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
package com.od.jtimeseries.ui.displaypattern;

import com.od.jtimeseries.ui.config.DisplayNamePatternConfig;
import com.od.jtimeseries.ui.util.ImageUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
* User: Nick Ebbutt
* Date: 01-Jun-2009
* Time: 11:14:29
*/
public class EditDisplayNamePatternsAction extends AbstractAction {

    private Component componentToPositionDialog;
    private DisplayNameCalculator displayNameCalculator;
    private List<DisplayNamePatternDialog.DisplayPatternListener> displayPatternListeners = new ArrayList<DisplayNamePatternDialog.DisplayPatternListener>();

    public EditDisplayNamePatternsAction(Component componentToPositionDialog, DisplayNameCalculator displayNameCalculator) {
        super("Display Name Patterns", ImageUtils.DISPLAY_NAME_16x16);
        super.putValue(SHORT_DESCRIPTION, "Edit display name patterns");
        this.componentToPositionDialog = componentToPositionDialog;

        //the calculator stores the list of patterns and is responsible for applying them to timeseries
        this.displayNameCalculator = displayNameCalculator;
    }

    public void addDisplayPatternListener(DisplayNamePatternDialog.DisplayPatternListener l) {
        displayPatternListeners.add(l);
    }

    public void actionPerformed(ActionEvent e) {
        DisplayNamePatternDialog d = new DisplayNamePatternDialog(getDisplayNamePatterns());

        d.addDisplayPatternListener(displayNameCalculator);
        for ( DisplayNamePatternDialog.DisplayPatternListener l : displayPatternListeners ) {
            d.addDisplayPatternListener(l);
        }

        d.setLocationRelativeTo(SwingUtilities.getWindowAncestor(componentToPositionDialog));
        d.setVisible(true);
    }

    public DisplayNamePatternConfig getDisplayNamePatterns() {
        return displayNameCalculator.getDisplayNamePatternConfig();
    }

    public void setDisplayNamePatterns(DisplayNamePatternConfig patterns) {
        displayNameCalculator.setDisplayNamePatternConfig(patterns);
    }
}
