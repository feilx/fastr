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
package com.oracle.truffle.r.nodes.builtin.casts;

import com.oracle.truffle.api.CompilerDirectives.ValueType;
import com.oracle.truffle.r.runtime.RError;
import com.oracle.truffle.r.runtime.RError.Message;
import com.oracle.truffle.r.runtime.nodes.RBaseNode;

/**
 * Value type that holds data necessary for error/warning message from a cast pipeline.
 */
@ValueType
public final class MessageData {
    private final RBaseNode callObj;
    private final RError.Message message;
    private final Object[] messageArgs;

    public MessageData(RBaseNode callObj, Message message, Object... messageArgs) {
        this.callObj = callObj;
        this.message = message;
        this.messageArgs = messageArgs;
    }

    public RBaseNode getCallObj() {
        return callObj;
    }

    public Message getMessage() {
        return message;
    }

    public Object[] getMessageArgs() {
        return messageArgs;
    }

    public MessageData fixCallObj(RBaseNode callObjFix) {
        if (callObj == null) {
            return new MessageData(callObjFix, message, messageArgs);
        } else {
            return this;
        }
    }

    /**
     * Helper method for operation that is often performed with {@link MessageData}.
     */
    public static MessageData getFirstNonNull(MessageData first, MessageData second, MessageData third) {
        assert third != null : "at least the last one must not be null";
        return first != null && first.getMessage() != null ? first : second != null && second.getMessage() != null ? second : third;
    }
}
