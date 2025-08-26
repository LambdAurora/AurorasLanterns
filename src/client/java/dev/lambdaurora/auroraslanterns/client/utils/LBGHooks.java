/*
 * Copyright © 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Lanterns.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.auroraslanterns.client.utils;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

/**
 * Provides access to LambdaBetterGrass' hooks.
 * <p>
 * This is written in a way to allow the runtime to recompile down the instructions as a direct INVOKE EXACT.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
@Environment(EnvType.CLIENT)
public final class LBGHooks {
	private static final MethodHandle PUSH;
	private static final MethodHandle POP;

	private LBGHooks() {
		throw new UnsupportedOperationException("LBGHooks only contains static definitions.");
	}

	public static void pushDisableBetterLayer() {
		if (PUSH != null) {
			try {
				PUSH.invokeExact();
			} catch (Throwable e) {
				throw new RuntimeException(e);
			}
		}
	}

	public static void popDisableBetterLayer() {
		if (POP != null) {
			try {
				POP.invokeExact();
			} catch (Throwable e) {
				throw new RuntimeException(e);
			}
		}
	}

	static {
		MethodHandle push = null;
		MethodHandle pop = null;

		try {
			final var clazz = Class.forName("dev.lambdaurora.lambdabettergrass.LambdaBetterGrass");
			final var lookup = MethodHandles.lookup();

			push = lookup.findStatic(clazz, "pushDisableBetterLayer", MethodType.methodType(void.class));
			pop = lookup.findStatic(clazz, "popDisableBetterLayer", MethodType.methodType(void.class));
		} catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException e) {
			// Ignored.
		}

		PUSH = push;
		POP = pop;
	}
}
