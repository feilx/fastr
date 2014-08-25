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

import com.oracle.truffle.api.dsl.*;
import com.oracle.truffle.r.nodes.*;
import com.oracle.truffle.r.nodes.access.*;
import com.oracle.truffle.r.runtime.*;
import com.oracle.truffle.r.runtime.data.*;

@RBuiltin(name = "cor", kind = SUBSTITUTE, parameterNames = {"x", "y", "use", "method"})
public abstract class Cor extends Covcor {

    @Override
    public RNode[] getParameterValues() {
        // x, y = NULL, use = "everything", method = c("pearson", "kendall", "spearman")
        return new RNode[]{ConstantNode.create(RMissing.instance), ConstantNode.create(RNull.instance), ConstantNode.create("everything"),
                        ConstantNode.create(RDataFactory.createStringVector(new String[]{"pearson", "kendall", "spearman"}, true))};
    }

    @Specialization
    protected RDoubleVector dimWithDimensions(RDoubleVector vector1, RDoubleVector vector2, @SuppressWarnings("unused") String use, @SuppressWarnings("unused") RStringVector method) {
        controlVisibility();
        return corcov(vector1, vector2, false, true);
    }

    @Specialization
    @SuppressWarnings("unused")
    protected RDoubleVector dimWithDimensions(RDoubleVector vector1, RMissing vector2, String use, RStringVector method) {
        controlVisibility();
        return corcov(vector1, null, false, true);
    }

    @Specialization
    @SuppressWarnings("unused")
    protected RDoubleVector dimWithDimensions(RDoubleVector vector1, RNull vector2, String use, RStringVector method) {
        controlVisibility();
        return corcov(vector1, null, false, true);
    }
}