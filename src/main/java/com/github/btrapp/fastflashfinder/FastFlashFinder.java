package com.github.btrapp.fastflashfinder;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.github.btrapp.fastflashfinder.FastFlashObjects.DieEdgeMatchLogic;
import com.github.btrapp.fastflashfinder.FastFlashObjects.FastFlashException;
import com.github.btrapp.fastflashfinder.FastFlashObjects.FlashDieInst;
import com.github.btrapp.fastflashfinder.FastFlashObjects.FlashId;
import com.github.btrapp.fastflashfinder.FastFlashObjects.FlastFlashExceptionErrorCode;
import com.github.btrapp.fastflashfinder.FastFlashObjects.ZeroZeroFlashInst;

public class FastFlashFinder {
	private final TreeMap<Double, ScanEvent> xMap;
	private final TreeMap<Double, ScanEvent> yMap;
	private final ZeroZeroFlashInst zeroZeroFlash; // The reference (0,0) flash.
	private DieEdgeMatchLogic dieEdgeLogic = DieEdgeMatchLogic.EITHER_SIDE; // Default is to assume that a x/y match
																			// will pass for any exact edge match

	/**
	 * 
	 * @param zeroZeroFlash     the information from the 0,0 flash
	 * @param flashRelativeDies a list of all die instances on a flash (coordinates
	 *                          relative to flash origin) For example if your 0,0
	 *                          flash's LLX/LLY is -120,400 a chip starting at the
	 *                          flash LLXY would have flash relative llx of 0 and
	 *                          lly of 0 since its relative to flash origin and not
	 *                          wafer coordinates.
	 * 
	 */
	public FastFlashFinder(ZeroZeroFlashInst zeroZeroFlash, List<FlashDieInst> flashRelativeDies) {
		this.zeroZeroFlash = zeroZeroFlash;
		this.xMap = buildScanMap(flashRelativeDies, FlashDieInst::llx, FlashDieInst::urx);
		this.yMap = buildScanMap(flashRelativeDies, FlashDieInst::lly, FlashDieInst::ury);
	}

	/**
	 * If you dont have a zero zero flash, this will build the grid for any X,Y id
	 * flash given the wafer level flash and die coordinates for that flash. (For
	 * example, if flash 0,0 is off wafer and not real, you could pass in a
	 * FlashInst at 4,10 with wafer coordinates and we'll convert to 0,0
	 * 
	 * Die references should still be relative to the flash origin (which does not
	 * change across flashes)
	 * 
	 * @param nonZeroFlash
	 * @param flashIdX
	 * @param flashIdY
	 * @param flashRelativeDies (always with coordinates relative to flash origin)
	 * @return
	 */
	public static FastFlashFinder fromNonZeroZerFlash(ZeroZeroFlashInst nonZeroZeroFlash, int flashIdX, int flashIdY,
			List<FlashDieInst> flashRelativeDies) {
		// waferfx = zeroZeroX + (idX * steppingW)
		// waferfx - zeroZeroX = (idX * stepppingW)
		// -zeroZeroX = (idX*steppingW) - waferFx
		// zeroZeroX = waferFx -(idX*steppingW)
		double fllx = nonZeroZeroFlash.llx() - (flashIdX * nonZeroZeroFlash.steppingWidth());
		double flly = nonZeroZeroFlash.lly() - (flashIdY * nonZeroZeroFlash.steppingHeight());
		ZeroZeroFlashInst zeroZero = new ZeroZeroFlashInst(fllx, flly, nonZeroZeroFlash.steppingWidth(),
				nonZeroZeroFlash.steppingHeight());

		return new FastFlashFinder(zeroZero, flashRelativeDies);

	}

