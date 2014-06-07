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

import com.oracle.truffle.api.*;
import com.oracle.truffle.api.dsl.*;
import com.oracle.truffle.r.nodes.*;
import com.oracle.truffle.r.nodes.access.*;
import com.oracle.truffle.r.nodes.builtin.*;
import com.oracle.truffle.r.runtime.*;
import com.oracle.truffle.r.runtime.data.*;
import com.oracle.truffle.r.runtime.ffi.*;
import com.oracle.truffle.r.runtime.ffi.DLL.*;

/**
 * {@code .C} and {.Fortran} functions.
 *
 * TODO Completeness (more types, more error checks), Performance (copying). Especially all the
 * subtleties around copying. See <a
 * href="https://stat.ethz.ch/R-manual/R-devel/library/base/html/Foreign.html">here</a>.
 */
public class ForeignFunctions {
    public abstract static class Adapter extends RBuiltinNode {
        private static final Object[] PARAMETER_NAMES = new Object[]{".NAME", "...", "NAOK", "DUP", "PACKAGE", "ENCODING"};

        public abstract String[] getArgNames();

        @Override
        public Object[] getParameterNames() {
            return PARAMETER_NAMES;
        }

        @Override
        public RNode[] getParameterValues() {
            return new RNode[]{ConstantNode.create(RMissing.instance), ConstantNode.create(EMPTY_OBJECT_ARRAY), ConstantNode.create(RRuntime.LOGICAL_FALSE),
                            ConstantNode.create(RRuntime.LOGICAL_FALSE), ConstantNode.create(RMissing.instance), ConstantNode.create(RMissing.instance)};
        }

        protected int[] checkNAs(int argIndex, int[] data) throws RError {
            for (int i = 0; i < data.length; i++) {
                if (RRuntime.isNA(data[i])) {
                    CompilerDirectives.transferToInterpreter();
                    throw RError.getGenericError(getEncapsulatingSourceSection(), "NAs in foreign function call (arg " + argIndex + ")");
                }
            }
            return data;
        }

        protected double[] checkNAs(int argIndex, double[] data) throws RError {
            for (int i = 0; i < data.length; i++) {
                if (!RRuntime.isFinite(data[i])) {
                    CompilerDirectives.transferToInterpreter();
                    throw RError.getGenericError(getEncapsulatingSourceSection(), "NA/NaN/Inf in foreign function call (arg " + argIndex + ")");
                }
            }
            return data;
        }
    }

    /**
     * For now, just some special case functions that are built in to the implementation.
     */
    @RBuiltin(name = ".Fortran", kind = RBuiltinKind.PRIMITIVE, isCombine = true)
    @NodeField(name = "argNames", type = String[].class)
    public abstract static class Fortran extends Adapter {
        private static final String E = RRuntime.NAMES_ATTR_EMPTY_VALUE;
        private static final RStringVector DQRDC2_NAMES = RDataFactory.createStringVector(new String[]{"qr", E, E, E, E, "rank", "qraux", "pivot", E}, RDataFactory.COMPLETE_VECTOR);

        @SuppressWarnings("unused")
        @Specialization(order = 0, guards = "dqrdc2")
        public RList fortranDqrdc2(String f, Object[] args, byte naok, byte dup, RMissing rPackage, RMissing encoding) {
            controlVisibility();
            try {
                RDoubleVector xVec = (RDoubleVector) args[0];
                int ldx = (int) args[1];
                int n = (int) args[2];
                int p = (int) args[3];
                double tol = (double) args[4];
                RIntVector rankVec = (RIntVector) args[5];
                RDoubleVector qrauxVec = (RDoubleVector) args[6];
                RIntVector pivotVec = (RIntVector) args[7];
                RDoubleVector workVec = (RDoubleVector) args[8];
                double[] x = xVec.getDataCopy();
                int[] rank = rankVec.getDataCopy();
                double[] qraux = qrauxVec.getDataCopy();
                int[] pivot = pivotVec.getDataCopy();
                RFFIFactory.getRFFI().getLinpackRFFI().dqrdc2(x, ldx, n, p, tol, rank, qraux, pivot, workVec.getDataCopy());
                // @formatter:off
                Object[] data = new Object[]{
                            RDataFactory.createDoubleVector(x, RDataFactory.COMPLETE_VECTOR, xVec.getDimensions()),
                            args[1], args[2], args[3], args[4],
                            RDataFactory.createIntVector(rank, RDataFactory.COMPLETE_VECTOR),
                            RDataFactory.createDoubleVector(qraux, RDataFactory.COMPLETE_VECTOR),
                            RDataFactory.createIntVector(pivot, RDataFactory.COMPLETE_VECTOR),
                            args[8]
                };
                // @formatter:on
                return RDataFactory.createList(data, DQRDC2_NAMES);
            } catch (ClassCastException | ArrayIndexOutOfBoundsException ex) {
                CompilerDirectives.transferToInterpreter();
                throw RError.getGenericError(getEncapsulatingSourceSection(), "incorrect arguments to dqrdc2");
            }
        }

