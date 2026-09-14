package com.github.btrapp.fastflashfinder;

import java.util.Objects;

public interface FastFlashObjects {
	public record FlashId(int flashIdX, int flashIdY) {
		
	}
	public record FlashInst(double llx, double lly, double steppingWidth, double steppingHeight) {
		
	}
	/**
	 * Each die on a flash should have an instace of this.  The dieId is unique
	 * among the dies on the flash.
	 * Location coordinates should be in um *relative to flash 0,0 at lower left*
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
		
		
	}
	public static enum FlastFlashExceptionErrorCode {
		OVERLAPPING_DIES;
	}
	public static final class FastFlashException extends Exception {
		private static final long serialVersionUID = -5185421960510705220L;
		FlastFlashExceptionErrorCode code;
		public FastFlashException(FlastFlashExceptionErrorCode code, String msg) {
			super(msg);
			this.code=code;
		}
	}
}
