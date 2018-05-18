/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 */

package org.apache.jmeter.visualizers;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import org.apache.jmeter.gui.util.FileDialoger;
import org.apache.jmeter.gui.util.HeaderAsPropertyRenderer;
import org.apache.jmeter.samplers.Clearable;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.save.CSVSaveService;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.visualizers.gui.AbstractVisualizer;
import org.apache.jorphan.gui.ObjectTableModel;
import org.apache.jorphan.gui.RendererUtils;
import org.apache.jorphan.util.JOrphanUtils;

/**
 * Aggregrate Table-Based Reporting Visualizer for JMeter. Props to the people
 * who've done the other visualizers ahead of me (Stefano Mazzocchi), who I
 * borrowed code from to start me off (and much code may still exist). Thank
 * you!
 *
 */
public class StatVisualizer extends AbstractVisualizer implements Clearable, ActionListener {

    private static final long serialVersionUID = 240L;

    private static final String USE_GROUP_NAME = "useGroupName"; //$NON-NLS-1$

    private static final String SAVE_HEADERS = "saveHeaders"; //$NON-NLS-1$

    private final String TOTAL_ROW_LABEL = JMeterUtils
            .getResString("aggregate_report_total_label"); //$NON-NLS-1$

    private JTable myJTable;

    private JScrollPane myScrollPane;

    private final JButton saveTable = new JButton(
            JMeterUtils.getResString("aggregate_graph_save_table")); //$NON-NLS-1$

    // should header be saved with the data?
    private final JCheckBox saveHeaders = new JCheckBox(
            JMeterUtils.getResString("aggregate_graph_save_table_header"), true); //$NON-NLS-1$

    private final JCheckBox useGroupName = new JCheckBox(
            JMeterUtils.getResString("aggregate_graph_use_group_name")); //$NON-NLS-1$

    private transient ObjectTableModel model;

    /**
     * Lock used to protect tableRows update + model update
     */
    private final transient Object lock = new Object();

    private final Map<String, SamplingStatCalculator> tableRows = new ConcurrentHashMap<>();

    public StatVisualizer() {
        super();
        model = StatGraphVisualizer.createObjectTableModel();
        clearData();
        init();
    }

    /**
     * @return <code>true</code> iff all functors can be found
     * @deprecated - only for use in testing
     * */
    @Deprecated
    public static boolean testFunctors(){
        StatVisualizer instance = new StatVisualizer();
        return instance.model.checkFunctors(null,instance.getClass());
    }

    @Override
    public String getLabelResource() {
        return "aggregate_report";  //$NON-NLS-1$
    }

    @Override
    public void add(final SampleResult res) {
        JMeterUtils.runSafe(false, new Runnable() {
            @Override
            public void run() {
                SamplingStatCalculator row = null;
                final String sampleLabel = res.getSampleLabel(useGroupName.isSelected());
                synchronized (lock) {
                    row = tableRows.get(sampleLabel);
                    if (row == null) {
                        row = new SamplingStatCalculator(sampleLabel);
                        tableRows.put(row.getLabel(), row);
                        model.insertRow(row, model.getRowCount() - 1);
                    }
                }
                /*
                 * Synch is needed because multiple threads can update the counts.
                 */
                synchronized(row) {
                    row.addSample(res);
                }
                SamplingStatCalculator tot = tableRows.get(TOTAL_ROW_LABEL);
                synchronized(tot) {
                    tot.addSample(res);
                }
                model.fireTableDataChanged();
            }
        });
    }

    /**
     * Clears this visualizer and its model, and forces a repaint of the table.
     */
    @Override
    public void clearData() {
        synchronized (lock) {
            model.clearData();
            tableRows.clear();
            tableRows.put(TOTAL_ROW_LABEL, new SamplingStatCalculator(TOTAL_ROW_LABEL));
            model.addRow(tableRows.get(TOTAL_ROW_LABEL));
        }
    }

    /**
     * Main visualizer setup.
     */
    private void init() { // WARNING: called from ctor so must not be overridden (i.e. must be private or final)
        this.setLayout(new BorderLayout());

        // MAIN PANEL
        JPanel mainPanel = new JPanel();
        Border margin = new EmptyBorder(10, 10, 5, 10);

        mainPanel.setBorder(margin);
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        mainPanel.add(makeTitlePanel());

        // SortFilterModel mySortedModel =
        // new SortFilterModel(myStatTableModel);
        myJTable = new JTable(model);
        JMeterUtils.applyHiDPI(myJTable);
        myJTable.getTableHeader().setDefaultRenderer(new HeaderAsPropertyRenderer(StatGraphVisualizer.COLUMNS_MSG_PARAMETERS));
        myJTable.setPreferredScrollableViewportSize(new Dimension(500, 70));
        RendererUtils.applyRenderers(myJTable, StatGraphVisualizer.RENDERERS);
        myScrollPane = new JScrollPane(myJTable);
        this.add(mainPanel, BorderLayout.NORTH);
        this.add(myScrollPane, BorderLayout.CENTER);
        saveTable.addActionListener(this);
        JPanel opts = new JPanel();
        opts.add(useGroupName, BorderLayout.WEST);
        opts.add(saveTable, BorderLayout.CENTER);
        opts.add(saveHeaders, BorderLayout.EAST);
        this.add(opts,BorderLayout.SOUTH);
    }

    @Override
    public void modifyTestElement(TestElement c) {
        super.modifyTestElement(c);
        c.setProperty(USE_GROUP_NAME, useGroupName.isSelected(), false);
        c.setProperty(SAVE_HEADERS, saveHeaders.isSelected(), true);
    }

    @Override
    public void configure(TestElement el) {
        super.configure(el);
        useGroupName.setSelected(el.getPropertyAsBoolean(USE_GROUP_NAME, false));
        saveHeaders.setSelected(el.getPropertyAsBoolean(SAVE_HEADERS, true));
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        if (ev.getSource() == saveTable) {
            JFileChooser chooser = FileDialoger.promptToSaveFile("aggregate.csv");//$NON-NLS-1$
            if (chooser == null) {
                return;
            }
            FileWriter writer = null;
            try {
                writer = new FileWriter(chooser.getSelectedFile()); // TODO Charset ?
                CSVSaveService.saveCSVStats(StatGraphVisualizer.getAllTableData(model, StatGraphVisualizer.FORMATS),writer,
                        saveHeaders.isSelected() ? StatGraphVisualizer.getLabels(StatGraphVisualizer.COLUMNS) : null);
            } catch (IOException e) {
                JMeterUtils.reportErrorToUser(e.getMessage(), "Error saving data");
            } finally {
                JOrphanUtils.closeQuietly(writer);
            }
        }
    }
}

