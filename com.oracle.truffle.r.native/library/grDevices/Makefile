#
# Copyright (c) 2014, 2015, Oracle and/or its affiliates. All rights reserved.
# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
#
# This code is free software; you can redistribute it and/or modify it
# under the terms of the GNU General Public License version 2 only, as
# published by the Free Software Foundation.
#
# This code is distributed in the hope that it will be useful, but WITHOUT
# ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
# FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
# version 2 for more details (a copy is included in the LICENSE file that
# accompanied this code).
#
# You should have received a copy of the GNU General Public License version
# 2 along with this work; if not, write to the Free Software Foundation,
# Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
#
# Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
# or visit www.oracle.com if you need additional information or have any
# questions.
#

ifneq (,$(wildcard $(TOPDIR)/platform.mk))
include $(TOPDIR)/platform.mk
else
ifneq ($(MAKECMDGOALS),clean)
$(error no platform.mk available)
endif
endif

OBJ = lib

GNUR_INCLUDES := -I$(TOPDIR)/fficall/src/include
GRDEV_INCLUDES := -I$(GNUR_HOME)/src/library/grDevices
GNUR_GZIO_H := $(GNUR_HOME)/src/main/gzio.h
GNUR_SRC_CAIRO := $(GNUR_HOME)/src/library/grDevices/src/cairo
GNUR_CAIRO_C_SOURCES := $(notdir $(wildcard $(GNUR_SRC_CAIRO)/*.c))
# not compiling Cairo currently
GNUR_C_SOURCES := axis_scales.c chull.c colors.c devCairo.c devPS.c devPicTeX.c devQuartz.c \
                devices.c init.c stubs.c

ifeq ($(OS_NAME), Darwin)
GNUR_C_SOURCES := $(GNUR_C_SOURCES) qdBitmap.c qdPDF.c
endif

GNUR_C_OBJECTS := $(addprefix $(OBJ)/, $(GNUR_C_SOURCES:.c=.o))

include ../lib.mk
include $(TOPDIR)/fficall/src/include/gnurheaders.mk

#CFLAGS := $(CFLAGS) -H

$(OBJ)/%.o: $(GNUR_SRC)/%.c
	$(CC) $(CFLAGS) $(INCLUDES) $(GNUR_INCLUDES) $(GNUR_HEADER_DEFS) $(GRDEV_INCLUDES) $(SUPPRESS_WARNINGS) -c $< -o $@

$(OBJ)/%.o: $(GNUR_SRC_CAIRO)/%.c
	$(CC) $(CFLAGS) $(INCLUDES) $(GNUR_INCLUDES) $(GNUR_HEADER_DEFS) $(GRDEV_INCLUDES) $(SUPPRESS_WARNINGS) -c $< -o $@

$(OBJ)/gzio.o: $(SRC)/gzio.c
	$(CC) $(CFLAGS) $(INCLUDES) $(GNUR_INCLUDES) $(GNUR_HEADER_DEFS) -DGNUR_GZIO_H=\"$(GNUR_GZIO_H)\" $(SUPPRESS_WARNINGS) -c $< -o $@
