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
import com.oracle.truffle.api.frame.*;
import com.oracle.truffle.api.frame.FrameInstance.FrameAccess;
import com.oracle.truffle.r.nodes.*;
import com.oracle.truffle.r.nodes.access.*;
import com.oracle.truffle.r.nodes.builtin.*;
import com.oracle.truffle.r.runtime.*;
import com.oracle.truffle.r.runtime.data.*;
import com.oracle.truffle.r.runtime.data.model.*;
import com.oracle.truffle.r.runtime.env.*;

/**
 * Encapsulates all the builtins related to R environments as nested static classes.
 */
public class EnvFunctions {

    @RBuiltin(name = "as.environment", kind = PRIMITIVE, parameterNames = {"fun"})
    public abstract static class AsEnvironment extends RBuiltinNode {

        @Specialization
        protected REnvironment asEnvironment(REnvironment env) {
            controlVisibility();
            return env;
        }

        @Specialization
        protected REnvironment asEnvironment(double dpos) {
            controlVisibility();
            return asEnvironmentInt((int) dpos);
        }

        @Specialization
        protected REnvironment asEnvironmentInt(int pos) {
            controlVisibility();
            if (pos == -1) {
                Frame callerFrame = Utils.getCallerFrame(FrameAccess.MATERIALIZE);
                if (callerFrame == null) {
                    throw RError.error(getEncapsulatingSourceSection(), RError.Message.NO_ENCLOSING_ENVIRONMENT);
                } else {
                    return REnvironment.frameToEnvironment(callerFrame.materialize());
                }

            }
            String[] searchPath = REnvironment.searchPath();
            if (pos == searchPath.length + 1) {
                // although the empty env does not appear in the result of "search", and it is
                // not accessible by name, GnuR allows it to be accessible by index
                return REnvironment.emptyEnv();
            } else if ((pos <= 0) || (pos > searchPath.length + 1)) {
                throw RError.error(getEncapsulatingSourceSection(), RError.Message.INVALID_ARGUMENT, "pos");
            } else {
                return REnvironment.lookupOnSearchPath(searchPath[pos - 1]);
            }
        }

        @Specialization
        protected REnvironment asEnvironment(RAbstractStringVector nameVec) {
            controlVisibility();
            String name = nameVec.getDataAt(0);
            String[] searchPath = REnvironment.searchPath();
            for (String e : searchPath) {
                if (e.equals(name)) {
                    return REnvironment.lookupOnSearchPath(e);
                }
            }
            throw RError.error(getEncapsulatingSourceSection(), RError.Message.NO_ITEM_NAMED, name);
        }

    }

    @RBuiltin(name = "emptyenv", kind = PRIMITIVE, parameterNames = {})
    public abstract static class EmptyEnv extends RBuiltinNode {

        @Specialization
        protected REnvironment emptyenv() {
            controlVisibility();
            return REnvironment.emptyEnv();
        }
    }

    @RBuiltin(name = "globalenv", kind = PRIMITIVE, parameterNames = {})
    public abstract static class GlobalEnv extends RBuiltinNode {

        @Specialization
        protected Object globalenv() {
            controlVisibility();
            return REnvironment.globalEnv();
        }
    }

    /**
     * Returns the "package:base" environment.
     */
    @RBuiltin(name = "baseenv", kind = PRIMITIVE, parameterNames = {})
    public abstract static class BaseEnv extends RBuiltinNode {

        @Specialization
        protected Object baseenv() {
            controlVisibility();
            return REnvironment.baseEnv();
        }
    }

    @RBuiltin(name = "parent.env", kind = INTERNAL, parameterNames = {"env"})
    public abstract static class ParentEnv extends RBuiltinNode {

        @Specialization
        protected REnvironment parentenv(REnvironment env) {
            controlVisibility();
            if (env == REnvironment.emptyEnv()) {
                throw RError.error(getEncapsulatingSourceSection(), RError.Message.EMPTY_NO_PARENT);
            }
            return env.getParent();
        }

    }

