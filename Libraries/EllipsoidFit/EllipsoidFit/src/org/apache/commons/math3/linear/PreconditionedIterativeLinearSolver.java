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
import org.apache.commons.math3.exception.MaxCountExceededException;
import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.util.IterationManager;
import org.apache.commons.math3.util.MathUtils;

/**
 * <p>
 * This abstract class defines preconditioned iterative solvers. When A is
 * ill-conditioned, instead of solving system A &middot; x = b directly, it is
 * preferable to solve M<sup>-1</sup> &middot; A &middot; x = M<sup>-1</sup>
 * &middot; b, where M approximates in some way A, while remaining comparatively
 * easier to invert. M (not M<sup>-1</sup>!) is called the
 * <em>preconditionner</em>.
 * </p>
 * <p>
 * Concrete implementations of this abstract class must be provided with
 * M<sup>-1</sup>, the inverse of the preconditioner, as a
 * {@link RealLinearOperator}.
 * </p>
 *
 * @version $Id: PreconditionedIterativeLinearSolver.java 1244107 2012-02-14 16:17:55Z erans $
 * @since 3.0
 */
public abstract class PreconditionedIterativeLinearSolver
    extends IterativeLinearSolver {

    /**
     * Creates a new instance of this class, with default iteration manager.
     *
     * @param maxIterations the maximum number of iterations
     */
    public PreconditionedIterativeLinearSolver(final int maxIterations) {
        super(maxIterations);
    }

    /**
     * Creates a new instance of this class, with custom iteration manager.
     *
     * @param manager the custom iteration manager
     * @throws NullArgumentException if {@code manager} is {@code null}
     */
    public PreconditionedIterativeLinearSolver(final IterationManager manager)
        throws NullArgumentException {
        super(manager);
    }

    /**
     * Returns an estimate of the solution to the linear system A &middot; x =
     * b.
     *
     * @param a the linear operator A of the system
     * @param minv the inverse of the preconditioner, M<sup>-1</sup>
     * (can be {@code null})
     * @param b the right-hand side vector
     * @param x0 the initial guess of the solution
     * @return a new vector containing the solution
     * @throws NullArgumentException if one of the parameters is {@code null}
     * @throws NonSquareOperatorException if {@code a} or {@code minv} is not
     * square
     * @throws DimensionMismatchException if {@code minv}, {@code b} or
     * {@code x0} have dimensions inconsistent with {@code a}
     * @throws MaxCountExceededException at exhaustion of the iteration count,
     * unless a custom
     * {@link org.apache.commons.math3.util.Incrementor.MaxCountExceededCallback callback}
     * has been set at construction
     */
    public RealVector solve(final RealLinearOperator a,
        final RealLinearOperator minv, final RealVector b, final RealVector x0)
        throws NullArgumentException, NonSquareOperatorException,
        DimensionMismatchException, MaxCountExceededException {
        MathUtils.checkNotNull(x0);
        return solveInPlace(a, minv, b, x0.copy());
    }

    /** {@inheritDoc} */
    @Override
    public RealVector solve(final RealLinearOperator a, final RealVector b)
        throws NullArgumentException, NonSquareOperatorException,
        DimensionMismatchException, MaxCountExceededException {
        MathUtils.checkNotNull(a);
        final RealVector x = new ArrayRealVector(a.getColumnDimension());
        x.set(0.);
        return solveInPlace(a, null, b, x);
    }

    /** {@inheritDoc} */
    @Override
    public RealVector solve(final RealLinearOperator a, final RealVector b,
                            final RealVector x0)
        throws NullArgumentException, NonSquareOperatorException,
        DimensionMismatchException, MaxCountExceededException {
        MathUtils.checkNotNull(x0);
        return solveInPlace(a, null, b, x0.copy());
    }

    /**
     * Performs all dimension checks on the parameters of
     * {@link #solve(RealLinearOperator, RealLinearOperator, RealVector, RealVector) solve}
     * and
     * {@link #solveInPlace(RealLinearOperator, RealLinearOperator, RealVector, RealVector) solveInPlace},
     * and throws an exception if one of the checks fails.
     *
     * @param a the linear operator A of the system
     * @param minv the inverse of the preconditioner, M<sup>-1</sup>
     * (can be {@code null})
     * @param b the right-hand side vector
     * @param x0 the initial guess of the solution
     * @throws NullArgumentException if one of the parameters is {@code null}
     * @throws NonSquareOperatorException if {@code a} or {@code minv} is not
     * square
     * @throws DimensionMismatchException if {@code minv}, {@code b} or
     * {@code x0} have dimensions inconsistent with {@code a}
     */
    protected static void checkParameters(final RealLinearOperator a,
        final RealLinearOperator minv, final RealVector b, final RealVector x0)
        throws NullArgumentException, NonSquareOperatorException,
        DimensionMismatchException {
        checkParameters(a, b, x0);
        if (minv != null) {
            if (minv.getColumnDimension() != minv.getRowDimension()) {
                throw new NonSquareOperatorException(minv.getColumnDimension(),
                                                     minv.getRowDimension());
            }
            if (minv.getRowDimension() != a.getRowDimension()) {
                throw new DimensionMismatchException(minv.getRowDimension(),
                                                     a.getRowDimension());
            }
        }
    }

    /**
     * Returns an estimate of the solution to the linear system A &middot; x =
     * b.
     *
     * @param a the linear operator A of the system
     * @param minv the inverse of the preconditioner, M<sup>-1</sup>
     * (can be {@code null})
     * @param b the right-hand side vector
     * @return a new vector containing the solution
     * @throws NullArgumentException if one of the parameters is {@code null}
     * @throws NonSquareOperatorException if {@code a} or {@code minv} is not
     * square
     * @throws DimensionMismatchException if {@code minv} or {@code b} have
     * dimensions inconsistent with {@code a}
     * @throws MaxCountExceededException at exhaustion of the iteration count,
     * unless a custom
     * {@link org.apache.commons.math3.util.Incrementor.MaxCountExceededCallback callback}
     * has been set at construction
     */
    public RealVector solve(RealLinearOperator a, RealLinearOperator minv,
        RealVector b) throws NullArgumentException, NonSquareOperatorException,
        DimensionMismatchException, MaxCountExceededException {
        MathUtils.checkNotNull(a);
        final RealVector x = new ArrayRealVector(a.getColumnDimension());
        return solveInPlace(a, minv, b, x);
    }

    /**
     * Returns an estimate of the solution to the linear system A &middot; x =
     * b. The solution is computed in-place (initial guess is modified).
     *
     * @param a the linear operator A of the system
     * @param minv the inverse of the preconditioner, M<sup>-1</sup>
     * (can be {@code null})
     * @param b the right-hand side vector
     * @param x0 the initial guess of the solution
     * @return a reference to {@code x0} (shallow copy) updated with the
     * solution
     * @throws NullArgumentException if one of the parameters is {@code null}
     * @throws NonSquareOperatorException if {@code a} or {@code minv} is not
     * square
     * @throws DimensionMismatchException if {@code minv}, {@code b} or
     * {@code x0} have dimensions inconsistent with {@code a}
     * @throws MaxCountExceededException at exhaustion of the iteration count,
     * unless a custom
     * {@link org.apache.commons.math3.util.Incrementor.MaxCountExceededCallback callback}
     * has been set at construction.
     */
    public abstract RealVector solveInPlace(RealLinearOperator a,
        RealLinearOperator minv, RealVector b, RealVector x0) throws
        NullArgumentException, NonSquareOperatorException,
        DimensionMismatchException, MaxCountExceededException;

    /** {@inheritDoc} */
    @Override
    public RealVector solveInPlace(final RealLinearOperator a,
        final RealVector b, final RealVector x0) throws
        NullArgumentException, NonSquareOperatorException,
        DimensionMismatchException, MaxCountExceededException {
        return solveInPlace(a, null, b, x0);
    }
}
