/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.apache.jmeter.report.processor;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.report.core.Sample;
import org.apache.jmeter.util.JMeterUtils;

/**
 * The class Top5ErrorsBySamplerConsumer provides a consumer that calculates
 * the TOP5 of errors by sampler.
 * 
 * @since 3.1
 */
public class Top5ErrorsBySamplerConsumer extends
        AbstractSummaryConsumer<Top5ErrorsSummaryData> {
    static final int MAX_NUMBER_OF_ERRORS_IN_TOP = 5;
    
    private boolean ignoreTCFromTop5ErrorsBySampler;

    /**
     * Instantiates a new Top5 Errors by sampler consumer.
     */
    public Top5ErrorsBySamplerConsumer() {
        super(true);
    }

    /**
     * 
     * @param sample {@link Sample}
     * @param data {@link Top5ErrorsSummaryData}
     * @param isOverall boolean indicating if aggregation concerns the Overall results in which case we ignore Transaction Controller's SampleResult
     */
    private void aggregateSample(Sample sample, Top5ErrorsSummaryData data, boolean isOverall) {
        if(isOverall && sample.isController()) {
            return;
        }
        
        if(ignoreTCFromTop5ErrorsBySampler && sample.isController()) {
            return;
        }
        
        if(!sample.getSuccess()) {
            data.registerError(getError(sample));
            data.incErrors();
        }
        data.incTotal();
    }
    
    private String getError(Sample sample) {
        String responseCode = sample.getResponseCode();
        String responseMessage = sample.getResponseMessage();
        String key = responseCode + (!StringUtils.isEmpty(responseMessage) ? 
                 "/" + StringEscapeUtils.escapeJson(responseMessage) : "");
        if (ErrorsSummaryConsumer.isSuccessCode(responseCode)) {
            key = ErrorsSummaryConsumer.ASSERTION_FAILED;
            if (ErrorsSummaryConsumer.ASSERTION_RESULTS_FAILURE_MESSAGE) {
                String msg = sample.getFailureMessage();
                if (!StringUtils.isEmpty(msg)) {
                    key = StringEscapeUtils.escapeJson(msg);
                }
            }
        }
        return key;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.apache.jmeter.report.processor.AbstractSummaryConsumer#updateData
     * (org.apache.jmeter.report.processor.AbstractSummaryConsumer.SummaryInfo,
     * org.apache.jmeter.report.core.Sample)
     */
    @Override
    protected void updateData(SummaryInfo info, Sample sample) {
        SummaryInfo overallInfo = getOverallInfo();
        Top5ErrorsSummaryData overallData = overallInfo.getData();
        if (overallData == null) {
            overallData = new Top5ErrorsSummaryData();
            overallInfo.setData(overallData);
        }

        Top5ErrorsSummaryData data = info.getData();
        if (data == null) {
            data = new Top5ErrorsSummaryData();
            info.setData(data);
        }

        if(!sample.isEmptyController()) {
            aggregateSample(sample, data, false);
            aggregateSample(sample, overallData, true);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.apache.jmeter.report.processor.AbstractSummaryConsumer#createDataResult
     * (java.lang.String)
     */
    @Override
    protected ListResultData createDataResult(String key,
            Top5ErrorsSummaryData data) {
        ListResultData result = new ListResultData();
        long errors = data.getErrors();
        if(errors > 0 || key == null) {
            result.addResult(new ValueResultData(
                    key != null ? key : JMeterUtils.getResString("reportgenerator_top5_total")));
            long total = data.getTotal();
            
            result.addResult(new ValueResultData(Long.valueOf(total)));
            result.addResult(new ValueResultData(Long.valueOf(errors)));
            Object[][] top5 = data.getTop5ErrorsMetrics();

            int numberOfValues = 0;
            for (int i = 0; i < top5.length; i++) {
                result.addResult(new ValueResultData(top5[i][0]));
                result.addResult(new ValueResultData(top5[i][1]));
                numberOfValues++;            
            }
            for (int i = numberOfValues; i < MAX_NUMBER_OF_ERRORS_IN_TOP; i++) {
                result.addResult(new ValueResultData(""));
                result.addResult(new ValueResultData(""));   
            }
        }
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.apache.jmeter.report.processor.AbstractSummaryConsumer#getKeyFromSample
     * (org.apache.jmeter.report.core.Sample)
     */
    @Override
    protected String getKeyFromSample(Sample sample) {
        return sample.getName();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.apache.jmeter.report.processor.AbstractSummaryConsumer#createResultTitles
     * ()
     */
    @Override
    protected ListResultData createResultTitles() {
        ListResultData titles = new ListResultData();
        titles.addResult(new ValueResultData(JMeterUtils
            .getResString("reportgenerator_top5_label")));
        titles.addResult(new ValueResultData(JMeterUtils
            .getResString("reportgenerator_top5_sample_count")));
        titles.addResult(new ValueResultData(JMeterUtils
            .getResString("reportgenerator_top5_error_count")));
        for (int i = 0; i < MAX_NUMBER_OF_ERRORS_IN_TOP; i++) {
            titles.addResult(new ValueResultData(
                    JMeterUtils.getResString("reportgenerator_top5_error_label")));
            titles.addResult(new ValueResultData(
                    JMeterUtils.getResString("reportgenerator_top5_error_count")));            
        }
        return titles;
    }

    /**
     * @param ignoreTCFromTop5ErrorsBySampler
     */
    public void setIgnoreTransactionController(
            boolean ignoreTCFromTop5ErrorsBySampler) {
        this.ignoreTCFromTop5ErrorsBySampler = ignoreTCFromTop5ErrorsBySampler;
    }
}
