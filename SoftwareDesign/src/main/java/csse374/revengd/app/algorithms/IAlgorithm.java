package csse374.revengd.app.algorithms;

import java.util.Set;

import soot.Scene;
import soot.SootMethod;
import soot.Unit;

public interface IAlgorithm {

	public Set<SootMethod> runAlgorithm(Scene scene, Unit stmt, SootMethod method);
}
