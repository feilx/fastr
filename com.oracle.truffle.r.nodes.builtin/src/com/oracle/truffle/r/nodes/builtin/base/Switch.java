/*
 * This material is distributed under the GNU General Public License
 * Version 2. You may review the terms of this license at
 * http://www.gnu.org/licenses/gpl-2.0.html
 *
 * Copyright (c) 2014, Purdue University
 * Copyright (c) 2014, Oracle and/or its affiliates
 *
 * All rights reserved.
 */

package com.oracle.truffle.r.nodes.builtin.base;

import static com.oracle.truffle.r.runtime.RBuiltinKind.*;

import com.oracle.truffle.api.*;
import com.oracle.truffle.api.dsl.*;
import com.oracle.truffle.api.frame.*;
import com.oracle.truffle.r.nodes.*;
import com.oracle.truffle.r.nodes.access.*;
import com.oracle.truffle.r.nodes.builtin.*;
import com.oracle.truffle.r.nodes.unary.*;
import com.oracle.truffle.r.runtime.*;
import com.oracle.truffle.r.runtime.data.*;
import com.oracle.truffle.r.runtime.data.model.*;

@RBuiltin(name = "switch", kind = PRIMITIVE, parameterNames = {"EXPR", "..."})
public abstract class Switch extends RBuiltinNode {
    @Child private CastIntegerNode castIntNode;

    private boolean isVisible = true;

    @Override
    public RNode[] getParameterValues() {
        return new RNode[]{ConstantNode.create(RMissing.instance), ConstantNode.create(RMissing.instance)};
    }

    public boolean getVisibility() {
        return this.isVisible;
    }

    @Specialization(guards = "isLengthOne")
    protected Object doSwitch(RAbstractStringVector x, RArgsValuesAndNames optionalArgs) {
        controlVisibility();
        Object[] optionalArgValues = optionalArgs.getValues();
        Object currentDefaultValue = null;
        final String xStr = x.getDataAt(0);
        final String[] argNames = this.getSuppliedArgsNames();
        for (int i = 1; i < argNames.length; ++i) {
            final String argName = argNames[i];
            final Object value = optionalArgValues[i - 1];
            if (xStr.equals(argName)) {
                if (value != null) {
                    return returnNonNull(value);
                }
            }
            if (argName == null) {
                if (currentDefaultValue != null) {
                    throw RError.error(getEncapsulatingSourceSection(), RError.Message.DUPLICATE_SWITCH_DEFAULT, currentDefaultValue.toString(), value.toString());
                }
                currentDefaultValue = value;
            }
        }
        if (currentDefaultValue != null) {
            return returnNonNull(currentDefaultValue);
        } else {
            return returnNull();
        }
    }

    @Specialization
    protected Object doSwitch(int x, RArgsValuesAndNames optionalArgs) {
        return doSwitchInt(x, optionalArgs);
    }

    @Specialization
    protected Object doSwitch(VirtualFrame frame, Object x, RArgsValuesAndNames optionalArgs) {
        if (castIntNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            castIntNode = insert(CastIntegerNodeFactory.create(null, false, false, false));
        }
        Object objIndex = castIntNode.executeCast(frame, x);
        if (!(objIndex instanceof Integer)) {
            return returnNull();
        }
        int index = (Integer) objIndex;
        return doSwitchInt(index, optionalArgs);
    }

    @SuppressWarnings("unused")
    @Specialization
    protected Object doSwitch(RMissing x, RMissing optionalArgs) {
        throw RError.error(getEncapsulatingSourceSection(), RError.Message.EXPR_MISSING);
    }

    private Object doSwitchInt(int index, RArgsValuesAndNames optionalArgs) {
        Object[] optionalArgValues = optionalArgs.getValues();
        if (index >= 1 && index <= optionalArgValues.length) {
            Object value = optionalArgValues[index - 1];
            if (value != null) {
                return returnNonNull(value);
            }
            throw RError.error(getEncapsulatingSourceSection(), RError.Message.NO_ALTERNATIVE_IN_SWITCH);
        }
        return returnNull();
    }

    protected boolean isLengthOne(RAbstractStringVector x) {
        return x.getLength() == 1;
    }

    private Object returnNull() {
        this.isVisible = false;
        controlVisibility();
        return RNull.instance;
    }

    private Object returnNonNull(Object value) {
        this.isVisible = true;
        controlVisibility();
        return value;
    }
}