package csse374.revengd.app.algorithms;

import java.util.List;
import java.util.Set;

import soot.Scene;
import soot.SootMethod;
import soot.Unit;

public interface IResolutionStragy {

	public Set<SootMethod> runAlgorithm(Scene scene, Unit stmt, SootMethod method);

	public void addAlgorithm(IAlgorithm alg);

	public void addAlgorithmList(List<IAlgorithm> alg);
}
