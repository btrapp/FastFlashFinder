package com.github.btrapp.fastflashfinder;

import java.util.ArrayList;
import java.util.List;

import com.github.btrapp.fastflashfinder.FastFlashObjects.FastFlashException;
import com.github.btrapp.fastflashfinder.FastFlashObjects.FlashDieInst;
import com.github.btrapp.fastflashfinder.FastFlashObjects.FlashId;
import com.github.btrapp.fastflashfinder.FastFlashObjects.ZeroZeroFlashInst;

public class Usage {
	public static void main(String[] args) throws FastFlashException {
		// A readlly dense die map:
		int dieId = 1;
		List<FlashDieInst> dies = new ArrayList<>();
		for (int x = 0; x < 2; x++) {
			for (int y = 0; y < 2; y++) {
				FlashDieInst d = new FlashDieInst(dieId, x, y, x + 1, y + 1);
				dieId++;
				dies.add(d);
			}
		}
		/*
		 * ZeroZeroFlashInst[llx=-16764.0, lly=-14040.0, steppingWidth=25128.0,
		 * steppingHeight=32880.0] ExpectedRecord[wx=-63698.789, wy=-127552.992, fx=-2,
		 * fy=-4, die=13] FlashAndDie[flashId=FlashId[flashIdX=-5, flashIdY=-2],
		 * die=null]
		 */
		ZeroZeroFlashInst zeroZeroFlash = new ZeroZeroFlashInst(-16764.0, -14040.0, 25128.0, 32880.0);
		ScanLineFlashFinder fff = new ScanLineFlashFinder(zeroZeroFlash, dies);
		FlashId flashId = CommonFlashUtils.findFlashId(zeroZeroFlash, -63698.789, -127552.992);
		System.out.println(flashId);

	}
}