        public boolean dqrdc2(String f) {
            return f.equals("dqrdc2");
        }

        private static final RStringVector DQRCF_NAMES = RDataFactory.createStringVector(new String[]{E, E, E, E, E, E, "coef", "info"}, RDataFactory.COMPLETE_VECTOR);

        @SuppressWarnings("unused")
        @Specialization(order = 1, guards = "dqrcf")
        public RList fortranDqrcf(String f, Object[] args, byte naok, byte dup, RMissing rPackage, RMissing encoding) {
            controlVisibility();
            try {
                RDoubleVector xVec = (RDoubleVector) args[0];
                int n = (int) args[1];
                RIntVector k = (RIntVector) args[2];
                RDoubleVector qrauxVec = (RDoubleVector) args[3];
                RDoubleVector yVec = (RDoubleVector) args[4];
                int ny = (int) args[5];
                RDoubleVector bVec = (RDoubleVector) args[6];
                RIntVector infoVec = (RIntVector) args[7];
                double[] x = xVec.getDataCopy();
                double[] qraux = qrauxVec.getDataCopy();
                double[] y = yVec.getDataCopy();
                double[] b = bVec.getDataCopy();
                int[] info = infoVec.getDataCopy();
                RFFIFactory.getRFFI().getLinpackRFFI().dqrcf(x, n, k.getDataAt(0), qraux, y, ny, b, info);
                RDoubleVector coef = RDataFactory.createDoubleVector(b, RDataFactory.COMPLETE_VECTOR);
                coef.copyAttributesFrom(bVec);
                // @formatter:off
                Object[] data = new Object[]{
                            RDataFactory.createDoubleVector(x, RDataFactory.COMPLETE_VECTOR),
                            args[1],
                            k.copy(),
                            RDataFactory.createDoubleVector(qraux, RDataFactory.COMPLETE_VECTOR),
                            RDataFactory.createDoubleVector(y, RDataFactory.COMPLETE_VECTOR),
                            args[5],
                            coef,
                            RDataFactory.createIntVector(info, RDataFactory.COMPLETE_VECTOR),
                };
            // @formatter:on
                return RDataFactory.createList(data, DQRCF_NAMES);

            } catch (ClassCastException | ArrayIndexOutOfBoundsException ex) {
                CompilerDirectives.transferToInterpreter();
                throw RError.getGenericError(getEncapsulatingSourceSection(), "incorrect arguments to dqrcf");
            }
        }

        public boolean dqrcf(String f) {
            return f.equals("dqrcf");
        }

    }

    @RBuiltin(name = ".C", kind = RBuiltinKind.PRIMITIVE, isCombine = true)
    @NodeField(name = "argNames", type = String[].class)
    public abstract static class C extends Adapter {

        private static final int SCALAR_DOUBLE = 0;
        private static final int SCALAR_INT = 1;
        private static final int SCALAR_LOGICAL = 2;
        @SuppressWarnings("unused") private static final int SCALAR_STRING = 3;
        private static final int VECTOR_DOUBLE = 10;
        private static final int VECTOR_INT = 11;
        private static final int VECTOR_LOGICAL = 12;
        @SuppressWarnings("unused") private static final int VECTOR_STRING = 12;

