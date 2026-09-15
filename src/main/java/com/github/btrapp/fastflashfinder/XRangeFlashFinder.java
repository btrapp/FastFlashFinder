package com.github.btrapp.fastflashfinder;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.github.btrapp.fastflashfinder.FastFlashObjects.DieEdgeMatchLogic;
import com.github.btrapp.fastflashfinder.FastFlashObjects.FlashAndDie;
import com.github.btrapp.fastflashfinder.FastFlashObjects.FlashDieInst;
import com.github.btrapp.fastflashfinder.FastFlashObjects.FlashId;
import com.github.btrapp.fastflashfinder.FastFlashObjects.ZeroZeroFlashInst;

/**
 * Groups dies by their X-range so that we can skip checking all dies in a
 * X-range we know is out of bounds. Note that X-ranges can certainly overlap
 * (Multi part wafers, for example)
 */
public class XRangeFlashFinder implements FlashFinderIf {
	private final ZeroZeroFlashInst zeroZeroFlash;
	private final Map<XRange, List<FlashDieInst>> dies;
	private DieEdgeMatchLogic edgeLogic = DieEdgeMatchLogic.EITHER_SIDE;

	private record XRange(double llx, double urx) {
		public boolean matchesX(double x, DieEdgeMatchLogic edgeLogic) {
			return edgeLogic.matches(llx, x, urx);
		}

		public static XRange fromFlashDieInst(FlashDieInst fdi) {
			return new XRange(fdi.llx(), fdi.urx());
		}
	}

	public XRangeFlashFinder(ZeroZeroFlashInst zeroZeroFlash, List<FlashDieInst> flashRelativeDies) {
		this.zeroZeroFlash = zeroZeroFlash;
		this.dies = flashRelativeDies.stream().collect(Collectors.groupingBy(XRange::fromFlashDieInst));
	}

	@Override
	public FlashAndDie findFlashAndDie(double waferX, double waferY) {
		FlashId flashId = CommonFlashUtils.findFlashId(zeroZeroFlash, waferX, waferY);
		FlashDieInst die = findDieInstanceOrNull(flashId, waferX, waferY);
		return new FlashAndDie(flashId, die);
	}

	private FlashDieInst findDieInstanceOrNull(FlashId flashId, double waferX, double waferY) {
		// Convert wafer XY into flash-Relative XY
		final double[] dieXY = CommonFlashUtils.calculateFlashRelativeXY(flashId, zeroZeroFlash, waferX, waferY);
		final double flashX = dieXY[0];
		final double flashY = dieXY[1];
		for (var e : dies.entrySet()) {
			if (e.getKey().matchesX(flashX, edgeLogic)) {
				FlashDieInst matchedDie = e.getValue().stream()
						.filter(die -> edgeLogic.matches(die.lly(), flashY, die.ury())).findFirst().orElse(null);
				if (matchedDie != null)
					return matchedDie;
			}
		}
		return null;
	}

	@Override
	public void setDieEdgeLogic(DieEdgeMatchLogic edgeLogic) {
		this.edgeLogic = edgeLogic;
	}
}
