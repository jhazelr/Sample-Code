package csse374.revengd.app.transformer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import csse374.revengd.app.filter.IFilter;
import csse374.revengd.app.model.Database;
import csse374.revengd.app.model.Dependency;
import csse374.revengd.app.model.Pattern;
import csse374.revengd.app.model.Dependency.RELATIONSHIP;
import soot.Body;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Type;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.Stmt;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.util.Chain;

public class AdapterTransformer extends Transformer {
	private String name;
	private double ratio;
	private double timesCalled = 0;
	private Database db;
	private Scene scene;
	private int countInMethod = 0;

	public AdapterTransformer(List<IFilter> filters) {
		super(filters);
	}

	public AdapterTransformer() {
		super(new ArrayList<IFilter>());
	}

	@Override
	public Database transform(Database db) {
		// TODO Auto-generated method stub
		this.db = db;
		Map<String, String> dbMap = db.getConfig();
		if (dbMap.containsKey("-adapterRatio")) {
			this.ratio = Double.parseDouble(dbMap.get("-adapterRatio"));
		} else {
			this.ratio = 0.75;
		}

		@SuppressWarnings("unchecked")
		List<Object> objs = db.getFilteredObjects(List.class, "classes");
		Collection<SootClass> clazzes = db.castElements(SootClass.class, objs);
		this.scene = db.getFilteredObjects(Scene.class, "scene");

		determinePattern(clazzes);

		return this.db;
	}

	private void determinePattern(Collection<SootClass> clazzes) {
		for (SootClass clazz : clazzes) {
			int interfaceCount = clazz.getInterfaceCount();
			if ((interfaceCount > 0) || (clazz.hasSuperclass() && clazz.getSuperclass().isAbstract())) {

				List<SootMethod> currentMethods = clazz.getMethods();
				if (interfaceCount > 0) {
					Chain<SootClass> interfaces = clazz.getInterfaces();
					for (SootClass c1 : interfaces) {
						List<SootMethod> interfaceMethods = c1.getMethods();
						List<SootMethod> finalMethods = compareMethods(currentMethods, interfaceMethods);

						determineIfAdaptor(finalMethods, clazz, c1);

					}
				} else if (clazz.getSuperclass().isAbstract()) {
					SootClass superClass = clazz.getSuperclass();
					List<SootMethod> superMethods = superClass.getMethods();
					List<SootMethod> finalMethods = compareMethods(currentMethods, superMethods);

					determineIfAdaptor(finalMethods, clazz, superClass);

				}

			}
		}
	}

	private void determineIfAdaptor(List<SootMethod> finalMethods, SootClass clazz, SootClass superClazz) {
		// TODO Auto-generated method stub
		Chain<SootField> fields = clazz.getFields();
		List<SootField> fieldList = new ArrayList<>();

		fieldList.addAll(fields);

		double methodCount = finalMethods.size();
		for (int i = 0; i < fieldList.size(); i++) {
			Type fieldClass = fieldList.get(i).getType();
			timesCalled = 0;
			for (int j = 0; j < finalMethods.size(); j++) {
				countInMethod = 0;
				SootMethod method = finalMethods.get(j);
				if (!method.hasActiveBody()) {
					continue;
				}
				Body body = method.retrieveActiveBody();
				System.out.println(method.getName());
				UnitGraph cfg = new ExceptionalUnitGraph(body);
				cfg.forEach(stmt -> {
					SootMethod inMethod = null;
					if (stmt instanceof AssignStmt || stmt instanceof InvokeStmt) {
						if (stmt instanceof AssignStmt) {
							Value rightOp = ((AssignStmt) stmt).getRightOp();
							if (rightOp instanceof InvokeExpr) {
								InvokeExpr invkExpr = (InvokeExpr) rightOp;
								inMethod = invkExpr.getMethod();
							} else {
								return;
							}
						} else if (stmt instanceof InvokeStmt) {
							InvokeExpr invkExpr = ((Stmt) stmt).getInvokeExpr();
							inMethod = invkExpr.getMethod();
						}
						if (fieldClass.toString().equals(inMethod.getDeclaringClass().toString())
								&& countInMethod < 1) {
							timesCalled++;
							countInMethod++;
						}
					}
				});

			}

			if ((timesCalled / methodCount) >= this.ratio) {
				System.out.println("===========WE GOT THE PATTERN!!!!===========");
				System.out.println((timesCalled / methodCount));

				SootClass adapteeClass = scene.getSootClass(fieldClass.toString());
				// Found Adapter
				addPattern(clazz, superClazz, adapteeClass);
			}
		}
	}

	private void addPattern(SootClass clazz, SootClass superClazz, SootClass adapteeClass) {
		Pattern newPattern = new Pattern(name);
		newPattern.putComponent("adapter", clazz);
		newPattern.putComponent("target", superClazz);
		newPattern.putComponent("adaptee", adapteeClass);

		Dependency dependency = new Dependency(clazz, adapteeClass, RELATIONSHIP.ASSOCIATES);
		newPattern.addRelation(dependency);

		@SuppressWarnings("unchecked")
		List<Object> depen = db.getFilteredObjects(List.class, "dependencies");
		Collection<Dependency> dependencies = this.db.castElements(Dependency.class, depen);
		dependencies.add(dependency);

		@SuppressWarnings("unchecked")
		List<Object> pats = db.getFilteredObjects(List.class, "patterns");
		Collection<Pattern> patterns = db.castElements(Pattern.class, pats);
		patterns.add(newPattern);
		db.putObject("patterns", patterns);

		@SuppressWarnings("unchecked")
		List<Object> objs = db.getFilteredObjects(List.class, "classes");
		Collection<SootClass> dbClasses = db.castElements(SootClass.class, objs);

		if (!dbClasses.contains(superClazz)) {
			dbClasses.add(superClazz);
			if (superClazz.isInterface()) {
				Dependency supdependency = new Dependency(clazz, adapteeClass, RELATIONSHIP.IMPLEMENTS);
				dependencies.add(supdependency);
			} else {
				Dependency supdependency = new Dependency(clazz, adapteeClass, RELATIONSHIP.EXTENDS);
				dependencies.add(supdependency);
			}

		}
		if (!dbClasses.contains(adapteeClass)) {
			dbClasses.add(adapteeClass);
		}

		this.db.putObject("dependencies", dependencies);
		db.putObject("classes", dbClasses);

	}

	private List<SootMethod> compareMethods(List<SootMethod> currentMethods, List<SootMethod> superMethods) {
		// TODO Auto-generated method stub
		List<SootMethod> methods = new ArrayList<>();

		for (SootMethod sm1 : currentMethods) {
			for (SootMethod sm2 : superMethods) {

				boolean sameName = sm1.getName().equals(sm2.getName());
				if (!sameName) {
					continue;
				}
				boolean samePrameters = (sm1.getParameterTypes().containsAll(sm2.getParameterTypes()));
				if (!samePrameters) {
					continue;
				}
				boolean sameReturnType = sm1.getReturnType().toString().equals("java.lang.Object")
						|| sm1.getReturnType().toString().equals("java.lang.Object");

				if (sameName && samePrameters && !sameReturnType) {
					methods.add(sm1);
				}

			}
		}

		return methods;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}
}
