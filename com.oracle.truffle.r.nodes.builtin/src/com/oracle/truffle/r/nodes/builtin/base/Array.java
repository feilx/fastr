/*
 * Copyright (c) 2014, 2014, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package com.oracle.truffle.r.nodes.builtin.base;

import static com.oracle.truffle.r.runtime.RBuiltinKind.*;

import com.oracle.truffle.api.dsl.*;
import com.oracle.truffle.r.nodes.*;
import com.oracle.truffle.r.nodes.access.*;
import com.oracle.truffle.r.nodes.builtin.*;
import com.oracle.truffle.r.nodes.unary.*;
import com.oracle.truffle.r.runtime.*;
import com.oracle.truffle.r.runtime.data.*;
import com.oracle.truffle.r.runtime.data.model.*;

/**
 * The {@code} .Internal part of the {@code array} function. The R code may alter the arguments
 * before calling {@code .Internal}.
 *
 * <pre>
 * array <- function(data = NA, dim = length(data), dimnames = NULL) { .Internal.array(data, dim, dimnames) }
 * </pre>
 *
 * TODO complete. This is sufficient for the b25 benchmark use.
 */
@RBuiltin(name = "array", kind = INTERNAL, parameterNames = {"data", "dim", "dimnames"})
public abstract class Array extends RBuiltinNode {

    @Override
    public RNode[] getParameterValues() {
        // TODO What is the correct NA value for "data"???
        // is inserted as cast, see below in "createCastDimensions"
        return new RNode[]{ConstantNode.create(RRuntime.NA_HEADER), null, ConstantNode.create(RNull.instance)};
    }

    @CreateCast({"arguments"})
    public RNode[] createCastDimensions(RNode[] children) {
        RNode dimsVector = CastToVectorNodeFactory.create(children[1], false, false, false, false);
        return new RNode[]{children[0], CastIntegerNodeFactory.create(dimsVector, false, false, false), children[2]};
    }

    private int dimDataHelper(RAbstractIntVector dim, int[] dimData) {
        int totalLength = 1;
        int seenNegative = 0;
        for (int i = 0; i < dim.getLength(); i++) {
            dimData[i] = dim.getDataAt(i);
            if (dimData[i] < 0) {
                seenNegative++;
            }
            totalLength *= dimData[i];
        }
        if (seenNegative == dim.getLength() && seenNegative != 0) {
            throw RError.error(getEncapsulatingSourceSection(), RError.Message.DIMS_CONTAIN_NEGATIVE_VALUES);
        } else if (seenNegative > 0) {
            throw RError.error(getEncapsulatingSourceSection(), RError.Message.NEGATIVE_LENGTH_VECTORS_NOT_ALLOWED);
        }
        return totalLength;
    }

    private RIntVector doArrayInt(RAbstractIntVector vec, RAbstractIntVector dim) {
        int[] dimData = new int[dim.getLength()];
        int totalLength = dimDataHelper(dim, dimData);
        int[] data = new int[totalLength];
        for (int i = 0; i < totalLength; i++) {
            data[i] = vec.getDataAt(i % vec.getLength());
        }
        return RDataFactory.createIntVector(data, vec.isComplete(), dimData);
    }

    @Specialization
    protected RIntVector doArrayNoDimNames(RAbstractIntVector vec, RAbstractIntVector dim, @SuppressWarnings("unused") RNull dimnames) {
        controlVisibility();
        return doArrayInt(vec, dim);
    }

    @Specialization
    protected RIntVector doArray(RAbstractIntVector vec, RAbstractIntVector dim, RList dimnames) {
        controlVisibility();
        RIntVector ret = doArrayInt(vec, dim);
        ret.setDimNames(dimnames, getEncapsulatingSourceSection());
        return ret;
    }

    private RDoubleVector doArrayDouble(RAbstractDoubleVector vec, RAbstractIntVector dim) {
        int[] dimData = new int[dim.getLength()];
        int totalLength = dimDataHelper(dim, dimData);
        double[] data = new double[totalLength];
        for (int i = 0; i < totalLength; i++) {
            data[i] = vec.getDataAt(i % vec.getLength());
        }
        return RDataFactory.createDoubleVector(data, vec.isComplete(), dimData);
    }

    @Specialization
    protected RDoubleVector doArrayNoDimNames(RAbstractDoubleVector vec, RAbstractIntVector dim, @SuppressWarnings("unused") RNull dimnames) {
        controlVisibility();
        return doArrayDouble(vec, dim);
    }

    @Specialization
    protected RDoubleVector doArray(RAbstractDoubleVector vec, RAbstractIntVector dim, RList dimnames) {
        controlVisibility();
        RDoubleVector ret = doArrayDouble(vec, dim);
        ret.setDimNames(dimnames, getEncapsulatingSourceSection());
        return ret;
    }

