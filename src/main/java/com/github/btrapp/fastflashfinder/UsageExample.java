package com.github.btrapp.fastflashfinder;

import java.util.ArrayList;
import java.util.List;

import com.github.btrapp.fastflashfinder.FastFlashObjects.FastFlashException;
import com.github.btrapp.fastflashfinder.FastFlashObjects.FlashDieInst;
import com.github.btrapp.fastflashfinder.FastFlashObjects.ZeroZeroFlashInst;

public class UsageExample {
	public static void main(String[] args) throws FastFlashException {
		// A 2x2 grid:
		int dieId = 1;
		List<FlashDieInst> dies = new ArrayList<>();
		for (int x = 0; x < 2; x++) {
			for (int y = 0; y < 2; y++) {
				FlashDieInst d = new FlashDieInst(dieId, x, y, x + 1, y + 1);
				dieId++;
				dies.add(d);
			}
		}
		ZeroZeroFlashInst zeroZeroFlash = new ZeroZeroFlashInst(0, 0, 100, 50);

		FastFlashFinder fff = new FastFlashFinder(zeroZeroFlash, dies);

	}
}
