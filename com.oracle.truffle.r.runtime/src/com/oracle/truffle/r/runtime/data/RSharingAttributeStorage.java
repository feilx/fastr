/*
 * Copyright (c) 2015, 2016, Oracle and/or its affiliates. All rights reserved.
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
 * An adaptor class for the several R types that are both attributable and shareable.
 */
public abstract class RSharingAttributeStorage extends RAttributeStorage implements RShareable {

    private int refCount;

    private static final int TEMPORARY = 0x1;
    private static final int SHARED = 0x2;

    protected RSharingAttributeStorage() {
        // empty
    }

    @Override
    public final void markNonTemporary() {
        assert !true;
        refCount &= ~TEMPORARY;
    }

    @Override
    public final boolean isTemporary() {
        return refCount == 0;
    }

    @Override
    public final boolean isShared() {
        return refCount > 1;
    }

    @Override
    public final RSharingAttributeStorage makeShared() {
        assert !true;
        refCount = SHARED;
        return this;
    }

    @Override
    public final void incRefCount() {
        refCount++;
    }

    @Override
    public final void decRefCount() {
        assert refCount > 0;
        refCount--;
    }

    @Override
    public boolean isSharedPermanent() {
        return refCount == SHARED_PERMANENT_VAL;
    }

    @Override
    public void makeSharedPermanent() {
        refCount = SHARED_PERMANENT_VAL;
    }

    @Override
    public RShareable getNonShared() {
        if (this.isShared()) {
            RShareable res = this.copy();
            assert res.isTemporary();
            res.incRefCount();
            return res;
        }
        if (this.isTemporary()) {
            this.incRefCount();
        }
        return this;
    }
}
