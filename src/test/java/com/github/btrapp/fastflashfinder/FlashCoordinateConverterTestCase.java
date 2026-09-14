package com.github.btrapp.fastflashfinder;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.github.btrapp.fastflashfinder.FastFlashObjects.FlashId;
import com.github.btrapp.fastflashfinder.FastFlashObjects.ZeroZeroFlashInst;

public class FlashCoordinateConverterTestCase extends CommonFlashUtils {
	@Test
	void testConversion() {
		ZeroZeroFlashInst zzf = new ZeroZeroFlashInst(-1000, 2000, 500, 750);
		double[] rel = CommonFlashUtils.calculateFlashRelativeXY(new FlashId(0, 0), zzf, -1000 + 1, 2000 + 2);
		double tol = 0.00001;
		assertEquals(1, rel[0], tol);
		assertEquals(2, rel[1], tol);

		// Now move to a +1 in X flash
		rel = CommonFlashUtils.calculateFlashRelativeXY(new FlashId(1, 0), zzf, -1000 + 1 + zzf.steppingWidth(),
				2000 + 2);
		assertEquals(1, rel[0], tol);
		assertEquals(2, rel[1], tol);

		// Now move to a -1 in X flash
		rel = CommonFlashUtils.calculateFlashRelativeXY(new FlashId(-1, 0), zzf, -1000 + 1 - zzf.steppingWidth(),
				2000 + 2);
		assertEquals(1, rel[0], tol);
		assertEquals(2, rel[1], tol);

		// Now move to a +1 in Y flash
		rel = CommonFlashUtils.calculateFlashRelativeXY(new FlashId(0, 1), zzf, -1000 + 1,
				2000 + 2 + zzf.steppingHeight());
		assertEquals(1, rel[0], tol);
		assertEquals(2, rel[1], tol);

		// Now move to a -1 in Y flash
		rel = CommonFlashUtils.calculateFlashRelativeXY(new FlashId(0, -1), zzf, -1000 + 1,
				2000 + 2 - zzf.steppingHeight());
		assertEquals(1, rel[0], tol);
		assertEquals(2, rel[1], tol);
	}
}