    @RBuiltin(name = "parent.env<-", kind = INTERNAL, parameterNames = {"env", ""})
    // 2nd parameter is "value", but should not be matched to so it's empty
    public abstract static class SetParentEnv extends RBuiltinNode {

        @Specialization
        protected REnvironment setParentenv(REnvironment env, REnvironment parent) {
            controlVisibility();
            if (env == REnvironment.emptyEnv()) {
                throw RError.error(getEncapsulatingSourceSection(), RError.Message.CANNOT_SET_PARENT);
            }
            env.setParent(parent);
            return env;
        }

    }

    @RBuiltin(name = "is.environment", kind = PRIMITIVE, parameterNames = {"x"})
    public abstract static class IsEnvironment extends RBuiltinNode {

        @Specialization
        protected byte isEnvironment(Object env) {
            controlVisibility();
            return env instanceof REnvironment ? RRuntime.LOGICAL_TRUE : RRuntime.LOGICAL_FALSE;
        }
    }

    @RBuiltin(name = "environment", kind = INTERNAL, parameterNames = {"fun"})
    public abstract static class Environment extends RBuiltinNode {

        @Override
        public RNode[] getParameterValues() {
            return new RNode[]{ConstantNode.create(RNull.instance)};
        }

        @Specialization
        protected Object environment(@SuppressWarnings("unused") RNull x) {
            controlVisibility();
            Frame callerFrame = Utils.getCallerFrame(FrameAccess.MATERIALIZE);
            return REnvironment.frameToEnvironment(callerFrame.materialize());
        }

        /**
         * Returns the environment that {@code func} was created in. N.B. In current Truffle we
         * cannot both have a specialization for {@link RFunction} and one for {@link Object}, but
         * an object that is not an {@link RFunction} is legal and must return {@code NULL}.
         */
        @Specialization
        protected Object environment(Object funcArg) {
            controlVisibility();
            if (funcArg instanceof RFunction) {
                RFunction func = (RFunction) funcArg;
                Frame enclosing = func.getEnclosingFrame();
                REnvironment env = RArguments.getEnvironment(enclosing);
                return env == null ? REnvironment.createEnclosingEnvironments(enclosing.materialize()) : env;
            } else {
                // Not an error according to GnuR
                return RNull.instance;
            }
        }

    }

    @RBuiltin(name = "environmentName", kind = INTERNAL, parameterNames = {"fun"})
    public abstract static class EnvironmentName extends RBuiltinNode {

        @Override
        public RNode[] getParameterValues() {
            // fun = NULL
            return new RNode[]{ConstantNode.create(RNull.instance)};
        }

        @Specialization
        protected String environmentName(REnvironment env) {
            controlVisibility();
            return env.getName();
        }

        @Specialization
        protected String environmentName(@SuppressWarnings("unused") Object env) {
            controlVisibility();
            // Not an error according to GnuR
            return "";
        }
    }

    @RBuiltin(name = "new.env", kind = INTERNAL, parameterNames = {"hash", "parent", "size"})
    public abstract static class NewEnv extends RBuiltinNode {
        @Override
        public RNode[] getParameterValues() {
            return new RNode[]{ConstantNode.create(RRuntime.LOGICAL_TRUE), ConstantNode.create(RMissing.instance), ConstantNode.create(29)};
        }

        @Specialization
        @SuppressWarnings("unused")
        protected REnvironment newEnv(VirtualFrame frame, byte hash, RMissing parent, int size) {
            return newEnv(frame, hash, RNull.instance, size);
        }

        @Specialization
        @SuppressWarnings("unused")
        protected REnvironment newEnv(VirtualFrame frame, byte hash, RNull parent, int size) {
            // TODO this will eventually go away when R code fixed when promises available
            controlVisibility();
            // FIXME what if hash == FALSE?
            return new REnvironment.NewEnv(REnvironment.frameToEnvironment(frame.materialize()), size);
        }

