package csse374.revengd.app.algorithms;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import soot.Scene;
import soot.SootMethod;
import soot.Unit;

public class ChainAlgorthim implements IResolutionStragy {

	private List<IAlgorithm> algorithms;

	public ChainAlgorthim() {
		algorithms = new ArrayList<>();
	}

	public ChainAlgorthim(List<IAlgorithm> alg) {
		algorithms = alg;
	}

	public void addAlgorithm(IAlgorithm alg) {
		this.algorithms.add(alg);
	}

	public void addAlgorithmList(List<IAlgorithm> alg) {
		this.algorithms.addAll(alg);
	}

	@Override
	public Set<SootMethod> runAlgorithm(Scene scene, Unit stmt, SootMethod method) {

		for (int i = 0; i < algorithms.size(); i++) {
			IAlgorithm tempAlg = algorithms.get(i);
			Set<SootMethod> tempList = tempAlg.runAlgorithm(scene, stmt, method);
			if (!tempList.isEmpty()) {
				return tempList;
			}

		}

		return new HashSet<>();
	}

}
