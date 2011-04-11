package com.od.jtimeseries.ui.timeserious;

import com.od.jtimeseries.net.udp.TimeSeriesServerDictionary;
import com.od.jtimeseries.ui.download.panel.TimeSeriesServerContext;
import com.od.jtimeseries.ui.selector.SeriesSelectionPanel;
import com.od.jtimeseries.ui.selector.shared.IdentifiableListActionModel;
import com.od.jtimeseries.ui.selector.shared.SelectorActionFactory;
import com.od.jtimeseries.ui.selector.shared.SelectorComponent;
import com.od.jtimeseries.ui.timeseries.UIPropertiesTimeSeries;
import com.od.jtimeseries.ui.timeserious.action.AddSeriesToActiveVisualizerAction;
import com.od.jtimeseries.ui.timeserious.action.ApplicationActionModels;
import com.od.jtimeseries.ui.timeserious.action.RemoveVisualizerAction;
import com.od.jtimeseries.ui.timeserious.action.ShowHiddenVisualizerAction;
import com.od.jtimeseries.util.identifiable.Identifiable;

import javax.swing.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
* Created by IntelliJ IDEA.
* User: Nick Ebbutt
* Date: 16/03/11
* Time: 07:03
*/
public class MainSelectorActionFactory implements SelectorActionFactory {

    private IdentifiableListActionModel selectionModel;
    private Action addSeriesAction;
    private Action refreshServerAction;
    private Action removeServerAction;
    private Action renameServerAction;
    private Action showHiddenVisualizerAction;
    private Action removeVisualizerAction;


    public MainSelectorActionFactory(TimeSeriousRootContext rootContext, ApplicationActionModels applicationActionModels, SeriesSelectionPanel<UIPropertiesTimeSeries> selectionPanel, TimeSeriesServerDictionary timeSeriesServerDictionary, JComponent parentSelector) {
        this.selectionModel = selectionPanel.getSelectionActionModel();
        addSeriesAction = new AddSeriesToActiveVisualizerAction(applicationActionModels.getVisualizerSelectionActionModel(), selectionModel);
        refreshServerAction = new RefreshServerSeriesAction(rootContext, selectionModel);
        removeServerAction = new RemoveServerAction(parentSelector, timeSeriesServerDictionary, selectionModel);
        renameServerAction = new RenameServerAction(parentSelector, selectionModel);
        showHiddenVisualizerAction = new ShowHiddenVisualizerAction(selectionModel);
        removeVisualizerAction = new RemoveVisualizerAction(selectionModel);
    }

    public java.util.List<Action> getActions(SelectorComponent s, List<Identifiable> selectedIdentifiable) {
        java.util.List<Action> result = Collections.emptyList();
        if (selectionModel.isSelectionLimitedToType(UIPropertiesTimeSeries.class)) {
            result = Arrays.asList(
                addSeriesAction
            );
        } else if ( selectionModel.isSelectionLimitedToType(VisualizerContext.class)) {
            result = Arrays.asList(
                showHiddenVisualizerAction,
                removeVisualizerAction
            );
        } else if ( selectionModel.isSelectionLimitedToType(TimeSeriesServerContext.class)) {
            result = Arrays.asList(
                refreshServerAction,
                removeServerAction,
                renameServerAction
            );
        }
        return result;
    }
}
