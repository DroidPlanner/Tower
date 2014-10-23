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

import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.util.LocalizedFormats;

/**
 * Exception to be thrown when a square matrix is expected.
 *
 * @since 3.0
 * @version $Id: NonSquareMatrixException.java 1244107 2012-02-14 16:17:55Z erans $
 */
public class NonSquareMatrixException extends DimensionMismatchException {
    /** Serializable version Id. */
    private static final long serialVersionUID = -660069396594485772L;

    /**
     * Construct an exception from the mismatched dimensions.
     *
     * @param wrong Row dimension.
     * @param expected Column dimension.
     */
    public NonSquareMatrixException(int wrong,
                                    int expected) {
        super(LocalizedFormats.NON_SQUARE_MATRIX, wrong, expected);
    }
}
