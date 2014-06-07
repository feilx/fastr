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
package com.oracle.truffle.r.runtime.ffi.jnr;

import java.lang.invoke.*;
import com.oracle.truffle.r.runtime.ffi.*;

import jnr.invoke.*;

/**
 * An implementation of {@link CRFFI} that uses {@code jnr-invoke}.
 */
public class CRFFI_JNR_Invoke implements CRFFI {

    /**
     * We construct a signature for jnr-invoke based on the types in {@code args}. Everything is an
     * array (call by reference for scalars). As we already loaded the library and looked up the
     * symbol address we don't need to use JNR for that.
     */
    public void invoke(DLL.SymbolInfo symbolInfo, Object[] args) throws Throwable {
        ParameterType[] parameterTypes = new ParameterType[args.length];
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            if (arg instanceof double[]) {
                parameterTypes[i] = ParameterType.array(double[].class, DataDirection.INOUT);
            } else if (arg instanceof int[]) {
                parameterTypes[i] = ParameterType.array(int[].class, DataDirection.INOUT);
            } else {
                assert (false);
            }
        }
        Signature sig = Signature.getSignature(ResultType.primitive(NativeType.VOID, void.class), parameterTypes);

        // We already have up the symbol address
        MethodHandle mh = Native.getMethodHandle(sig, new CodeAddress(symbolInfo.getAddress()));
        mh.invokeWithArguments(args);
    }
}