package com.github.btrapp.fastflashfinder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.github.btrapp.fastflashfinder.ScanLineFlashFinder.ScanEvent;
import com.github.btrapp.fastflashfinder.FastFlashObjects.DieEdgeMatchLogic;
import com.github.btrapp.fastflashfinder.FastFlashObjects.FlashDieInst;

public class ScanEventTestCase {
	@Test
	public void testOverlapCheck() {
		double x1 = 0;
		double x2 = 10;
		double x3 = 20;
		double y1 = 0;
		double y2 = 10;
		FlashDieInst d0 = new FlashDieInst(0, x1, y1, x2, y2);
		FlashDieInst d1 = new FlashDieInst(1, x2, y1, x3, y2);
		assertTrue(ScanLineFlashFinder.doMyDieCornersOverlap(List.of(d0, d1)), "detected a die overlap");

		d0 = new FlashDieInst(0, x1, y1, x2, y2);
		d1 = new FlashDieInst(1, x2 + 0.01, y1, x3, y2);
		assertFalse(ScanLineFlashFinder.doMyDieCornersOverlap(List.of(d0, d1)), "No die overlap");

	}

	@Test
	public void testOverlappingScanEvent() {
		double x1 = 0;
		double x2 = 10;
		double x3 = 20;
		ScanEvent se = new ScanEvent(x2);
		double y1 = 0;
		double y2 = 10;
		FlashDieInst d0 = new FlashDieInst(0, x1, y1, x2, y2);
		FlashDieInst d1 = new FlashDieInst(1, x2, y1, x3, y2);
		se.setEndEvents(Set.of(d0)); // D0 ends at x2
		se.setStartEvents(Set.of(d1)); // D1 starts at x2

		assertTrue(ScanLineFlashFinder.doMyDieCornersOverlap(List.of(d0, d1)), "detected a die overlap");

		assertEquals(Set.of(d0, d1), se.matchDieIds(x2, DieEdgeMatchLogic.EITHER_SIDE), "BothEdge"); // Both
		assertEquals(Set.of(d1), se.matchDieIds(x2, DieEdgeMatchLogic.LEFT_SIDE), "Left matches"); // Just D1
		assertEquals(Set.of(d0), se.matchDieIds(x2, DieEdgeMatchLogic.RIGHT_SIDE), "Right matches"); // Just D0

		// A tiny bit past start always only matches d1
		double smallOffsetPastStart = x2 + 0.1;
		assertEquals(Set.of(d1), se.matchDieIds(smallOffsetPastStart, DieEdgeMatchLogic.EITHER_SIDE), "BothEdge");
		assertEquals(Set.of(d1), se.matchDieIds(smallOffsetPastStart, DieEdgeMatchLogic.LEFT_SIDE), "Left matches");
		assertEquals(Set.of(d1), se.matchDieIds(smallOffsetPastStart, DieEdgeMatchLogic.RIGHT_SIDE), "Right matches");
	}
}