	public FlashId findFlash(double waferX, double waferY) {
		// Figure out the difference in X from the zeroZero flash LLx
		int dxWaferStepInt = flashStep(waferX, zeroZeroFlash.llx(), zeroZeroFlash.steppingWidth());
		int dyWaferStepInt = flashStep(waferY, zeroZeroFlash.lly(), zeroZeroFlash.steppingHeight());
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

	/**
	 * Returns true if any of your dies corners (ll,ul,lr,ur) overlap with any other
	 * dies'
	 * 
	 * @param dies
	 * @return
	 */
	public static boolean doMyDieCornersOverlap(List<FlashDieInst> dies) {
		// Look at all my corners..
		record XyUm(double x, double y) {

		}
		Map<XyUm, Integer> counts = new HashMap<>();
		for (FlashDieInst fdi : dies) {
			counts.merge(new XyUm(fdi.llx(), fdi.lly()), 1, Integer::sum);
			counts.merge(new XyUm(fdi.llx(), fdi.ury()), 1, Integer::sum);
			counts.merge(new XyUm(fdi.urx(), fdi.lly()), 1, Integer::sum);
			counts.merge(new XyUm(fdi.urx(), fdi.ury()), 1, Integer::sum);
		}
		// True if any die's LL matches any other dies UR
		return counts.values().stream().anyMatch(i -> i.intValue() > 1);
	}

	/**
	 * This expects WAFER level coordinates (notch down, 0,0 at center of wafer, x++
	 * is right, y++ is up)
	 * 
	 * @param flashId (@See findFlash(waferX,waferY) to determine this)
	 * @param waferX
	 * @param waferY
	 * @return
	 * @throws FastFlashException
	 */
	public FlashDieInst findDieInstanceOrNull(FlashId flashId, double waferX, double waferY) throws FastFlashException {
		// Convert wafer XY into flash-Relative XY
		double[] dieXY = calculateFlashRelativeXY(flashId, waferX, waferY);
		return findDieInstanceOrNullForFlashXY(dieXY[0], dieXY[1]);
	}

	double[] calculateFlashRelativeXY(FlashId flashIdXy, double waferX, double waferY) {
		double[] flashLlXy = zeroZeroFlash.calcFlashLlXy(flashIdXy);
		double dx = waferX - flashLlXy[0];
		double dy = waferY - flashLlXy[1];
		return new double[] { dx, dy };
	}

	// This expects FLASH coordinates (from the LLxy of the correct flash)
	protected FlashDieInst findDieInstanceOrNullForFlashXY(double flashX, double flashY) throws FastFlashException {

		Entry<Double, ScanEvent> seX = xMap.floorEntry(flashX);
		Entry<Double, ScanEvent> seY = yMap.floorEntry(flashY);
		if (seX == null || seY == null) {
			// System.out.println("X or Y is null " + seX + "," + seY);
			return null;
		}
		Set<FlashDieInst> xMatches = seX.getValue().matchDieIds(flashX, dieEdgeLogic);
		if (xMatches.isEmpty()) {
			// System.out.println("X doesn't match");
			return null;
		}

		Set<FlashDieInst> yMatches = seY.getValue().matchDieIds(flashY, dieEdgeLogic);
		yMatches.retainAll(xMatches);
		if (yMatches.isEmpty()) {
			// System.out.println("Y doesn't match");
			return null;
		}

		if (yMatches.size() == 1)
			return yMatches.iterator().next();
		throw new FastFlashObjects.FastFlashException(FlastFlashExceptionErrorCode.OVERLAPPING_DIES,
				"Overlapping Dies found at FlashXY: " + flashX + "," + flashY);
	}

	protected static final class ScanEvent {
		private double eventKey;
		private Set<FlashDieInst> endEvents = Set.of();
		private Set<FlashDieInst> continueEvents = Set.of();
		private Set<FlashDieInst> startEvents = Set.of();

		public ScanEvent(double key) {
			this.eventKey = key;
		}

		public void setEndEvents(Set<FlashDieInst> endEvents) {
			this.endEvents = endEvents;
		}

		public void setContinueEvents(Set<FlashDieInst> continueEvents) {
			this.continueEvents = continueEvents;
		}

		public void setStartEvents(Set<FlashDieInst> startEvents) {
			this.startEvents = startEvents;
		}

		@Override
		public String toString() {
			// A debug string
			Function<Set<FlashDieInst>, String> toStr = (s) -> s.stream().map(i -> i.toString())
					.collect(Collectors.joining(","));
			String s = "Scan Event for key " + eventKey + "\n";
			s += "Strt: " + toStr.apply(startEvents) + "\n";
			s += "Cont: " + toStr.apply(continueEvents) + "\n";
			s += "Ends: " + toStr.apply(endEvents) + "\n";
			return s;
		}

		public Set<FlashDieInst> matchDieIds(double key, DieEdgeMatchLogic dieLogic) {
			Set<FlashDieInst> matches = new HashSet<>();
			matches.addAll(continueEvents);
			if (key > eventKey) {
				matches.addAll(startEvents); // Include start events if the key is **after** (not equal to) the event
			} else if (key == eventKey) { // This is EXACTLY a start or end...
				if (dieLogic == DieEdgeMatchLogic.EITHER_SIDE || dieLogic == DieEdgeMatchLogic.LEFT_SIDE)
					matches.addAll(startEvents);
				if (dieLogic == DieEdgeMatchLogic.EITHER_SIDE || dieLogic == DieEdgeMatchLogic.RIGHT_SIDE)
					matches.addAll(endEvents);
			}
			return matches;
		}
	}

	private static TreeMap<Double, ScanEvent> buildScanMap(List<FlashDieInst> dies,
			Function<FlashDieInst, Double> startKey, Function<FlashDieInst, Double> endKey) {
		Map<Double, Set<FlashDieInst>> startEvents = dies.stream()
				.collect(Collectors.groupingBy(startKey, Collectors.mapping(Function.identity(), Collectors.toSet())));
		Map<Double, Set<FlashDieInst>> endEvents = dies.stream()
				.collect(Collectors.groupingBy(endKey, Collectors.mapping(Function.identity(), Collectors.toSet())));

		Set<Double> sortedKeys = new TreeSet<>(startEvents.keySet());
		sortedKeys.addAll(endEvents.keySet());

		Set<FlashDieInst> activeDies = new HashSet<>(); // Tracks dies that have started but not ended yet

		TreeMap<Double, ScanEvent> scanEvents = new TreeMap<>();
		for (Double key : sortedKeys) {
			ScanEvent se = new ScanEvent(key);

			// Process END events
			Set<FlashDieInst> endEventList = endEvents.get(key);
			if (endEventList != null) {
				se.setEndEvents(endEventList);
				// Remove these IDs from the active list
				activeDies.removeAll(endEventList);
			}

			// Process 'Continue' events
			if (!activeDies.isEmpty()) {
				se.setContinueEvents(new HashSet<>(activeDies)); // Use a new set as we will modify activeDies object
			}

			// Process 'Start' events
			Set<FlashDieInst> startEventList = startEvents.get(key);
			if (startEventList != null) {
				se.setStartEvents(startEventList);
				// Add these IDs from the active list for the *next* key
				activeDies.addAll(startEventList);
			}

			scanEvents.put(key, se);
		}
		return scanEvents;
	}

	public void setDieEdgeLogic(DieEdgeMatchLogic dieEdgeLogic) {
		this.dieEdgeLogic = dieEdgeLogic;
	}
}