        @Specialization
        protected REnvironment newEnv(@SuppressWarnings("unused") VirtualFrame frame, @SuppressWarnings("unused") byte hash, REnvironment parent, int size) {
            controlVisibility();
            // FIXME what if hash == FALSE?
            return new REnvironment.NewEnv(parent, size);
        }
    }

    @RBuiltin(name = "search", kind = SUBSTITUTE, parameterNames = {})
    // TODO INTERNAL
    public abstract static class Search extends RBuiltinNode {
        @Specialization
        protected RStringVector search() {
            return RDataFactory.createStringVector(REnvironment.searchPath(), RDataFactory.COMPLETE_VECTOR);
        }
    }

    @RBuiltin(name = "lockEnvironment", kind = INTERNAL, parameterNames = {"env", "bindings"})
    public abstract static class LockEnvironment extends RInvisibleBuiltinNode {

        @Override
        public RNode[] getParameterValues() {
            // env, bindings = FALSE
            return new RNode[]{ConstantNode.create(RMissing.instance), ConstantNode.create(RRuntime.LOGICAL_FALSE)};
        }

        @Specialization
        protected Object lockEnvironment(REnvironment env, byte bindings) {
            controlVisibility();
            env.lock(bindings == RRuntime.LOGICAL_TRUE);
            return RNull.instance;
        }

    }

    @RBuiltin(name = "environmentIsLocked", kind = INTERNAL, parameterNames = {"env"})
    public abstract static class EnvironmentIsLocked extends RBuiltinNode {
        @Specialization
        protected Object lockEnvironment(REnvironment env) {
            controlVisibility();
            return RDataFactory.createLogicalVectorFromScalar(env.isLocked());
        }

    }

    @RBuiltin(name = "lockBinding", kind = INTERNAL, parameterNames = {"sym", "env"})
    public abstract static class LockBinding extends RInvisibleBuiltinNode {
        @Specialization
        protected Object lockBinding(String sym, REnvironment env) {
            controlVisibility();
            env.lockBinding(sym);
            return RNull.instance;
        }

    }

    @RBuiltin(name = "unlockBinding", kind = INTERNAL, parameterNames = {"sym", "env"})
    public abstract static class UnlockBinding extends RInvisibleBuiltinNode {
        @Specialization
        protected Object unlockBinding(String sym, REnvironment env) {
            controlVisibility();
            env.unlockBinding(sym);
            return RNull.instance;
        }

    }

    @RBuiltin(name = "bindingIsLocked", kind = INTERNAL, parameterNames = {"sym", "env"})
    public abstract static class BindingIsLocked extends RBuiltinNode {
        @Specialization
        protected Object bindingIsLocked(String sym, REnvironment env) {
            controlVisibility();
            return RDataFactory.createLogicalVectorFromScalar(env.bindingIsLocked(sym));
        }

    }

    @RBuiltin(name = "makeActiveBinding", kind = INTERNAL, parameterNames = {"sym", "fun", "env"})
    public abstract static class MakeActiveBinding extends RInvisibleBuiltinNode {
        @SuppressWarnings("unused")
        @Specialization
        protected Object makeActiveBinding(Object sym, Object fun, Object env) {
            // TODO implement
            controlVisibility();
            throw RError.nyi(getEncapsulatingSourceSection(), "makeActiveBinding not implemented");
        }
    }

    @RBuiltin(name = "bindingIsActive", kind = INTERNAL, parameterNames = {"sym", "env"})
    public abstract static class BindingIsActive extends RInvisibleBuiltinNode {
        @SuppressWarnings("unused")
        @Specialization
        protected Object bindingIsActive(Object sym, Object fun, Object env) {
            // TODO implement
            controlVisibility();
            return RDataFactory.createLogicalVectorFromScalar(false);
        }
    }

}