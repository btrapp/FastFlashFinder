package com.github.btrapp.fastflashfinder;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.github.btrapp.fastflashfinder.FastFlashObjects.FlashDieInst;
import com.github.btrapp.fastflashfinder.FastFlashObjects.FlashId;
import com.github.btrapp.fastflashfinder.FastFlashObjects.ZeroZeroFlashInst;

/**
 * Check some real world examples to ensure some historical data matches our logic.
 */
public class RealWorldExampleTestCase {
	@Test
	void testSimpleProd() {
		DieInfo dieInfo = readDieList("SimpleProd");
		assertEquals(36, dieInfo.flashDies.size());
		List<ExpectedRecord> recs = readRecs("SimpleProd");
		assertEquals(261, recs.size());

		FastFlashFinder fff = new FastFlashFinder(dieInfo.zzf, dieInfo.flashDies);
		allDiesMatch(recs, fff);

	}

	@Test
	void testMpwProd() {
		DieInfo dieInfo = readDieList("Mpw");
		assertEquals(28, dieInfo.flashDies.size());
		List<ExpectedRecord> recs = readRecs("Mpw");
		assertEquals(80, recs.size());

		FastFlashFinder fff = new FastFlashFinder(dieInfo.zzf, dieInfo.flashDies);
		allDiesMatch(recs, fff);
	}

	private void allDiesMatch(List<ExpectedRecord> recs, FastFlashFinder fff) {
		for (ExpectedRecord r : recs) {
			FlashId flashId = fff.findFlash(r.wx(), r.wy());
			assertEquals(r.fx(), flashId.flashIdX());
			assertEquals(r.fy(), flashId.flashIdY());
			FlashDieInst die = fff.findDieInstanceOrNull(flashId, r.wx(), r.wy());
			assertEquals(r.die(), die.dieId());
		}

	}

	record ExpectedRecord(double wx, double wy, int fx, int fy, int die) {

	}

	public List<ExpectedRecord> readRecs(String name) {
		List<ExpectedRecord> recs = new ArrayList<>();
		try (var fis = getClass().getClassLoader().getResourceAsStream("testCases/" + name + "_XYList.csv");
				BufferedReader br = new BufferedReader(new InputStreamReader(fis));) {
			String line;
			int col = 0;
			while ((line = br.readLine()) != null) {
				if (line.startsWith("#"))
					continue;
				String[] arr = line.split(",");
				if (arr.length == 5) {
					col = 0;
					double wx = Double.parseDouble(arr[col++]);
					double wy = Double.parseDouble(arr[col++]);
					int fx = Integer.parseInt(arr[col++]);
					int fy = Integer.parseInt(arr[col++]);
					int die = Integer.parseInt(arr[col++]);
					ExpectedRecord er = new ExpectedRecord(wx, wy, fx, fy, die);
					recs.add(er);
				}
			}
			return recs;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	private record DieInfo(ZeroZeroFlashInst zzf, List<FlashDieInst> flashDies) {

	}

	public DieInfo readDieList(String name) {
		try (var fis = getClass().getClassLoader().getResourceAsStream("testCases/" + name + "_DieList.csv");
				BufferedReader br = new BufferedReader(new InputStreamReader(fis));) {
			String line;
			double flashLLX = 0;
			double flashLLY = 0;
			double flashW = 0;
			double flashH = 0;

			List<FlashDieInst> dies = new ArrayList<>();
			while ((line = br.readLine()) != null) {
				String[] arr = line.split(",");
				if (arr[0].startsWith("#")) {
					if (arr[0].equalsIgnoreCase("#FlashLLX"))
						flashLLX = Double.valueOf(arr[1]);
					if (arr[0].equalsIgnoreCase("#FlashLLY"))
						flashLLY = Double.valueOf(arr[1]);
					if (arr[0].equalsIgnoreCase("#FlashW"))
						flashW = Double.valueOf(arr[1]);
					if (arr[0].equalsIgnoreCase("#FlashH"))
						flashH = Double.valueOf(arr[1]);
				} else if (arr.length == 5) {
					int dieId = Integer.valueOf(arr[0]);
					double llx = Double.valueOf(arr[1]);
					double lly = Double.valueOf(arr[2]);
					double w = Double.valueOf(arr[3]);
					double h = Double.valueOf(arr[4]);
					FlashDieInst fdi = new FlashDieInst(dieId, llx, lly, llx + w, lly + h);
					dies.add(fdi);
				}

			}
			ZeroZeroFlashInst zzf = new ZeroZeroFlashInst(flashLLX, flashLLY, flashW, flashH);
			return new DieInfo(zzf, dies);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

	}

	//	private record Rect(int id, double llx, double lly, double urx, double ury) {
	//
	//	}
	//
	//	public long timeFlashThenDieLookup(List<ExpectedRecord> recs, ZeroZeroFlashInst zzf, List<FlashDieInst> dies) {
	//		int flashId = 0;
	//		int dieId = 0;
	//		Map<Rect, List<Rect>> sillyMap = new HashMap<>();
	//		for (int fx = -20; fx < 20; fx++) {
	//			double fllx = zzf.llx() + (fx * zzf.steppingWidth());
	//			for (int fy = -20; fy < 20; fy++) {
	//				double flly = zzf.lly() + (fy * zzf.steppingHeight());
	//				List<Rect> diesForFlash = new ArrayList<>();
	//				for (FlashDieInst fdi : dies) {
	//					double dllx = fllx + fdi.llx();
	//					double dlly = flly + fdi.lly();
	//					double durx = fllx + fdi.urx();
	//					double dury = flly + fdi.ury();
	//					double centerx = dllx + ((durx - dllx) / 2.0);
	//					double centery = dlly + ((dury - dlly) / 2.0);
	//					double r = Math.sqrt((centerx * centerx) + (centery * centery));
	//					if (r < 147_000) { // rougly on wafer
	//						Rect drect = new Rect(dieId++, dllx, dlly, durx, dury);
	//						diesForFlash.add(drect);
	//					}
	//				}
	//				if (!diesForFlash.isEmpty()) {
	//					Rect frect = new Rect(flashId++, fllx, flly, fllx + zzf.steppingWidth(),
	//							flly + zzf.steppingHeight());
	//					sillyMap.put(frect, diesForFlash);
	//				}
	//			}
	//		}
	//
	//		long startTime = Instant.now().toEpochMilli();
	//		BiPredicate<Rect, ExpectedRecord> recMatch = (rec, er) -> {
	//			if (rec.llx < er.wx && er.wx <= rec.urx)
	//				return false;
	//			if (rec.lly < er.wy && er.wy <= rec.ury)
	//				return false;
	//			return true;
	//		};
	//		int nMatched = 0;
	//		for (ExpectedRecord rec : recs) {
	//			Rect frect = sillyMap.keySet().stream().filter(r -> recMatch.test(r, rec)).findFirst().orElse(null);
	//			Rect drect = null;
	//			if (frect != null) {
	//				List<Rect> drects = sillyMap.get(frect);
	//				drect = drects.stream().filter(r -> recMatch.test(r, rec)).findFirst().orElse(null);
	//				nMatched++;
	//			}
	//			assertNotNull(drect);
	//		}
	//		assertEquals(nMatched, recs.size());
	//		long endTime = Instant.now().toEpochMilli();
	//		return (endTime - startTime);
	//	}
}
