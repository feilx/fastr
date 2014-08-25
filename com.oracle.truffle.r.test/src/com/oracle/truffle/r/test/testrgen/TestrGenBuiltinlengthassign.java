/*
 * This material is distributed under the GNU General Public License
 * Version 2. You may review the terms of this license at
 * http://www.gnu.org/licenses/gpl-2.0.html
 * 
 * Copyright (c) 2014, Purdue University
 * Copyright (c) 2014, Oracle and/or its affiliates
 *
 * All rights reserved.
 */
package com.oracle.truffle.r.test.testrgen;

import org.junit.*;

import com.oracle.truffle.r.test.*;

public class TestrGenBuiltinlengthassign extends TestBase {

    @Test
    @Ignore
    public void testlengthassign1() {
        assertEval("argv <- list(c(\'A\', \'B\'), value = 5);`length<-`(argv[[1]],argv[[2]]);");
    }

    @Test
    @Ignore
    public void testlengthassign2() {
        assertEval("argv <- list(list(list(2, 2, 6), list(2, 2, 0)), value = 0);`length<-`(argv[[1]],argv[[2]]);");
    }

    @Test
    @Ignore
    public void testlengthassign3() {
        assertEval("argv <- list(list(list(2, 2, 6), list(1, 3, 9), list(1, 3, -1)), value = 1);`length<-`(argv[[1]],argv[[2]]);");
    }

    @Test
    @Ignore
    public void testlengthassign4() {
        assertEval("argv <- list(c(28, 27, 26, 25, 24, 23, 22, 21, 20, 19, 18, 17, 16, 15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1), value = 0L);`length<-`(argv[[1]],argv[[2]]);");
    }

    @Test
    @Ignore
    public void testlengthassign5() {
        assertEval("argv <- list(c(0L, 1L, 2L, 3L, 4L, 5L, 6L, 0L, 1L, 2L, 3L, 4L, 5L, 6L, 0L, 1L, 2L, 3L, 4L, 5L, 6L, 0L, 1L, 2L, 3L, 4L, 5L, 6L), value = 0L);`length<-`(argv[[1]],argv[[2]]);");
    }

    @Test
    @Ignore
    public void testlengthassign6() {
        assertEval("argv <- list(list(), value = 0L);`length<-`(argv[[1]],argv[[2]]);");
    }
}