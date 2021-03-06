/*
 * Copyright (c) 2014, 2016, Oracle and/or its affiliates. All rights reserved.
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

import static com.oracle.truffle.r.nodes.builtin.CastBuilder.Predef.stringValue;
import static com.oracle.truffle.r.runtime.builtins.RBehavior.PURE;
import static com.oracle.truffle.r.runtime.builtins.RBuiltinKind.PRIMITIVE;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.r.nodes.RASTUtils;
import com.oracle.truffle.r.nodes.access.variables.ReadVariableNode;
import com.oracle.truffle.r.nodes.builtin.CastBuilder;
import com.oracle.truffle.r.nodes.builtin.RBuiltinNode;
import com.oracle.truffle.r.runtime.ArgumentsSignature;
import com.oracle.truffle.r.runtime.RError;
import com.oracle.truffle.r.runtime.builtins.RBuiltin;
import com.oracle.truffle.r.runtime.data.RArgsValuesAndNames;
import com.oracle.truffle.r.runtime.data.RDataFactory;
import com.oracle.truffle.r.runtime.data.RFunction;
import com.oracle.truffle.r.runtime.data.RLanguage;
import com.oracle.truffle.r.runtime.data.RMissing;
import com.oracle.truffle.r.runtime.nodes.RSyntaxNode;

/**
 * Construct a call object ({@link RLanguage}) from a name and optional arguments.
 *
 * Does not perform argument matching for first parameter "name".
 */
@RBuiltin(name = "call", kind = PRIMITIVE, parameterNames = {"", "..."}, behavior = PURE)
public abstract class Call extends RBuiltinNode {

    @Override
    public Object[] getDefaultParameterValues() {
        return new Object[]{RMissing.instance, RArgsValuesAndNames.EMPTY};
    }

    @Override
    protected void createCasts(CastBuilder casts) {
        casts.arg("").mustBe(stringValue(), RError.Message.FIRST_ARG_MUST_BE_STRING).asStringVector().findFirst();
    }

    @Specialization
    protected RLanguage call(String name, RArgsValuesAndNames args) {
        return makeCall(name, args);
    }

    @TruffleBoundary
    private static RLanguage makeCall(String name, RArgsValuesAndNames args) {
        return makeCall0(ReadVariableNode.createFunctionLookup(RSyntaxNode.EAGER_DEPARSE, name), false, args);
    }

    @TruffleBoundary
    protected static RLanguage makeCallSourceUnavailable(String name, RArgsValuesAndNames args) {
        return makeCall0(ReadVariableNode.createFunctionLookup(RSyntaxNode.EAGER_DEPARSE, name), true, args);
    }

    @TruffleBoundary
    protected static RLanguage makeCallSourceUnavailable(RFunction function, RArgsValuesAndNames args) {
        return makeCall0(function, true, args);
    }

    /**
     *
     * @param fn an {@link RFunction} or {@link String}
     * @param argsAndNames if not {@code null} the argument values and (optional) names
     * @return the {@link RLanguage} instance denoting the call
     */
    @TruffleBoundary
    private static RLanguage makeCall0(Object fn, boolean sourceUnavailable, RArgsValuesAndNames argsAndNames) {
        assert !(fn instanceof String);
        int argLength = argsAndNames == null ? 0 : argsAndNames.getLength();
        RSyntaxNode[] args = new RSyntaxNode[argLength];
        Object[] values = argsAndNames == null ? null : argsAndNames.getArguments();
        ArgumentsSignature signature = argsAndNames == null ? ArgumentsSignature.empty(0) : argsAndNames.getSignature();

        for (int i = 0; i < argLength; i++) {
            args[i] = (RSyntaxNode) RASTUtils.createNodeForValue(values[i]);
        }

        return RDataFactory.createLanguage(RASTUtils.createCall(fn, sourceUnavailable, signature, args).asRNode());
    }
}
