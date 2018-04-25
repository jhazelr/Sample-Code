package csse374.revengd.app.transformer;

import java.util.Collection;
import java.util.List;

import csse374.revengd.app.filter.IFilter;
import csse374.revengd.app.model.Database;
import edu.rosehulman.jvm.sigevaluator.GenericType;
import edu.rosehulman.jvm.sigevaluator.MethodEvaluator;
import soot.Body;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.tagkit.Tag;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;

public class DependencyTransformer extends Transformer {
	List<IFilter> filters;

	public DependencyTransformer(List<IFilter> filters) {
		super(filters);
		this.filters = filters;
	}

	@Override
	public Database transform(Database db) {
		DependencyUpdater updater = new DependencyUpdater(db);
		@SuppressWarnings("unchecked")
		List<Object> objs = db.getFilteredObjects(List.class, "classes");
		Collection<SootClass> clazzes = db.castElements(SootClass.class, objs);
		for (SootClass clazz : clazzes) {
			for (SootMethod method : clazz.getMethods()) {
				if (shouldProcess(method)) {
					// Check body
					if (method.hasActiveBody()) {
						Body body = method.retrieveActiveBody();
						UnitGraph cfg = new ExceptionalUnitGraph(body);
						cfg.forEach(stmt -> {
							if (stmt instanceof AssignStmt) {
								Value rightOp = ((AssignStmt) stmt).getRightOp();
								String rightClass = rightOp.getType().toString();
								updater.findDependency(clazz, rightClass, false, false);
							}
						});
					}

					// Check return type
					boolean useSoot = true;
					Tag signatureTag = method.getTag("SignatureTag");
					if (signatureTag != null) {
						useSoot = false;
						String signature = signatureTag.toString();
						MethodEvaluator evaluator = new MethodEvaluator(signature);
						try {
							GenericType returnType = evaluator.getReturnType();
							if (returnType.getContainerType() != null) {
								for (String element : returnType.getAllElementTypes()) {
									updater.findDependency(clazz, element, false, true);
								}
							} else {
								updater.findDependency(clazz, returnType.toString(), false, false);
							}
						} catch (IllegalStateException e) {
							useSoot = true;
						}
					}
					if (useSoot) {
						updater.findDependency(clazz, method.getReturnType().toString(), false, false);
					}

					// Check parameters
					useSoot = true;
					if (signatureTag != null) {
						useSoot = false;
						String signature = signatureTag.toString();
						MethodEvaluator evaluator = new MethodEvaluator(signature);
						try {
							List<GenericType> params = evaluator.getParameterTypes();
							for (GenericType param : params) {
								if (param.getContainerType() != null) {
									for (String element : param.getAllElementTypes()) {
										updater.findDependency(clazz, element, false, true);
									}
								} else {
									updater.findDependency(clazz, param.toString(), false, false);
								}
							}
						} catch (IllegalStateException e) {
							useSoot = true;
						}
					}
					if (useSoot) {
						for (Type paramType : method.getParameterTypes()) {
							updater.findDependency(clazz, paramType.toString(), false, false);
						}
					}
				}
			}
		}
		return db;
	}

	private boolean shouldProcess(SootMethod method) {
		for (IFilter filter : this.filters) {
			if (!filter.shouldProcessMethod(method)) {
				return false;
			}
		}
		return true;
	}
}
