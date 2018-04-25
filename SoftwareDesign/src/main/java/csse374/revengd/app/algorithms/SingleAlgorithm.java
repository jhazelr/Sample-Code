package csse374.revengd.app.algorithms;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import soot.Scene;
import soot.SootMethod;
import soot.Unit;

public class SingleAlgorithm implements IResolutionStragy {

	private IAlgorithm algorithm;

	public SingleAlgorithm(IAlgorithm algorithm) {
		this.algorithm = algorithm;
	}

	public SingleAlgorithm() {

	}

	@Override
	public Set<SootMethod> runAlgorithm(Scene scene, Unit stmt, SootMethod method) {
		Set<SootMethod> returnList = new HashSet<>();
		Set<SootMethod> temp = algorithm.runAlgorithm(scene, stmt, method);
		if (temp.isEmpty()) {
			return temp;
		}
		returnList.add(temp.iterator().next());
		return returnList;
	}

	@Override
	public void addAlgorithm(IAlgorithm alg) {
		if (this.algorithm == null) {
			this.algorithm = alg;
		}
	}

	@Override
	public void addAlgorithmList(List<IAlgorithm> alg) {
		this.algorithm = alg.get(0);
	}

}
