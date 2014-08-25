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
import com.oracle.truffle.r.runtime.*;
import com.oracle.truffle.r.runtime.data.*;

/**
 * Placeholder. {@code on.exit} is special (cf {@code .Internal} in that {@code expr} is not
 * evaluated, but {@code add} is. TODO arrange for the {@code expr} be stored with the currently
 * evaluating function using a new slot in {@link RArguments} and run it on function exit.
 */
@RBuiltin(name = "on.exit", kind = PRIMITIVE, parameterNames = {"expr", "add"}, nonEvalArgs = {0})
public abstract class OnExit extends RInvisibleBuiltinNode {

    @Override
    public RNode[] getParameterValues() {
        return new RNode[]{ConstantNode.create(RNull.instance), ConstantNode.create(false)};
    }

    @Specialization
    protected Object onExit(@SuppressWarnings("unused") RPromise expr, @SuppressWarnings("unused") byte add) {
        controlVisibility();
        RContext.getInstance().setEvalWarning("on.exit ignored");
        return RNull.instance;
    }

    @Specialization
    protected Object onExit(@SuppressWarnings("unused") RNull expr, @SuppressWarnings("unused") byte add) {
        controlVisibility();
        RContext.getInstance().setEvalWarning("on.exit ignored");
        return RNull.instance;
    }

}