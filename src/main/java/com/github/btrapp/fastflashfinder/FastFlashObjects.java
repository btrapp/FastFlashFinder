package com.github.btrapp.fastflashfinder;

import java.util.Objects;

public interface FastFlashObjects {
	public record FlashId(int flashIdX, int flashIdY) {

	}

	public record ZeroZeroFlashInst(double llx, double lly, double steppingWidth, double steppingHeight) {
		public double[] calcFlashLlXy(FlashId fid) {
			return new double[] { llx + (fid.flashIdX() * steppingWidth), lly + (fid.flashIdY() * steppingHeight), };
		}
	}

	/**
	 * Each die on a flash should have an instace of this. The dieId is unique among
	 * the dies on the flash. Location coordinates should be in um *relative to
	 * flash 0,0 at lower left*
	 * 
	 * Equality really only should be checking the dieId
	 */
	public record FlashDieInst(Integer dieId, double llx, double lly, double urx, double ury) {

		@Override
		public int hashCode() {
			return Objects.hash(dieId);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			FlashDieInst other = (FlashDieInst) obj;
			return dieId.equals(other.dieId);
		}

		public boolean containsXY(double x, double y, DieEdgeMatchLogic edgeLogic) {
			if (edgeLogic == DieEdgeMatchLogic.EITHER_SIDE) {
				if (x < llx || x > urx) {
					return false;
				}
				if (y < lly || y > ury) {
					return false;
				}
				return true;
			}
			if (edgeLogic == DieEdgeMatchLogic.LEFT_SIDE) {
				if (x < llx || x >= urx) {
					return false;
				}
				if (y < lly || y >= ury) {
					return false;
				}
				return true;
			}
			if (edgeLogic == DieEdgeMatchLogic.RIGHT_SIDE) {
				if (x <= llx || x > urx) {
					return false;
				}
				if (y <= lly || y > ury) {
					return false;
				}
				return true;
			}
			return false;
		}

	}

	public static enum FlastFlashExceptionErrorCode {
		OVERLAPPING_DIES;
	}

	public static enum DieEdgeMatchLogic {
		EITHER_SIDE, LEFT_SIDE, RIGHT_SIDE;
	}

	public static final class FastFlashException extends Exception {
		private static final long serialVersionUID = -5185421960510705220L;
		FlastFlashExceptionErrorCode code;

		public FastFlashException(FlastFlashExceptionErrorCode code, String msg) {
			super(msg);
			this.code = code;
		}
	}
}
