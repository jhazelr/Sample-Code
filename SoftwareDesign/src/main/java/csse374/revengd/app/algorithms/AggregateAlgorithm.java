package csse374.revengd.app.algorithms;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import soot.Scene;
import soot.SootMethod;
import soot.Unit;

public class AggregateAlgorithm {
	private List<IAlgorithm> algorithms;
	private IResolutionStragy stratgy;

	public AggregateAlgorithm(IResolutionStragy strat) {
		algorithms = new ArrayList<>();
		stratgy = strat;
	}

	public void addAlgorithm(IAlgorithm alg) {
		this.algorithms.add(alg);
	}

	public Set<SootMethod> resolveMethod(Scene scene, Unit stmt, SootMethod method) {
		return stratgy.runAlgorithm(scene, stmt, method);
	}

}
