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
package org.apache.commons.math3.linear;

import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.exception.util.LocalizedFormats;

/**
 * Exception to be thrown when a symmetric matrix is expected.
 *
 * @since 3.0
 * @version $Id: NonSymmetricMatrixException.java 1244107 2012-02-14 16:17:55Z erans $
 */
public class NonSymmetricMatrixException extends MathIllegalArgumentException {
    /** Serializable version Id. */
    private static final long serialVersionUID = -7518495577824189882L;
    /** Row. */
    private final int row;
    /** Column. */
    private final int column;
    /** Threshold. */
    private final double threshold;

    /**
     * Construct an exception.
     *
     * @param row Row index.
     * @param column Column index.
     * @param threshold Relative symmetry threshold.
     */
    public NonSymmetricMatrixException(int row,
                                       int column,
                                       double threshold) {
        super(LocalizedFormats.NON_SYMMETRIC_MATRIX, row, column, threshold);
        this.row = row;
        this.column = column;
        this.threshold = threshold;
    }

    /**
     * @return the row index of the entry.
     */
    public int getRow() {
        return row;
    }
    /**
     * @return the column index of the entry.
     */
    public int getColumn() {
        return column;
    }
    /**
     * @return the relative symmetry threshold.
     */
    public double getThreshold() {
        return threshold;
    }
}
