package com.github.btrapp.fastflashfinder;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.github.btrapp.fastflashfinder.FastFlashObjects.FastFlashException;
import com.github.btrapp.fastflashfinder.FastFlashObjects.FlashDieInst;
import com.github.btrapp.fastflashfinder.FastFlashObjects.FlashId;
import com.github.btrapp.fastflashfinder.FastFlashObjects.FlashInst;
import com.github.btrapp.fastflashfinder.FastFlashObjects.FlastFlashExceptionErrorCode;

public class FastFlashFinder {
	private TreeMap<Double,ScanEvent> xMap;
	private TreeMap<Double,ScanEvent> yMap;
	private FlashInst zeroZeroFlash; //The reference (0,0) flash.
	public FastFlashFinder (FlashInst zeroZeroFlash, List<FlashDieInst> dies) {
		this.zeroZeroFlash = zeroZeroFlash;
		this.xMap = buildScanMap(dies, FlashDieInst::llx, FlashDieInst::urx);
		this.yMap = buildScanMap(dies, FlashDieInst::lly, FlashDieInst::ury);
	}
	
	public FlashId findFlash(double waferX, double waferY) {
		//Figure out the difference in X from the zeroZero flash LLx
		double dxWafer = waferX-zeroZeroFlash.llx();
		double dxWaferSteps = dxWafer / zeroZeroFlash.steppingWidth();
		int dxWaferStepInt = (int) Math.floor(dxWaferSteps);
		
		double dyWafer = waferY-zeroZeroFlash.lly();
		double dyWaferSteps = dyWafer / zeroZeroFlash.steppingHeight();
		int dyWaferStepInt = (int) Math.floor(dyWaferSteps);
		
		return new FlashId(dxWaferStepInt,dyWaferStepInt);
	}
	
	/**
	 * This expects WAFER level coordiantes (notch down, 0,0 at center of wafer)
	 * @param flashId (@see findFlash(waferX,waferY) to determine this)
	 * @param waferX
	 * @param waferY
	 * @return
	 * @throws FastFlashException
	 */
	public FlashDieInst findDieInstanceOrNull(FlashId flashId, double waferX, double waferY) throws FastFlashException {
		//Convert wafer XY into flash-Relative XY
		double[] dieXY = calculateFlashRelativeXY(flashId, waferX, waferY);
		return findDieInstanceOrNull(dieXY[0], dieXY[1]);
	}
	
	
	
	private double[] calculateFlashRelativeXY(FlashId flashIdXy, double waferX, double waferY) {
		double fx = flashIdXy.flashIdX()*zeroZeroFlash.steppingWidth();
		double fy = flashIdXy.flashIdY()*zeroZeroFlash.steppingHeight();
		double dx = waferX-fx;
		double dy = waferY-fy;
		return new double[] {dx,dy};
	}
	
	//This expects FLASH coordinates (from the LLxy of the correct flash)
	protected FlashDieInst findDieInstanceOrNull(double flashX, double flashY) throws FastFlashException {
		
		Entry<Double, ScanEvent> seX = xMap.floorEntry(flashX);
		Entry<Double, ScanEvent> seY = yMap.floorEntry(flashY);
		if (seX == null || seY == null) {
			return null;
		}
		Set<FlashDieInst> xMatches = seX.getValue().matchDieIds(flashX);
		if (xMatches.isEmpty())
			return null;
		
		Set<FlashDieInst> yMatches = seY.getValue().matchDieIds(flashY);
		yMatches.retainAll(xMatches);
		if (yMatches.isEmpty())
			return null;
		
		if (yMatches.size() == 1)
			return yMatches.iterator().next();
		throw new FastFlashObjects.FastFlashException(FlastFlashExceptionErrorCode.OVERLAPPING_DIES, "Overlapping Dies found at FlashXY: "+flashX+","+flashY);
	}
	
	private static final class ScanEvent {
		private double eventKey;
		private Set<FlashDieInst> endEvents = Set.of();
		private Set<FlashDieInst> continueEvents = Set.of();
		private Set<FlashDieInst> startEvents = Set.of();
		public ScanEvent(double key) {
			this.eventKey=key;
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
			//A debug string
			Function<Set<FlashDieInst>,String> toStr = (s)-> s.stream().map(i->i.toString()).collect(Collectors.joining(","));
			String s = "Scan Event for key "+eventKey+"\n";
			s += "Strt: "+toStr.apply(startEvents)+"\n";
			s += "Cont: "+toStr.apply(continueEvents)+"\n";
			s += "Ends: "+toStr.apply(endEvents)+"\n";
			return s;
		}
		public Set<FlashDieInst> matchDieIds(double key) {
			Set<FlashDieInst> matches = new HashSet<>();
			matches.addAll(continueEvents);
			if (key == eventKey) 
				matches.addAll(endEvents); //If we end exactly on this key, its included.
			if (key > eventKey)
				matches.addAll(startEvents); //only include start events if the key is **after** (not equal to) the event key!
			 
			return matches;
		}
	}

	private static TreeMap<Double,ScanEvent> buildScanMap(List<FlashDieInst> dies, Function<FlashDieInst,Double> startKey,  Function<FlashDieInst,Double> endKey) {
		Map<Double,Set<FlashDieInst>> startEvents = dies.stream().collect(Collectors.groupingBy(startKey,Collectors.mapping(Function.identity(),Collectors.toSet())));
		Map<Double,Set<FlashDieInst>> endEvents = dies.stream().collect(Collectors.groupingBy(endKey,Collectors.mapping(Function.identity(),Collectors.toSet())));
		
		Set<Double> sortedKeys = new TreeSet<>(startEvents.keySet());
		sortedKeys.addAll(endEvents.keySet());
		
		Set<FlashDieInst> activeDies = new HashSet<>(); //Tracks dies that have started but not ended yet
		
		TreeMap<Double,ScanEvent> scanEvents = new TreeMap<>();
		for (Double key: sortedKeys) {
			ScanEvent se = new ScanEvent(key);
			
			//Process END events
			Set<FlashDieInst> endEventList = endEvents.get(key);
			if (endEventList != null) {
				se.setEndEvents(endEventList);
				//Remove these IDs from the active list
				activeDies.removeAll(endEventList);
			}
			
			//Process 'Continue' events
			if (!activeDies.isEmpty()) {
				se.setContinueEvents(new HashSet<>(activeDies)); //Use a new set as we will modify activeDies object
			}
			
			//Process 'Start' events
			Set<FlashDieInst> startEventList = startEvents.get(key);
			if (startEventList != null) {
				se.setStartEvents(startEventList);
				//Add these IDs from the active list for the *next* key
				activeDies.addAll(startEventList);
			}
			
			scanEvents.put(key, se);
		}
		return scanEvents;
	}
}
