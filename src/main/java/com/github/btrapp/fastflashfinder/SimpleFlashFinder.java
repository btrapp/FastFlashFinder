package com.github.btrapp.fastflashfinder;

import java.util.List;
import java.util.function.Predicate;

import com.github.btrapp.fastflashfinder.FastFlashObjects.DieEdgeMatchLogic;
import com.github.btrapp.fastflashfinder.FastFlashObjects.FlashDieInst;
import com.github.btrapp.fastflashfinder.FastFlashObjects.FlashId;
import com.github.btrapp.fastflashfinder.FastFlashObjects.ZeroZeroFlashInst;

public class SimpleFlashFinder {
	private final ZeroZeroFlashInst zeroZeroFlash;
	private final List<FlashDieInst> dies;
	private DieEdgeMatchLogic edgeLogic = DieEdgeMatchLogic.EITHER_SIDE;

	public SimpleFlashFinder(ZeroZeroFlashInst zeroZeroFlash, List<FlashDieInst> flashRelativeDies) {
		this.zeroZeroFlash = zeroZeroFlash;
		this.dies = flashRelativeDies;
	}

	public FlashId findFlashId(double waferX, double waferY) {
		return CommonFlashUtils.findFlashId(zeroZeroFlash, waferX, waferY);
	}

	public FlashDieInst findDieInstanceOrNull(FlashId flashId, double waferX, double waferY) {
		// Convert wafer XY into flash-Relative XY
		final double[] dieXY = CommonFlashUtils.calculateFlashRelativeXY(flashId, zeroZeroFlash, waferX, waferY);
		final Predicate<FlashDieInst> containsXY = fdi -> fdi.containsXY(dieXY[0], dieXY[1], edgeLogic);
		return dies.stream().filter(containsXY).findFirst().orElse(null);
	}

	public void setEdgeLogic(DieEdgeMatchLogic edgeLogic) {
		this.edgeLogic = edgeLogic;
	}
}
