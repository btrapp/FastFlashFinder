package com.github.btrapp.fastflashfinder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.github.btrapp.fastflashfinder.FastFlashObjects.FlashDieInst;
import com.github.btrapp.fastflashfinder.FastFlashObjects.FlashId;
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
			FlashId flashId = fff.findFlashId(r.wx(), r.wy());
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

	public long timeFlashThenDieLookup(List<ExpectedRecord> recs, ZeroZeroFlashInst zzf, List<FlashDieInst> dies) {
		SimpleFlashFinder sff = new SimpleFlashFinder(zzf, dies);

		long startTime = Instant.now().toEpochMilli();
		int nMatched = 0;
		for (ExpectedRecord rec : recs) {
			FlashId fid = sff.findFlashId(rec.wx, rec.wy);
			FlashDieInst die = sff.findDieInstanceOrNull(fid, rec.wx, rec.wy);
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
