package csse374.revengd.app.transformer;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import csse374.revengd.app.algorithms.AggregateAlgorithm;
import csse374.revengd.app.filter.IFilter;
import csse374.revengd.app.model.Database;
import soot.Body;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.Stmt;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;

public class SDRenderingTransformer extends Transformer {

	private AggregateAlgorithm aggAlg;

	public SDRenderingTransformer(List<IFilter> filters) {
		super(filters);
	}

	@Override
	public Database transform(Database db) {
		Map<String, String> dbMap = db.getConfig();
		SootMethod method = getMethod(dbMap.get("-sd"), db.getFilteredObjects(Scene.class, "scene"));
		int number = Integer.parseInt(dbMap.get("-sdn"));
		StringBuilder plantumlCode = new StringBuilder();

		aggAlg = db.getFilteredObjects(AggregateAlgorithm.class, "aggAlg");

		boolean excludeJDK = dbMap.containsKey("-exc");

		plantumlCode.append("@startuml\n");
		plantumlCode.append(render(db.getFilteredObjects(Scene.class, "scene"), method, number, excludeJDK).toString());
		plantumlCode.append("@enduml\n");

		saveCode(plantumlCode.toString());

		return db;
	}

	private StringBuilder render(Scene scene, SootMethod method, int number, boolean excludeJDK) {
		if (number < 1) {
			return new StringBuilder();
		}

		if (!method.hasActiveBody()) {
			return new StringBuilder();
		}
		Body body = method.retrieveActiveBody();
		UnitGraph cfg = new ExceptionalUnitGraph(body);

		StringBuilder plantumlCode = new StringBuilder();
		cfg.forEach(stmt -> {

			SootMethod inMethod = null;
			Set<SootMethod> possibleMethods = null;
			if (stmt instanceof AssignStmt || stmt instanceof InvokeStmt) {
				if (stmt instanceof AssignStmt) {
					Value rightOp = ((AssignStmt) stmt).getRightOp();
					if (rightOp instanceof InvokeExpr) {
						InvokeExpr invkExpr = (InvokeExpr) rightOp;
						inMethod = invkExpr.getMethod();
						possibleMethods = aggAlg.resolveMethod(scene, stmt, inMethod);
					} else {
						return;
					}

				} else if (stmt instanceof InvokeStmt) {
					InvokeExpr invkExpr = ((Stmt) stmt).getInvokeExpr();
					inMethod = invkExpr.getMethod();

				}
				if (inMethod.isJavaLibraryMethod() && excludeJDK) {
					return;
				}

				if (possibleMethods == null) {
					possibleMethods = aggAlg.resolveMethod(scene, stmt, inMethod);
				}

				if (possibleMethods.isEmpty()) {
					return;
				}

				List<SootMethod> pM = new ArrayList<>();
				pM.addAll(possibleMethods);

				SootMethod officalMethod = pM.get(0);

				String note = createNote(pM);

				String methodName = "";

				if (officalMethod.toString().contains("<init>")) {
					methodName = "<<new>>";
				} else {
					methodName = officalMethod.getName().toString();
				}

				String temp = method.getDeclaringClass().getName() + " --> "
						+ officalMethod.getDeclaringClass().getName() + ": " + methodName + "(";
				List<Type> pramaters = officalMethod.getParameterTypes();
				for (int i = 0; i < pramaters.size(); i++) {
					temp += pramaters.get(i).toString() + ", ";
				}
				if (pramaters.size() > 0) {
					temp = temp.substring(0, temp.length() - 2);
				}
				temp += ")\n";
				plantumlCode.append(temp);
				plantumlCode.append(note);
				plantumlCode.append("activate " + officalMethod.getDeclaringClass().getName() + "\n");
				plantumlCode.append(render(scene, officalMethod, number - 1, excludeJDK).toString());
				if (!officalMethod.getReturnType().toString().equals("void")) {
					String temp2 = officalMethod.getDeclaringClass().getName() + " <-- "
							+ officalMethod.getDeclaringClass().getName() + ": " + officalMethod.getReturnType();
					plantumlCode.append(temp2 + "\n");
				}
				plantumlCode.append("deactivate " + officalMethod.getDeclaringClass().getName() + "\n");

			}

		});

		return plantumlCode;
	}

	private String createNote(List<SootMethod> possibleMethods) {
		// TODO Auto-generated method stub
		if (possibleMethods.size() < 2) {
			return "";
		}

		StringBuilder note = new StringBuilder();

		note.append("note left \nThe possible classes are: \n");

		for (int i = 1; i < possibleMethods.size(); i++) {
			SootMethod tempMethod = possibleMethods.get(i);
			note.append(tempMethod.getDeclaringClass().getName() + "\n");
		}

		note.append("end note\n");

		return note.toString();
	}

	private void saveCode(String umlCode) {
		try {
			OutputStream out = new FileOutputStream("./plant_uml_code/SDcode.txt");
			out.write(umlCode.getBytes());
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private SootMethod getMethod(String method, Scene scene) {
		int lastDot = method.lastIndexOf(".");
		String clazz = method.substring(0, lastDot);
		String methodName = method.substring(lastDot + 1, method.length() - 2);

		SootClass app = scene.getSootClass(clazz);
		SootMethod sootMethod = app.getMethodByName(methodName);
		return sootMethod;
	}

}
