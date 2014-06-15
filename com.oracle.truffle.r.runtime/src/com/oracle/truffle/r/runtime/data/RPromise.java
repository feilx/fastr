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
package com.oracle.truffle.r.runtime.data;

/**
 * Denotes an R {@code promise}. It extends {@link RLanguageRep} with a (lazily) evaluated value.
 */
@com.oracle.truffle.api.CompilerDirectives.ValueType
public class RPromise extends RLanguageRep {
    /**
     * Denotes a promise that raised an error during evaluation.
     */
    private static Object ERROR = new Object();

    private Object value;

    /**
     * Create the promise with a representation that allow evaluation later.
     */
    public RPromise(Object rep) {
        super(rep);
    }

    /**
     * This is a workaround for the fact that REngine can't be called from here (at the moment),
     * otherwise the evaluation would be implicitly done in {@link #getValue}.
     */
    public Object setValue(Object newValue) {
        if (value == null) {
            if (newValue == null) {
                this.value = ERROR;
            } else {
                this.value = newValue;
            }
        } else {
            assert false : "promise already has a value";
        }
        return this.value;
    }

    public Object getValue() {
        return value;
    }

    /**
     * Returns {@code true} if this promise has been evaluated?
     */
    public boolean hasValue() {
        return value != null;
    }
}
