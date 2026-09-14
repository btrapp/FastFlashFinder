package com.github.btrapp.fastflashfinder;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class FlashLookupTestCase {
	@Test
	void testFlashStepMath() {
		// Given a flash start at 0, and size of 100:
		double tol = 0.000001; // Allowable tolerance
		// An exact match at sh
		double zeroZeroFlashStart = 0;
		double flashSize = 100;
		// Easy on 0,0 flash
		assertEquals(0, FastFlashFinder.flashStep(1, zeroZeroFlashStart, flashSize), tol);
		// Easy on next flash +1
		assertEquals(1, FastFlashFinder.flashStep(1 + flashSize, zeroZeroFlashStart, flashSize), tol);
		// Easy on next flash +2
		assertEquals(2, FastFlashFinder.flashStep(1 + flashSize + flashSize, zeroZeroFlashStart, flashSize), tol);
		// Easy on flash -1
		assertEquals(-1, FastFlashFinder.flashStep(1 - flashSize, zeroZeroFlashStart, flashSize), tol);

		// Fussier boundary conditions. By convention we do not match on start but we do
		// match on end.

		// So using the START of 0,0 flash should actually return -1
		assertEquals(-1, FastFlashFinder.flashStep(zeroZeroFlashStart, zeroZeroFlashStart, flashSize), tol);
		// And the end of 0,0 should match 0
		assertEquals(0, FastFlashFinder.flashStep(zeroZeroFlashStart + flashSize, zeroZeroFlashStart, flashSize), tol);

		// So using the START of -10,0 flash should actually return -1
		assertEquals(-11, FastFlashFinder.flashStep(flashSize * -10, zeroZeroFlashStart, flashSize), tol);
		// And the START of 10,0 should match 9
		assertEquals(9, FastFlashFinder.flashStep(flashSize * 10, zeroZeroFlashStart, flashSize), tol);

	}
}
