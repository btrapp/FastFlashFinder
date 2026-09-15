package com.github.btrapp.fastflashfinder;

import com.github.btrapp.fastflashfinder.FastFlashObjects.DieEdgeMatchLogic;
import com.github.btrapp.fastflashfinder.FastFlashObjects.FlashAndDie;

public interface DieFinderIf {
	public FlashAndDie findFlashAndDie(double waferX, double waferY);

	public void setDieEdgeLogic(DieEdgeMatchLogic dieEdgeLogic);

}