        @SuppressWarnings("unused")
        @Specialization
        public RList c(String f, Object[] args, byte naok, byte dup, RMissing rPackage, RMissing encoding) {
            controlVisibility();
            SymbolInfo symbolInfo = DLL.findSymbolInfo(f, null);
            if (symbolInfo == null) {
                CompilerDirectives.transferToInterpreter();
                throw RError.getGenericError(getEncapsulatingSourceSection(), "symbol " + f + " not in load table");
            }
            boolean dupArgs = RRuntime.fromLogical(dup);
            boolean checkNA = RRuntime.fromLogical(naok);
            // Analyze the args, making copies (ignoring dup for now)
            int[] argTypes = new int[args.length];
            Object[] nativeArgs = new Object[args.length];
            for (int i = 0; i < args.length; i++) {
                Object arg = args[i];
                if (arg instanceof RDoubleVector) {
                    argTypes[i] = VECTOR_DOUBLE;
                    nativeArgs[i] = checkNAs(i + 1, ((RDoubleVector) arg).getDataCopy());
                } else if (arg instanceof RIntVector) {
                    argTypes[i] = VECTOR_INT;
                    nativeArgs[i] = checkNAs(i + 1, ((RIntVector) arg).getDataCopy());
                } else if (arg instanceof RLogicalVector) {
                    argTypes[i] = VECTOR_LOGICAL;
                    // passed as int[]
                    byte[] data = ((RLogicalVector) arg).getDataWithoutCopying();
                    int[] dataAsInt = new int[data.length];
                    for (int j = 0; j < data.length; j++) {
                        // An NA is an error but the error handling happens in checkNAs
                        dataAsInt[j] = RRuntime.isNA(data[j]) ? RRuntime.INT_NA : data[j];
                    }
                    nativeArgs[i] = checkNAs(i + 1, dataAsInt);
                } else if (arg instanceof Double) {
                    argTypes[i] = SCALAR_DOUBLE;
                    nativeArgs[i] = checkNAs(i + 1, new double[]{(double) arg});
                } else if (arg instanceof Integer) {
                    argTypes[i] = SCALAR_INT;
                    nativeArgs[i] = checkNAs(i + 1, new int[]{(int) arg});
                } else if (arg instanceof Byte) {
                    argTypes[i] = SCALAR_LOGICAL;
                    nativeArgs[i] = checkNAs(i + 1, new int[]{RRuntime.isNA((byte) arg) ? RRuntime.INT_NA : (byte) arg});
                } else {
                    CompilerDirectives.transferToInterpreter();
                    throw RError.getGenericError(getEncapsulatingSourceSection(), "unimplemented argument type (arg " + (i + 1) + ")");
                }
            }
            try {
                RFFIFactory.getRFFI().getCRFFI().invoke(symbolInfo, nativeArgs);
            } catch (Throwable t) {
                CompilerDirectives.transferToInterpreter();
                throw RError.getGenericError(getEncapsulatingSourceSection(), "native call failed: " + t.getMessage());
            }
            // we have to assume that the native method updated everything
            RStringVector listNames = validateArgNames(args.length, getArgNames());
            Object[] results = new Object[args.length];
            for (int i = 0; i < args.length; i++) {
                switch (argTypes[i]) {
                    case SCALAR_DOUBLE:
                        results[i] = RDataFactory.createDoubleVector((double[]) nativeArgs[i], RDataFactory.COMPLETE_VECTOR);
                        break;
                    case SCALAR_INT:
                        results[i] = RDataFactory.createIntVector((int[]) nativeArgs[i], RDataFactory.COMPLETE_VECTOR);
                        break;
                    case SCALAR_LOGICAL:
                        results[i] = RDataFactory.createLogicalVector((byte[]) nativeArgs[i], RDataFactory.COMPLETE_VECTOR);
                        break;
                    case VECTOR_DOUBLE: {
                        RVector dVec = (RVector) args[i];
                        results[i] = ((RDoubleVector) dVec.copy()).resetData((double[]) nativeArgs[i]);
                        break;
                    }
                    case VECTOR_INT: {
                        RVector iVec = (RVector) args[i];
                        results[i] = ((RIntVector) iVec.copy()).resetData((int[]) nativeArgs[i]);
                        break;
                    }
                    case VECTOR_LOGICAL: {
                        RVector iVec = (RVector) args[i];
                        results[i] = ((RLogicalVector) iVec.copy()).resetData((byte[]) nativeArgs[i]);
                        break;
                    }

                }
            }
            return RDataFactory.createList(results, listNames);
        }

        private static RStringVector validateArgNames(int argsLength, String[] argNames) {
            String[] listArgNames = new String[argsLength];
            for (int i = 0; i < argsLength; i++) {
                String name = argNames == null ? null : argNames[i + 1];
                if (name == null) {
                    name = RRuntime.NAMES_ATTR_EMPTY_VALUE;
                }
                listArgNames[i] = name;
            }
            return RDataFactory.createStringVector(listArgNames, RDataFactory.COMPLETE_VECTOR);
        }

    }

}