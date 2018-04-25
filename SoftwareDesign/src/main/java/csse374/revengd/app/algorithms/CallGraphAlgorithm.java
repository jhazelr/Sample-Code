package csse374.revengd.app.algorithms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import soot.MethodOrMethodContext;
import soot.Scene;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.toolkits.callgraph.CallGraph;

public class CallGraphAlgorithm implements IAlgorithm {

	@Override
	public Set<SootMethod> runAlgorithm(Scene scene, Unit stmt, SootMethod method) {
		CallGraph callGraph = scene.getCallGraph();

		Collection<SootMethod> targetMethods = new ArrayList<>();
		callGraph.edgesOutOf(stmt).forEachRemaining(edge -> {
			MethodOrMethodContext methodOrCntxt = edge.getTgt();
			SootMethod targetMethod = methodOrCntxt.method();
			if (targetMethod != null) {
				targetMethods.add(targetMethod);
			}
		});
		Set<SootMethod> returnT = new HashSet<SootMethod>();

		returnT.addAll(targetMethods);
		return returnT;
	}

}
