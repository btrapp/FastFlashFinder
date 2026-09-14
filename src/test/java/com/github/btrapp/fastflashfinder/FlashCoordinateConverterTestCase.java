package com.github.btrapp.fastflashfinder;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.github.btrapp.fastflashfinder.FastFlashObjects.FlashId;
import com.github.btrapp.fastflashfinder.FastFlashObjects.FlashInst;

public class FlashCoordinateConverterTestCase {
	@Test
	void testConversion() {
		FlashInst zzf = new FlashInst(-1000, 2000, 500, 750);
		FastFlashFinder fff = new FastFlashFinder(zzf, List.of());
		double[] rel = fff.calculateFlashRelativeXY(new FlashId(0, 0), -1000 + 1, 2000 + 2);
		double tol = 0.00001;
		assertEquals(1, rel[0], tol);
		assertEquals(2, rel[1], tol);

		// Now move to a +1 in X flash
		rel = fff.calculateFlashRelativeXY(new FlashId(1, 0), -1000 + 1 + zzf.steppingWidth(), 2000 + 2);
		assertEquals(1, rel[0], tol);
		assertEquals(2, rel[1], tol);

		// Now move to a -1 in X flash
		rel = fff.calculateFlashRelativeXY(new FlashId(-1, 0), -1000 + 1 - zzf.steppingWidth(), 2000 + 2);
		assertEquals(1, rel[0], tol);
		assertEquals(2, rel[1], tol);

		// Now move to a +1 in Y flash
		rel = fff.calculateFlashRelativeXY(new FlashId(0, 1), -1000 + 1, 2000 + 2 + zzf.steppingHeight());
		assertEquals(1, rel[0], tol);
		assertEquals(2, rel[1], tol);

		// Now move to a -1 in Y flash
		rel = fff.calculateFlashRelativeXY(new FlashId(0, -1), -1000 + 1, 2000 + 2 - zzf.steppingHeight());
		assertEquals(1, rel[0], tol);
		assertEquals(2, rel[1], tol);
	}
}
