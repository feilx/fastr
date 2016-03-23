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
package com.oracle.truffle.r.nodes.builtin.helpers;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.MaterializedFrame;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.r.nodes.builtin.base.FrameFunctions.SysCalls;
import com.oracle.truffle.r.nodes.builtin.base.FrameFunctionsFactory.SysCallsNodeGen;
import com.oracle.truffle.r.nodes.builtin.base.PrettyPrinterNode;
import com.oracle.truffle.r.nodes.builtin.base.PrettyPrinterNodeGen;
import com.oracle.truffle.r.nodes.builtin.base.Quit;
import com.oracle.truffle.r.runtime.BrowserQuitException;
import com.oracle.truffle.r.runtime.RArguments;
import com.oracle.truffle.r.runtime.RRuntime;
import com.oracle.truffle.r.runtime.ReturnException;
import com.oracle.truffle.r.runtime.context.ConsoleHandler;
import com.oracle.truffle.r.runtime.context.Engine.ParseException;
import com.oracle.truffle.r.runtime.context.RContext;
import com.oracle.truffle.r.runtime.data.RLogicalVector;
import com.oracle.truffle.r.runtime.data.RPairList;
import com.oracle.truffle.r.runtime.nodes.RNode;

/**
 * The interactive component of the {@code browser} function.
 *
 * TODO GnuR does not allow quit() from the browser. This really needs to be checked in the quit
 * builtin somehow.
 */
public abstract class BrowserInteractNode extends RNode {

    public static final int STEP = 0;
    public static final int NEXT = 1;
    public static final int CONTINUE = 2;
    public static final int FINISH = 3;

    private static final String BROWSER_SOURCE = "<browser_input>";
    private static String lastEmptyLineCommand = "n";

    /**
     * This used by {@link Quit} to prevent a "quit" from the browser (as per GnuR). If we supported
     * multiple interactive contexts, this would need become context specific.
     */
    private static boolean inBrowser;

    @Child private SysCalls sysCalls;
    @Child private PrettyPrinterNode printer;

    public static boolean inBrowser() {
        return inBrowser;
    }

    @Specialization
    protected int interact(VirtualFrame frame) {
        CompilerDirectives.transferToInterpreter();
        MaterializedFrame mFrame = frame.materialize();
        ConsoleHandler ch = RContext.getInstance().getConsoleHandler();
        String savedPrompt = ch.getPrompt();
        ch.setPrompt(browserPrompt(RArguments.getDepth(frame)));
        int exitMode = NEXT;
        try {
            inBrowser = true;
            LW: while (true) {
                String input = ch.readLine();
                if (input != null) {
                    input = input.trim();
                }
                if (input == null || input.length() == 0) {
                    RLogicalVector browserNLdisabledVec = (RLogicalVector) RContext.getInstance().stateROptions.getValue("browserNLdisabled");
                    if (!RRuntime.fromLogical(browserNLdisabledVec.getDataAt(0))) {
                        input = lastEmptyLineCommand;
                    }
                }
                switch (input) {
                    case "c":
                    case "cont":
                        exitMode = CONTINUE;
                        break LW;
                    case "n":
                        exitMode = NEXT;
                        lastEmptyLineCommand = "n";
                        break LW;
                    case "s":
                        exitMode = STEP;
                        lastEmptyLineCommand = "s";
                        break LW;
                    case "f":
                        exitMode = FINISH;
                        break LW;
                    case "Q":
                        throw new BrowserQuitException();
                    case "where": {
                        /*
                         * This is experimental and perhaps too indirect, but by using syscalls and
                         * the printer we avoid repeating the logic for stack traversal and
                         * printing. TODO print source info where available as per GnuR
                         */
                        if (sysCalls == null) {
                            sysCalls = insert(SysCallsNodeGen.create(new RNode[0], null, null));
                            sysCalls.setIncludeTop();
                            printer = insert(PrettyPrinterNodeGen.create(null, null, null, null, false));
                        }
                        if (RArguments.getDepth(mFrame) > 1) {
                            RPairList stack = (RPairList) sysCalls.execute(frame);
                            int length = stack.getLength();
                            for (int i = length - 1; i >= 0; i--) {
                                Object element = stack.getDataAtAsObject(i);
                                String call = (String) printer.executeString(element, null, RRuntime.LOGICAL_FALSE, RRuntime.LOGICAL_FALSE);
                                ch.printf("where %d: %s%n", length - i, call);
                            }
                        }
                        ch.println("");
                        break;
                    }

                    default:
                        try {
                            RContext.getEngine().parseAndEval(Source.fromText(input, BROWSER_SOURCE), mFrame, true);
                        } catch (ReturnException e) {
                            exitMode = NEXT;
                            break LW;
                        } catch (ParseException e) {
                            throw e.throwAsRError();
                        }
                        break;
                }
            }
        } finally {
            ch.setPrompt(savedPrompt);
            inBrowser = false;
        }
        return exitMode;
    }

    private static String browserPrompt(int depth) {
        return "Browse[" + depth + "]> ";
    }
}
