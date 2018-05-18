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

/**
 * The class TimeRateAggregator is used to get samples rate by second.
 * 
 * @since 3.0
 */
public class TimeRateAggregator implements Aggregator {

    private long count = 0;
    private long granularity = 1;
    private double value = 0;

    /**
     * @return the granularity
     */
    public final long getGranularity() {
        return granularity;
    }

    /**
     * @param granularity
     *            the granularity to set
     */
    public final void setGranularity(long granularity) {
        this.granularity = granularity;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.jmeter.report.core.GraphAggregator#getCount()
     */
    @Override
    public long getCount() {
        return count;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.jmeter.report.core.GraphAggregator#getResult()
     */
    @Override
    public double getResult() {
        return value * 1000 / granularity;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.jmeter.report.core.GraphAggregator#addValue(double)
     */
    @Override
    public void addValue(final double value) {
        this.count++;
        this.value += value;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.jmeter.report.core.GraphAggregator#reset()
     */
    @Override
    public void reset() {
        count = 0;
        value = 0;
    }

}
