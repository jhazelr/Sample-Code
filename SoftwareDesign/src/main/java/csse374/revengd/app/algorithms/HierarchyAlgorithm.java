package csse374.revengd.app.algorithms;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import soot.Hierarchy;
import soot.Scene;
import soot.SootMethod;
import soot.Unit;

public class HierarchyAlgorithm implements IAlgorithm {

	@Override
	public Set<SootMethod> runAlgorithm(Scene scene, Unit stmt, SootMethod method) {

		Hierarchy hierarchy = scene.getActiveHierarchy();
		Collection<SootMethod> possibleMethods = hierarchy.resolveAbstractDispatch(method.getDeclaringClass(), method);

		Set<SootMethod> returnList = new HashSet<>();

		returnList.addAll(possibleMethods);

		return returnList;
	}

}
