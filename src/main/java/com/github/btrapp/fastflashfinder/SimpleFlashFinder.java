package com.github.btrapp.fastflashfinder;

import java.util.List;
import java.util.function.Predicate;

import com.github.btrapp.fastflashfinder.FastFlashObjects.FlashDieInst;
import com.github.btrapp.fastflashfinder.FastFlashObjects.FlashId;
import com.github.btrapp.fastflashfinder.FastFlashObjects.ZeroZeroFlashInst;

public class SimpleFlashFinder {
	ZeroZeroFlashInst zeroZeroFlash;
	List<FlashDieInst> dies;

	public SimpleFlashFinder(ZeroZeroFlashInst zeroZeroFlash, List<FlashDieInst> flashRelativeDies) {
		this.zeroZeroFlash = zeroZeroFlash;
		this.dies = flashRelativeDies;
	}

	public FlashId findFlash(double waferX, double waferY) {
		// Figure out the difference in X from the zeroZero flash LLx
		int dxWaferStepInt = FastFlashFinder.flashStep(waferX, zeroZeroFlash.llx(), zeroZeroFlash.steppingWidth());
		int dyWaferStepInt = FastFlashFinder.flashStep(waferY, zeroZeroFlash.lly(), zeroZeroFlash.steppingHeight());
		return new FlashId(dxWaferStepInt, dyWaferStepInt);
	}

	double[] calculateFlashRelativeXY(FlashId flashIdXy, double waferX, double waferY) {
		double[] flashLlXy = zeroZeroFlash.calcFlashLlXy(flashIdXy);
		double dx = waferX - flashLlXy[0];
		double dy = waferY - flashLlXy[1];
		return new double[] { dx, dy };
	}

	public FlashDieInst findDieInstanceOrNull(FlashId flashId, double waferX, double waferY) {
		// Convert wafer XY into flash-Relative XY
		final double[] dieXY = calculateFlashRelativeXY(flashId, waferX, waferY);
		final Predicate<FlashDieInst> containsXY = fdi -> fdi.containsXY(dieXY[0], dieXY[1]);
		return dies.stream().filter(containsXY).findFirst().orElse(null);
	}
}
