/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.math3.util;

import org.apache.commons.math3.exception.MaxCountExceededException;

/**
 * Utility that increments a counter until a maximum is reached, at
 * which point, the instance will by default throw a
 * {@link MaxCountExceededException}.
 * However, the user is able to override this behaviour by defining a
 * custom {@link MaxCountExceededCallback callback}, in order to e.g.
 * select which exception must be thrown.
 *
 * @version $Id$
 * @since 3.0
 */
public class Incrementor {
    /**
     * Upper limit for the counter.
     */
    private int maximalCount;
    /**
     * Current count.
     */
    private int count = 0;
    /**
     * Function called at counter exhaustion.
     */
    private final MaxCountExceededCallback maxCountCallback;

    /**
     * Default constructor.
     * For the new instance to be useful, the maximal count must be set
     * by calling {@link #setMaximalCount(int) setMaximalCount}.
     */
    public Incrementor() {
        this(0);
    }

    /**
     * Defines a maximal count.
     *
     * @param max Maximal count.
     */
    public Incrementor(int max) {
        this(max,
             new MaxCountExceededCallback() {
                 /** {@inheritDoc} */
                 public void trigger(int max) {
                     throw new MaxCountExceededException(max);
                 }
             });
    }

    /**
     * Defines a maximal count and a callback method to be triggered at
     * counter exhaustion.
     *
     * @param max Maximal count.
     * @param cb Function to be called when the maximal count has been reached.
     */
    public Incrementor(int max,
                       MaxCountExceededCallback cb) {
        maximalCount = max;
        maxCountCallback = cb;
    }

    /**
     * Sets the upper limit for the counter.
     * This does not automatically reset the current count to zero (see
     * {@link #resetCount()}).
     *
     * @param max Upper limit of the counter.
     */
    public void setMaximalCount(int max) {
        maximalCount = max;
    }

    /**
     * Gets the upper limit of the counter.
     *
     * @return the counter upper limit.
     */
    public int getMaximalCount() {
        return maximalCount;
    }

    /**
     * Gets the current count.
     *
     * @return the current count.
     */
    public int getCount() {
        return count;
    }

    /**
     * Checks whether a single increment is allowed.
     *
     * @return {@code false} if the next call to {@link #incrementCount(int)
     * incrementCount} will trigger a {@code MaxCountExceededException},
     * {@code true} otherwise.
     */
    public boolean canIncrement() {
        return count < maximalCount;
    }

    /**
     * Performs multiple increments.
     * See the other {@link #incrementCount() incrementCount} method).
     *
     * @param value Number of increments.
     * @throws MaxCountExceededException at counter exhaustion.
     */
    public void incrementCount(int value) {
        for (int i = 0; i < value; i++) {
            incrementCount();
        }
    }

    /**
     * Adds one to the current iteration count.
     * At counter exhaustion, this method will call the
     * {@link MaxCountExceededCallback#trigger(int) trigger} method of the
     * callback object passed to the
     * {@link #Incrementor(int,MaxCountExceededCallback) constructor}.
     * If not explictly set, a default callback is used that will throw
     * a {@code MaxCountExceededException}.
     *
     * @throws MaxCountExceededException at counter exhaustion, unless a
     * custom {@link MaxCountExceededCallback callback} has been set at
     * construction.
     */
    public void incrementCount() {
        if (++count > maximalCount) {
            maxCountCallback.trigger(maximalCount);
        }
    }

    /**
     * Resets the counter to 0.
     */
    public void resetCount() {
        count = 0;
    }

    /**
     * Defines a method to be called at counter exhaustion.
     * The {@link #trigger(int) trigger} method should usually throw an exception.
     */
    public interface MaxCountExceededCallback {
        /**
         * Function called when the maximal count has been reached.
         *
         * @param maximalCount Maximal count.
         */
        void trigger(int maximalCount);
    }
}
