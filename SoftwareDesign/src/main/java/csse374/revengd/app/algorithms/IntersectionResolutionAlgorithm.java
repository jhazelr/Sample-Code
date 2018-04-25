package csse374.revengd.app.algorithms;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import soot.Scene;
import soot.SootMethod;
import soot.Unit;

public class IntersectionResolutionAlgorithm implements IResolutionStragy {

	private List<IAlgorithm> algorithms;

	public IntersectionResolutionAlgorithm() {
		algorithms = new ArrayList<>();
	}

	public IntersectionResolutionAlgorithm(List<IAlgorithm> alg) {
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
		Set<SootMethod> methods = new HashSet<>();
		Set<SootMethod> returnList = new HashSet<>();
		if (algorithms.size() > 1) {
			IAlgorithm tempAlg = algorithms.get(0);
			Set<SootMethod> tempList = tempAlg.runAlgorithm(scene, stmt, method);
			methods.addAll(tempList);
		}

		for (int i = 1; i < algorithms.size(); i++) {
			IAlgorithm tempAlg = algorithms.get(i);
			Set<SootMethod> tempList = tempAlg.runAlgorithm(scene, stmt, method);
			methods.retainAll(tempList);
		}

		returnList.addAll(methods);

		return returnList;
	}
}
