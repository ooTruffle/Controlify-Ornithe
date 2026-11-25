/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.ingame;

public enum InputCurves implements InputCurve {
	LINEAR(d -> d),
	GENTLE(InputCurve.power(1.5)),
	STANDARD(InputCurve.power(2.0)),
	STRONG(InputCurve.power(2.5)),
	PRECISION_CUBIC(InputCurve.cubicBlend(0.5)),
	S_CURVE(InputCurve.sCurve());

	private final InputCurve curve;

	InputCurves(InputCurve curve) {
		this.curve = curve;
	}

	@Override
	public double apply(double input) {
		return this.curve.apply(input);
	}
}
