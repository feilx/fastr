/*
 * Copyright (c) 2013, 2016, Oracle and/or its affiliates. All rights reserved.
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
package com.oracle.truffle.r.nodes.builtin;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.dsl.CreateCast;
import com.oracle.truffle.api.dsl.GeneratedBy;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.r.nodes.access.AccessArgumentNode;
import com.oracle.truffle.r.nodes.function.FormalArguments;
import com.oracle.truffle.r.nodes.function.RCallNode;
import com.oracle.truffle.r.nodes.unary.ApplyCastNode;
import com.oracle.truffle.r.nodes.unary.CastNode;
import com.oracle.truffle.r.runtime.RDispatch;
import com.oracle.truffle.r.runtime.builtins.RBuiltin;
import com.oracle.truffle.r.runtime.builtins.RBuiltinDescriptor;
import com.oracle.truffle.r.runtime.builtins.RBuiltinKind;
import com.oracle.truffle.r.runtime.data.RArgsValuesAndNames;
import com.oracle.truffle.r.runtime.data.RMissing;
import com.oracle.truffle.r.runtime.data.RNull;
import com.oracle.truffle.r.runtime.env.frame.FrameSlotChangeMonitor;
import com.oracle.truffle.r.runtime.nodes.RBaseNode;
import com.oracle.truffle.r.runtime.nodes.RNode;
import com.oracle.truffle.r.runtime.nodes.RSyntaxNode;

@NodeChild(value = "arguments", type = RNode[].class)
public abstract class RBuiltinNode extends RNode {

    public abstract Object executeBuiltin(VirtualFrame frame, Object... args);

    protected void createCasts(@SuppressWarnings("unused") CastBuilder casts) {
        // nothing to do
    }

    public CastNode[] getCasts() {
        CastBuilder builder = new CastBuilder(getRBuiltin());
        createCasts(builder);
        return builder.getCasts();
    }

    @CreateCast("arguments")
    protected RNode[] castArguments(RNode[] arguments) {
        CastNode[] casts = getCasts();
        if (casts.length == 0) {
            return arguments;
        }
        RNode[] castArguments = arguments.clone();
        for (int i = 0; i < casts.length; i++) {
            if (casts[i] != null) {
                castArguments[i] = new ApplyCastNode(casts[i], castArguments[i]);
            }
        }
        return castArguments;
    }

    /**
     * Return the default values of the builtin's formal arguments. This is only valid for builtins
     * of {@link RBuiltinKind kind} PRIMITIVE or SUBSTITUTE. Only simple scalar constants and
     * {@link RMissing#instance}, {@link RNull#instance} and {@link RArgsValuesAndNames#EMPTY} are
     * allowed.
     */
    public Object[] getDefaultParameterValues() {
        return EMPTY_OBJECT_ARRAY;
    }

    private static RNode[] createAccessArgumentsNodes(RBuiltinDescriptor builtin) {
        int total = builtin.getSignature().getLength();
        RNode[] args = new RNode[total];
        for (int i = 0; i < total; i++) {
            args[i] = AccessArgumentNode.create(i);
        }
        return args;
    }

    static RootCallTarget createArgumentsCallTarget(RBuiltinFactory builtin) {
        CompilerAsserts.neverPartOfCompilation();

        // Create function initialization
        RBuiltinNode node;
        FormalArguments formals;
        if (builtin.getDispatch() == RDispatch.SPECIAL) {
            node = null;
            formals = FormalArguments.createForBuiltin(EMPTY_OBJECT_ARRAY, builtin.getSignature());
        } else {
            RNode[] argAccessNodes = createAccessArgumentsNodes(builtin);
            node = builtin.getConstructor().apply(argAccessNodes.clone());
            assert builtin.getKind() != RBuiltinKind.INTERNAL || node.getDefaultParameterValues().length == 0 : "INTERNAL builtins do not need default values";
            formals = FormalArguments.createForBuiltin(node.getDefaultParameterValues(), builtin.getSignature());
            for (RNode access : argAccessNodes) {
                ((AccessArgumentNode) access).setFormals(formals);
            }
        }

        // Setup
        FrameDescriptor frameDescriptor = new FrameDescriptor();
        RBuiltinRootNode root = new RBuiltinRootNode(builtin, node, formals, frameDescriptor, null);
        FrameSlotChangeMonitor.initializeFunctionFrameDescriptor(builtin.getName(), frameDescriptor);
        return Truffle.getRuntime().createCallTarget(root);
    }

    public static final RBuiltinNode inline(RBuiltinDescriptor factory, RNode[] args) {
        // static number of arguments
        return ((RBuiltinFactory) factory).getConstructor().apply(args);
    }

    protected final RBuiltin getRBuiltin() {
        return getRBuiltin(getClass());
    }

    private static RBuiltin getRBuiltin(Class<?> klass) {
        GeneratedBy generatedBy = klass.getAnnotation(GeneratedBy.class);
        if (generatedBy != null) {
            return generatedBy.value().getAnnotation(RBuiltin.class);
        } else {
            return null;
        }
    }

    /**
     * Generally, {@link RBuiltinNode} instances are created as child nodes of a private class in
     * {@link RCallNode} that can return the original {@link RCallNode} which has all the pertinent
     * information as initially parsed. However, currently, builtins called via
     * {@code do.call("func", )} have a {@link RBuiltinRootNode} as a parent, which carries no
     * context about the original call, so we return {@code null}.
     */
    public RSyntaxNode getOriginalCall() {
        Node p = getParent();
        if (p instanceof RBuiltinRootNode) {
            return null;
        } else {
            return ((RBaseNode) getParent()).asRSyntaxNode();
        }
    }

    @Override
    public String toString() {
        return (getRBuiltin() == null ? getClass().getSimpleName() : getRBuiltin().name());
    }
}
