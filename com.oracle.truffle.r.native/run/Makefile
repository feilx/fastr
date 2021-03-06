#
# Copyright (c) 2015, 2016, Oracle and/or its affiliates. All rights reserved.
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

# Sets up the "bin" directory with scripts mostly copied from GnuR.
# The FastR'ness is handled in the "R" file in the "bin/exec" subdirectory.
# Rscript is a separate program in GnuR and stored directly in the "bin" directory,
# but in FastR it is just a slight variant of "R". However, we cannot put
# a FastR-specific Rscript in "exec" because the install_packages code
# treats everything in there except "R" as a sub-architecture, so we put in
# execRextras.
#
# The R script defines the R_HOME environment variable from R_HOME_DIR
# which is set in the script during the GnuR build. This has to be changed.

# For building R packages with native code we use the Makeconf file from GnuR etc
# with some minor changes

.PHONY: bindir all rcmd

FASTR_BIN_DIR := $(FASTR_R_HOME)/bin
FASTR_DOC_DIR := $(FASTR_R_HOME)/doc
FASTR_ETC_DIR := $(FASTR_R_HOME)/etc
FASTR_SHARE_DIR := $(FASTR_R_HOME)/share
FASTR_INCLUDE_DIR := $(FASTR_R_HOME)/include

R_SCRIPT := $(addprefix $(GNUR_HOME)/bin/,R)
BIN_FILES := $(wildcard $(GNUR_HOME)/bin/*)

DOC_FILES := $(wildcard $(GNUR_HOME)/doc/*)

# Not all of these work unchanged
ETC_FILES := $(addprefix $(GNUR_HOME)/etc/,javaconf ldpaths Renviron repositories)

SHARE_FILES := $(addprefix $(GNUR_HOME)/share/,R Rd make java encodings)

all: rundirs rcmds includedir

rundirs:
	mkdir -p $(FASTR_BIN_DIR)
	mkdir -p $(FASTR_DOC_DIR)
	mkdir -p $(FASTR_BIN_DIR)/exec
	mkdir -p $(FASTR_BIN_DIR)/execRextras
	mkdir -p $(FASTR_ETC_DIR)
	mkdir -p $(FASTR_SHARE_DIR)
	
rcmds: $(FASTR_BIN_DIR)/R

$(FASTR_BIN_DIR)/R: Makefile R.sh Rscript.sh Rscript_exec.sh Rclasspath.sh
	cp -r $(BIN_FILES) $(FASTR_BIN_DIR)
    # overide bin/R
	cp R.sh $(FASTR_BIN_DIR)/exec/R
	# override bin/Rscript
	cp Rscript_exec.sh $(FASTR_BIN_DIR)/execRextras/Rscript
	cp Rscript.sh $(FASTR_BIN_DIR)/Rscript
	# for embedded support
	cp Rclasspath.sh $(FASTR_BIN_DIR)/execRextras/Rclasspath
	chmod +x $(FASTR_BIN_DIR)/exec/R $(FASTR_BIN_DIR)/execRextras/Rscript $(FASTR_BIN_DIR)/Rscript $(FASTR_BIN_DIR)/execRextras/Rclasspath
	# update R_HOME_DIR to FastR
	sed -e 's!^\(R_HOME_DIR=\)\(.*\)!\1"$(FASTR_R_HOME)"!' < $(R_SCRIPT) > $(FASTR_BIN_DIR)/R
	chmod +x $(FASTR_BIN_DIR)/R 
	touch $(FASTR_ETC_DIR)/ldpaths
	sed -e "s|\(R_LIBS_USER=.*-'\)\(.*\)'}|\1$(FASTR_R_HOME)/library'}|" < $(GNUR_HOME)/etc/Renviron > $(FASTR_ETC_DIR)/Renviron
	cp $(GNUR_HOME)/etc/Makeconf Makeconf.etc
	cp $(GNUR_HOME)/etc/javaconf $(FASTR_ETC_DIR)/javaconf
	cp $(GNUR_HOME)/etc/repositories $(FASTR_ETC_DIR)/repositories
	cp $(GNUR_HOME)/etc/ldpaths $(FASTR_ETC_DIR)/ldpaths
	ed Makeconf.etc < edMakeconf.etc
	cp Makeconf.etc $(FASTR_ETC_DIR)/Makeconf
	cp -r $(SHARE_FILES) $(FASTR_SHARE_DIR)
	# TODO may need filtering
	cp -r $(DOC_FILES) $(FASTR_DOC_DIR)
	# overrides
	cp examples-header.R examples-footer.R $(FASTR_SHARE_DIR)/R

includedir: $(FASTR_R_HOME)/include/Rinternals.h

$(FASTR_R_HOME)/include/Rinternals.h: Makefile $(TOPDIR)/include/Rinternals.h
	mkdir -p $(FASTR_R_HOME)/include
	cp -r $(TOPDIR)/include/* $(FASTR_R_HOME)/include

clean:
	rm -rf $(FASTR_BIN_DIR)
	rm -rf $(FASTR_ETC_DIR)
	rm -rf $(FASTR_SHARE_DIR)
	rm -rf $(FASTR_INCLUDE_DIR)
