package com.github.btrapp.fastflashfinder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.github.btrapp.fastflashfinder.FastFlashObjects.FlashAndDie;
import com.github.btrapp.fastflashfinder.FastFlashObjects.FlashDieInst;
import com.github.btrapp.fastflashfinder.FastFlashObjects.ZeroZeroFlashInst;

/**
 * Check some real world examples to ensure some historical data matches our
 * logic.
 */
public class RealWorldExampleTestCase {
	@Test
	void testSimpleProd() {
		DieInfo dieInfo = readDieList("SimpleProd");
		assertEquals(36, dieInfo.flashDies.size());
		List<ExpectedRecord> recs = readRecs("SimpleProd");
		assertEquals(261, recs.size());

		long t0 = Instant.now().toEpochMilli();
		ScanLineFlashFinder fff = new ScanLineFlashFinder(dieInfo.zzf(), dieInfo.flashDies);
		allDiesMatch(recs, fff);
		long t1 = Instant.now().toEpochMilli();
		XRangeFlashFinder sff = new XRangeFlashFinder(dieInfo.zzf(), dieInfo.flashDies);
		allDiesMatch(recs, sff);
		long t2 = Instant.now().toEpochMilli();
		// System.out.println("Timings: " + (t1 - t0) + " and " + (t2 - t1));

	}

	@Test
	void testMpwProd() {
		DieInfo dieInfo = readDieList("Mpw");
		assertEquals(28, dieInfo.flashDies.size());
		List<ExpectedRecord> recs = readRecs("Mpw");
		assertEquals(80, recs.size());
		// Uncomment this to emulate a case where we have to check a lot more dies
//		List<ExpectedRecord> lotsOfRecords = new ArrayList<>();
//		for (int i = 0; i < 10_000; i++) { // Get to 800_000 recs
//			lotsOfRecords.addAll(recs);
//		}
//		recs = lotsOfRecords;

		long t0 = Instant.now().toEpochMilli();
		ScanLineFlashFinder fff = new ScanLineFlashFinder(dieInfo.zzf(), dieInfo.flashDies);
		allDiesMatch(recs, fff);
		long t1 = Instant.now().toEpochMilli();
		XRangeFlashFinder sff = new XRangeFlashFinder(dieInfo.zzf(), dieInfo.flashDies);
		allDiesMatch(recs, sff);
		long t2 = Instant.now().toEpochMilli();
		// System.out.println("Timings: " + (t1 - t0) + " and " + (t2 - t1));

	}

	private void allDiesMatch(List<ExpectedRecord> recs, FlashFinderIf fff) {
		for (ExpectedRecord r : recs) {
			FlashAndDie fad = fff.findFlashAndDie(r.wx(), r.wy());
			assertEquals(r.fx(), fad.flashId().flashIdX(), "Fx check");
			assertEquals(r.fy(), fad.flashId().flashIdY(), "Fy check");
			assertEquals(r.die(), fad.die().dieId(), "Die check");
		}

	}

	record ExpectedRecord(double wx, double wy, int fx, int fy, int die) {

	}

	private List<ExpectedRecord> readRecs(String name) {
		List<ExpectedRecord> recs = new ArrayList<>();
		try (var fis = getClass().getClassLoader().getResourceAsStream("testCases/" + name + "_XYList.csv");
				BufferedReader br = new BufferedReader(new InputStreamReader(fis));) {
			String line;
			int col = 0;
			while ((line = br.readLine()) != null) {
				if (line.startsWith("#"))
					continue;
				String[] arr = line.split(",");
				if (arr.length >= 5) {
					col = 0;
					double wx = Double.parseDouble(arr[col++]);
					double wy = Double.parseDouble(arr[col++]);
					int fx = Integer.parseInt(arr[col++]);
					int fy = Integer.parseInt(arr[col++]);
					int die = Integer.parseInt(arr[col++]);
					recs.add(new ExpectedRecord(wx, wy, fx, fy, die));
				}
			}
			return recs;
		} catch (IOException e) {
			e.printStackTrace();
			assertTrue(false);
			return null;
		}
	}

	private record DieInfo(ZeroZeroFlashInst zzf, List<FlashDieInst> flashDies) {

	}

	private DieInfo readDieList(String name) {
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
			e.printStackTrace();
			assertTrue(false);
			return null;
		}

	}

	public long timeFlashThenDieLookup(List<ExpectedRecord> recs, ZeroZeroFlashInst zzf, List<FlashDieInst> dies) {
		XRangeFlashFinder sff = new XRangeFlashFinder(zzf, dies);

		long startTime = Instant.now().toEpochMilli();
		int nMatched = 0;
		for (ExpectedRecord rec : recs) {
			FlashAndDie fad = sff.findFlashAndDie(rec.wx, rec.wy);
			FlashDieInst die = fad.die();
			if (die != null) {
				nMatched++;
			}
			assertNotNull(die);
		}
		assertEquals(nMatched, recs.size());
		long endTime = Instant.now().toEpochMilli();
		return (endTime - startTime);
	}
}
