package com.od.jtimeseries.ui.timeserious;

import com.od.jtimeseries.ui.config.ConfigAware;
import com.od.jtimeseries.ui.config.DesktopConfiguration;
import com.od.jtimeseries.ui.config.TimeSeriousConfig;
import com.od.jtimeseries.ui.displaypattern.DisplayNameCalculator;
import com.od.jtimeseries.ui.event.TimeSeriousBusListener;
import com.od.jtimeseries.ui.net.udp.UiTimeSeriesServerDictionary;
import com.od.jtimeseries.ui.selector.SeriesSelectionPanel;
import com.od.jtimeseries.ui.timeserious.action.ApplicationActionModels;
import com.od.swing.eventbus.EventSender;
import com.od.swing.eventbus.UIEventBus;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.util.Collections;

/**
 * Created by IntelliJ IDEA.
 * User: Nick Ebbutt
 * Date: 08/04/11
 * Time: 17:02
 */
public class AbstractDesktopFrame extends JFrame  implements ConfigAware {

    private TimeSeriousDesktopPane desktopPane;
    private DesktopContext desktopContext;

    public AbstractDesktopFrame(UiTimeSeriesServerDictionary serverDictionary, DisplayNameCalculator displayNameCalculator, DesktopContext desktopContext, SeriesSelectionPanel selectionPanel) {
        this.desktopContext = desktopContext;
        this.desktopPane = new TimeSeriousDesktopPane(this, serverDictionary, displayNameCalculator, selectionPanel, desktopContext);
        getContentPane().add(desktopPane, BorderLayout.CENTER);
    }

    protected void configureFrame(DesktopConfiguration c) {
        Rectangle frameLocation = c.getFrameLocation();
        if ( frameLocation != null) {
            setBounds(frameLocation);
            setExtendedState(c.getFrameExtendedState());
        } else {
            setSize(800, 600);
            setLocationRelativeTo(null);
        }
    }

    protected TimeSeriousDesktopPane getDesktopPane() {
        return desktopPane;
    }

    //set the selected desktop in the desktopSelectionActionModel when this window is focused
    protected class DesktopSelectionWindowFocusListener implements WindowFocusListener {

        public void windowGainedFocus(WindowEvent e) {
            UIEventBus.getInstance().fireEvent(TimeSeriousBusListener.class,
                new EventSender<TimeSeriousBusListener>() {
                    public void sendEvent(TimeSeriousBusListener listener) {
                        listener.desktopSelected(desktopPane);
                    }
                }
            );
        }

        public void windowLostFocus(WindowEvent e) {
        }
    }


    public void restoreConfig(TimeSeriousConfig config) {
        configureFrame(desktopContext.getDesktopConfiguration());
    }

    public java.util.List<ConfigAware> getConfigAwareChildren() {
        return Collections.singletonList((ConfigAware) desktopPane);
    }

    public void prepareConfigForSave(TimeSeriousConfig config) {}
}
