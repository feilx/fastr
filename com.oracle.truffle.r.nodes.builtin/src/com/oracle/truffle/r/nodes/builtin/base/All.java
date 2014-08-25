/*
 * Copyright (c) 2013, 2014, Oracle and/or its affiliates. All rights reserved.
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

import com.oracle.truffle.api.*;
import com.oracle.truffle.api.dsl.*;
import com.oracle.truffle.api.frame.*;
import com.oracle.truffle.r.nodes.builtin.*;
import com.oracle.truffle.r.nodes.unary.*;
import com.oracle.truffle.r.runtime.*;
import com.oracle.truffle.r.runtime.data.*;
import com.oracle.truffle.r.runtime.ops.na.*;

@RBuiltin(name = "all", kind = PRIMITIVE, parameterNames = {"...", "na.rm"})
@SuppressWarnings("unused")
public abstract class All extends RBuiltinNode {

    private final NACheck check = NACheck.create();

    @Child private CastLogicalNode castLogicalNode;

    private byte castLogical(VirtualFrame frame, Object o) {
        if (castLogicalNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            castLogicalNode = insert(CastLogicalNodeFactory.create(null, true, false, false));
        }
        return (byte) castLogicalNode.executeByte(frame, o);
    }

    private RLogicalVector castLogicalVector(VirtualFrame frame, Object o) {
        if (castLogicalNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            castLogicalNode = insert(CastLogicalNodeFactory.create(null, true, false, false));
        }
        return (RLogicalVector) castLogicalNode.executeLogical(frame, o);
    }

    @Specialization
    protected byte all(byte value) {
        controlVisibility();
        return value;
    }

    @Specialization
    protected byte all(int value) {
        controlVisibility();
        check.enable(value);
        return check.convertIntToLogical(value);
    }

    @Specialization
    protected byte all(double value) {
        controlVisibility();
        check.enable(value);
        return check.convertDoubleToLogical(value);
    }

    @Specialization
    protected byte all(RComplex value) {
        controlVisibility();
        check.enable(value);
        return check.convertComplexToLogical(value);
    }

    @Specialization
    protected byte all(VirtualFrame frame, String value) {
        controlVisibility();
        check.enable(value);
        return check.convertStringToLogical(value);
    }

    @Specialization
    protected byte all(RNull vector) {
        controlVisibility();
        return RRuntime.LOGICAL_TRUE;
    }

    @Specialization
    protected byte all(RMissing vector) {
        controlVisibility();
        return RRuntime.LOGICAL_TRUE;
    }

    @Specialization
    protected byte all(RLogicalVector vector) {
        controlVisibility();
        for (int i = 0; i < vector.getLength(); i++) {
            byte b = vector.getDataAt(i);
            if (b != RRuntime.LOGICAL_TRUE) {
                return b;
            }
        }
        return RRuntime.LOGICAL_TRUE;
    }

    @Specialization
    protected byte all(VirtualFrame frame, RIntVector vector) {
        controlVisibility();
        return all(castLogicalVector(frame, vector));
    }

    @Specialization
    protected byte all(VirtualFrame frame, RStringVector vector) {
        controlVisibility();
        return all(castLogicalVector(frame, vector));
    }

    @Specialization
    protected byte all(VirtualFrame frame, RDoubleVector vector) {
        controlVisibility();
        return all(castLogicalVector(frame, vector));
    }

    @Specialization
    protected byte all(VirtualFrame frame, RComplexVector vector) {
        controlVisibility();
        return all(castLogicalVector(frame, vector));
    }

    @Specialization
    protected byte all(VirtualFrame frame, RDoubleSequence sequence) {
        controlVisibility();
        return all(castLogicalVector(frame, sequence));
    }

    @Specialization
    protected byte all(VirtualFrame frame, RIntSequence sequence) {
        controlVisibility();
        return all(castLogicalVector(frame, sequence));
    }

    @Specialization
    protected byte all(VirtualFrame frame, RRawVector vector) {
        controlVisibility();
        return all(castLogicalVector(frame, vector));
    }

    @Specialization
    protected byte all(VirtualFrame frame, RArgsValuesAndNames args) {
        controlVisibility();
        Object[] argValues = args.getValues();
        for (int i = 0; i < argValues.length; i++) {
            byte result;
            if (argValues[i] instanceof RVector || argValues[i] instanceof RSequence) {
                result = all(castLogicalVector(frame, argValues[i]));
            } else {
                result = all(castLogical(frame, argValues[i]));
            }
            if (result != RRuntime.LOGICAL_TRUE) {
                return result;
            }
        }
        return RRuntime.LOGICAL_TRUE;
    }
}