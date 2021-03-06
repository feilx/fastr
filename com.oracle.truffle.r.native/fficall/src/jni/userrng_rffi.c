/*
 * Copyright (c) 2016, 2016, Oracle and/or its affiliates. All rights reserved.
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

#include <rffiutils.h>

typedef void (*call_init)(int seed);
typedef double* (*call_rand)(void);
typedef int* (*call_nSeed)(void);
typedef int* (*call_seeds)(void);

JNIEXPORT void JNICALL
Java_com_oracle_truffle_r_runtime_ffi_jni_JNI_1UserRng_init(JNIEnv *env, jclass c, jlong address, jint seed) {
	call_init f = (call_init) address;
	f(seed);
}

JNIEXPORT double JNICALL
Java_com_oracle_truffle_r_runtime_ffi_jni_JNI_1UserRng_rand(JNIEnv *env, jclass c, jlong address) {
	call_rand f = (call_rand) address;
	double* dp = f();
	return *dp;
}

JNIEXPORT jint JNICALL
Java_com_oracle_truffle_r_runtime_ffi_jni_JNI_1UserRng_nSeed(JNIEnv *env, jclass c, jlong address) {
	call_nSeed f = (call_nSeed) address;
	int *pn = f();
	return *pn;
}

JNIEXPORT void JNICALL
Java_com_oracle_truffle_r_runtime_ffi_jni_JNI_1UserRng_seeds(JNIEnv *env, jclass c, jlong address, jintArray seedsArray) {
	call_seeds f = (call_seeds) address;
	int *pseeds = f();
	int seedslen = (*env)->GetArrayLength(env, seedsArray);
	int *data = (*env)->GetIntArrayElements(env, seedsArray, NULL);
	for (int i = 0; i < seedslen; i++) {
		data[i] = pseeds[i];
	}
	(*env)->ReleaseIntArrayElements(env, seedsArray, data, 0);
}
