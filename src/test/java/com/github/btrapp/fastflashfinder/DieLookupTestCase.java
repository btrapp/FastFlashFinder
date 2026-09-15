package com.github.btrapp.fastflashfinder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.github.btrapp.fastflashfinder.FastFlashObjects.DieEdgeMatchLogic;
import com.github.btrapp.fastflashfinder.FastFlashObjects.FlashDieInst;

public class DieLookupTestCase {
	@Test
	void testFlashDieStepMathSimple() {
		// A regular 3x2 grid of 1x1 dies
		// |x|x|x|
		// |x|x|x|
		int dieId = 1;
		List<FlashDieInst> dies = new ArrayList<>();
		for (int x = 0; x < 3; x++) {
			for (int y = 0; y < 2; y++) {
				FlashDieInst d = new FlashDieInst(dieId++, x, y, x + 1, y + 1);
				dies.add(d);
			}
		}

		FastFlashFinder fff = new FastFlashFinder(null, dies);
		fff.setDieEdgeLogic(DieEdgeMatchLogic.RIGHT_SIDE);
		double fx = 0.1;
		double fy = 0.1;
		double tol = 0.0001;
		FlashDieInst d;
		try {
			d = fff.findDieInstanceOrNullForFlashXY(fx, fy);
			assertTrue(matches(d, fx, fy), "Checking " + fx + "," + fy);

			fx = 2.5;
			fy = 1.5;
			d = fff.findDieInstanceOrNullForFlashXY(fx, fy);
			assertTrue(matches(d, fx, fy), "Checking " + fx + "," + fy);

			fx = 5; // Off in +x
			fy = 1.5;
			assertNull(fff.findDieInstanceOrNullForFlashXY(fx, fy));
			fx = -0.5; // Off in -x
			fy = 1.5;
			assertNull(fff.findDieInstanceOrNullForFlashXY(fx, fy));

			fx = 2.5;
			fy = -0.5; // Off in -y
			assertNull(fff.findDieInstanceOrNullForFlashXY(fx, fy));
			fx = 2.5;
			fy = 12.5; // Off in +y
			assertNull(fff.findDieInstanceOrNullForFlashXY(fx, fy));

			// Check an exact edge match in X
			FlashDieInst edgeX = fff.findDieInstanceOrNullForFlashXY(1.0, 0.5);
			assertEquals(0, edgeX.llx(), tol); // Should match the die that starts at 0 and END at 1;
			edgeX = fff.findDieInstanceOrNullForFlashXY(2.0, 0.5);
			assertEquals(1, edgeX.llx(), tol); // Should match the die that starts at 1 and END at 2;
			edgeX = fff.findDieInstanceOrNullForFlashXY(0, 0.5);
			assertNull(edgeX, "Should match no die");

			FlashDieInst edgeY = fff.findDieInstanceOrNullForFlashXY(0.5, 1.0);
			assertEquals(0, edgeY.lly(), tol); // Should match the die that starts at 0 and END at 1;

		} catch (Exception ex) {
			ex.printStackTrace();
			assertFalse(true, "Exception thrown");
		}
	}

	private boolean matches(FlashDieInst d0, double fx, double fy) {
		if (fx <= d0.llx() || fx > d0.urx()) {
			System.out.println("X fail " + d0.llx() + " <= " + fx + " > " + d0.urx());
			return false;
		}
		if (fy <= d0.lly() || fy > d0.ury()) {
			System.out.println("Y fail " + d0.lly() + " <= " + fy + " > " + d0.ury());
			return false;
		}
		return true;
	}
}
