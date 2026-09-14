package com.github.btrapp.fastflashfinder;

import com.github.btrapp.fastflashfinder.FastFlashObjects.FlashId;
import com.github.btrapp.fastflashfinder.FastFlashObjects.ZeroZeroFlashInst;

public class CommonFlashUtils {
	public static final FlashId findFlashId(ZeroZeroFlashInst zzfi, double waferX, double waferY) {
		// Figure out the difference in X from the zeroZero flash LLx
		int dxWaferStepInt = flashStep(waferX, zzfi.llx(), zzfi.steppingWidth());
		int dyWaferStepInt = flashStep(waferY, zzfi.lly(), zzfi.steppingHeight());
		return new FlashId(dxWaferStepInt, dyWaferStepInt);
	}

	/**
	 * Given a wafer level dimension (um), figure out which flash ID would contain
	 * that dimension. (Notch down, x++ is right, y++ is up)
	 * 
	 * @param waferDim
	 * @param flashStart
	 * @param flashStep
	 * @return
	 */
	protected static int flashStep(double waferDim, double flashStart, double flashStep) {
		double dWafer = waferDim - flashStart;
		double stepDouble = dWafer / flashStep;
		int stepInt = (int) Math.floor(stepDouble);
		if (stepDouble == stepInt) {
			// This is EXACTLY at the start. By convention this doesn't match and should
			// return the previous flash.
			return stepInt - 1;
		}
		return stepInt;
	}

	protected static double[] calculateFlashRelativeXY(FlashId flashIdXy, ZeroZeroFlashInst zzfi, double waferX,
			double waferY) {
		double[] flashLlXy = zzfi.calcFlashLlXy(flashIdXy);
		double dx = waferX - flashLlXy[0];
		double dy = waferY - flashLlXy[1];
		return new double[] { dx, dy };
	}
}
