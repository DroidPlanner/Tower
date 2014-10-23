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
package org.apache.commons.math3.exception;

import org.apache.commons.math3.exception.util.Localizable;

/**
 * Exception to be thrown when the argument is negative.
 *
 * @since 2.2
 * @version $Id$
 */
public class NotPositiveException extends NumberIsTooSmallException {
    /** Serializable version Id. */
    private static final long serialVersionUID = -2250556892093726375L;

    /**
     * Construct the exception.
     *
     * @param value Argument.
     */
    public NotPositiveException(Number value) {
        super(value, 0, true);
    }
    /**
     * Construct the exception with a specific context.
     *
     * @param specific Specific context where the error occurred.
     * @param value Argument.
     */
    public NotPositiveException(Localizable specific,
                                Number value) {
        super(specific, value, 0, true);
    }
}