    private RLogicalVector doArrayLogical(RAbstractLogicalVector vec, RAbstractIntVector dim) {
        int[] dimData = new int[dim.getLength()];
        int totalLength = dimDataHelper(dim, dimData);
        byte[] data = new byte[totalLength];
        for (int i = 0; i < totalLength; i++) {
            data[i] = vec.getDataAt(i % vec.getLength());
        }
        return RDataFactory.createLogicalVector(data, vec.isComplete(), dimData);
    }

    @Specialization
    protected RLogicalVector doArrayNoDimNames(RAbstractLogicalVector vec, RAbstractIntVector dim, @SuppressWarnings("unused") RNull dimnames) {
        controlVisibility();
        return doArrayLogical(vec, dim);
    }

    @Specialization
    protected RLogicalVector doArray(RAbstractLogicalVector vec, RAbstractIntVector dim, RList dimnames) {
        controlVisibility();
        RLogicalVector ret = doArrayLogical(vec, dim);
        ret.setDimNames(dimnames, getEncapsulatingSourceSection());
        return ret;
    }

    private RStringVector doArrayString(RAbstractStringVector vec, RAbstractIntVector dim) {
        int[] dimData = new int[dim.getLength()];
        int totalLength = dimDataHelper(dim, dimData);
        String[] data = new String[totalLength];
        for (int i = 0; i < totalLength; i++) {
            data[i] = vec.getDataAt(i % vec.getLength());
        }
        return RDataFactory.createStringVector(data, vec.isComplete(), dimData);
    }

    @Specialization
    protected RStringVector doArrayNoDimNames(RAbstractStringVector vec, RAbstractIntVector dim, @SuppressWarnings("unused") RNull dimnames) {
        controlVisibility();
        return doArrayString(vec, dim);
    }

    @Specialization
    protected RStringVector doArray(RAbstractStringVector vec, RAbstractIntVector dim, RList dimnames) {
        controlVisibility();
        RStringVector ret = doArrayString(vec, dim);
        ret.setDimNames(dimnames, getEncapsulatingSourceSection());
        return ret;
    }

    private RComplexVector doArrayComplex(RAbstractComplexVector vec, RAbstractIntVector dim) {
        int[] dimData = new int[dim.getLength()];
        int totalLength = dimDataHelper(dim, dimData);
        double[] data = new double[totalLength << 1];
        int ind = 0;
        for (int i = 0; i < totalLength; i++) {
            RComplex d = vec.getDataAt(i % vec.getLength());
            data[ind++] = d.getRealPart();
            data[ind++] = d.getImaginaryPart();
        }
        return RDataFactory.createComplexVector(data, vec.isComplete(), dimData);
    }

    @Specialization
    protected RComplexVector doArrayNoDimNames(RAbstractComplexVector vec, RAbstractIntVector dim, @SuppressWarnings("unused") RNull dimnames) {
        controlVisibility();
        return doArrayComplex(vec, dim);
    }

    @Specialization
    protected RComplexVector doArray(RAbstractComplexVector vec, RAbstractIntVector dim, RList dimnames) {
        controlVisibility();
        RComplexVector ret = doArrayComplex(vec, dim);
        ret.setDimNames(dimnames, getEncapsulatingSourceSection());
        return ret;
    }

    private RRawVector doArrayRaw(RAbstractRawVector vec, RAbstractIntVector dim) {
        int[] dimData = new int[dim.getLength()];
        int totalLength = dimDataHelper(dim, dimData);
        byte[] data = new byte[totalLength];
        for (int i = 0; i < totalLength; i++) {
            data[i] = vec.getDataAt(i % vec.getLength()).getValue();
        }
        return RDataFactory.createRawVector(data, dimData);
    }

    @Specialization
    protected RRawVector doArrayNoDimNames(RAbstractRawVector vec, RAbstractIntVector dim, @SuppressWarnings("unused") RNull dimnames) {
        controlVisibility();
        return doArrayRaw(vec, dim);
    }

    @Specialization
    protected RRawVector doArray(RAbstractRawVector vec, RAbstractIntVector dim, RList dimnames) {
        controlVisibility();
        RRawVector ret = doArrayRaw(vec, dim);
        ret.setDimNames(dimnames, getEncapsulatingSourceSection());
        return ret;
    }

    private RList doArrayList(RList vec, RAbstractIntVector dim) {
        int[] dimData = new int[dim.getLength()];
        int totalLength = dimDataHelper(dim, dimData);
        Object[] data = new Object[totalLength];
        for (int i = 0; i < totalLength; i++) {
            data[i] = vec.getDataAt(i % vec.getLength());
        }
        return RDataFactory.createList(data, dimData);

    }

    @Specialization
    protected RList doArrayNoDimeNames(RList vec, RAbstractIntVector dim, @SuppressWarnings("unused") RNull dimnames) {
        controlVisibility();
        return doArrayList(vec, dim);
    }

    @Specialization
    protected RList doArray(RList vec, RAbstractIntVector dim, RList dimnames) {
        controlVisibility();
        RList ret = doArrayList(vec, dim);
        ret.setDimNames(dimnames, getEncapsulatingSourceSection());
        return ret;
    }
}