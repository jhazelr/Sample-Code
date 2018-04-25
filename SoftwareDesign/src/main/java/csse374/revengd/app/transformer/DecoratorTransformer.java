package csse374.revengd.app.transformer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.Stmt;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;

public class DecoratorTransformer extends Transformer {
	private String name;
	boolean isFound;

	public DecoratorTransformer() {
		super(new ArrayList<IFilter>());
	}

	public DecoratorTransformer(List<IFilter> filters) {
		super(filters);
	}

	@Override
	public Database transform(Database db) {
		@SuppressWarnings("unchecked")
		List<Object> objs = db.getFilteredObjects(List.class, "classes");
		Collection<SootClass> clazzes = db.castElements(SootClass.class, objs);
		for (SootClass clazz : clazzes) {
			Pattern pattern = new Pattern(name);
			if (isMinimumDecorator(clazz, db, pattern)) {
				checkDecoratedMethods(clazz, pattern);
				checkSecondLevelDecorators(clazz, clazzes, pattern);

				Dependency dependency = new Dependency(clazz, clazz.getSuperclass(), RELATIONSHIP.ASSOCIATES);
				pattern.addRelation(dependency);

				@SuppressWarnings("unchecked")
				List<Object> pats = db.getFilteredObjects(List.class, "patterns");
				Collection<Pattern> patterns = db.castElements(Pattern.class, pats);
				patterns.add(pattern);
				db.putObject("patterns", patterns);
			}
		}
		return db;
	}

	private boolean isMinimumDecorator(SootClass clazz, Database db, Pattern pattern) {
		@SuppressWarnings("unchecked")
		List<Object> objs = db.getFilteredObjects(List.class, "classes");
		Collection<SootClass> clazzes = db.castElements(SootClass.class, objs);
		if (clazz.hasSuperclass() && !clazz.getSuperclass().toString().equals("java.lang.Object")) {
			for (SootField field : clazz.getFields()) {
				if (field.getType().toString().equals(clazz.getSuperclass().toString())) {
					pattern.putComponent("component", clazz.getSuperclass());
					return true;
				} else {
					boolean found = false;
					List<SootClass> potentialComponents = new ArrayList<>();
					SootClass next = db.getFilteredObjects(Scene.class, "scene")
							.getSootClass(field.getType().toString());
					while (next.hasSuperclass()) {
						potentialComponents.add(next);
						if (next.getSuperclass().toString().equals("java.lang.Object")) {
							break;
						}
						if (next.getSuperclass().toString().equals(clazz.getSuperclass().toString())) {
							found = true;
							next = next.getSuperclass();
							potentialComponents.add(next);
							break;
						}
						next = next.getSuperclass();
					}
					if (found) {
						for (SootClass comp : potentialComponents) {
							pattern.putComponent("component", comp);
						}
						return true;
					}
				}
			}
		}
		return false;
	}

	// check if decorator, which is a first-level decorator, overrides all
	// the methods from component and invokes component in each of them.
	private void checkDecoratedMethods(SootClass decorator, Pattern pattern) {
		SootClass component = pattern.getComponents().get("component").get(pattern.getComponents().size() - 1);
		boolean isBad = false;
		for (SootMethod methodFromComponent : component.getMethods()) {
			boolean decoratesMethod = false;
			for (SootMethod methodFromDecorator : decorator.getMethods()) {
				String sigFromCom = methodFromComponent.getSignature();
				sigFromCom = sigFromCom.substring(sigFromCom.indexOf(":"));
				String sigFromDec = methodFromDecorator.getSignature();
				sigFromDec = sigFromDec.substring(sigFromDec.indexOf(":"));
				if (sigFromCom.equals(sigFromDec)) {
					decoratesMethod = isInvoked(methodFromDecorator, component);
					break;
				}
			}
			if (!decoratesMethod && !methodFromComponent.isConstructor()) {
				isBad = true;
				pattern.addMethod(methodFromComponent);
			}
		}
		if (isBad) {
			pattern.putComponent("Bad decorator", decorator);
		} else {
			pattern.putComponent("decorator", decorator);
		}
	}

	// check if there is any second-level decorator that extends to decorator
	// and check if each second-level decorator is a good decorator
	private void checkSecondLevelDecorators(SootClass decorator, Collection<SootClass> clazzes, Pattern pattern) {
		SootClass component = pattern.getComponents().get("component").get(0);
		for (SootClass c : clazzes) {
			if (c.hasSuperclass() && c.getSuperclass().toString().equals(decorator.toString())) {
				boolean isMinimumDecorator = false;
				isMinimumDecorator = true;
				if (isMinimumDecorator) {
					Dependency dependency = new Dependency(c, component, RELATIONSHIP.ASSOCIATES);
					pattern.addRelation(dependency);
					if (hasInvokedComponentInOverriddenMethods(c, decorator)) {
						if (!pattern.getComponents().get("decorator").contains(c)) {
							pattern.putComponent("decorator", c);
						}
					} else {
						if (!pattern.getComponents().get("Bad decorator").contains(c)) {
							pattern.putComponent("Bad decorator", c);
						}
					}
				}
			}
		}
	}

	// check if component is called in every overridden method
	private boolean hasInvokedComponentInOverriddenMethods(SootClass concreteDecorator, SootClass component) {
		// TODO: loop through all the overridden methods in concreteDecorator,
		// and check if each of them calls component by calling isInvoked()
		for (SootMethod methodFromDecorator : concreteDecorator.getMethods()) {
			for (SootMethod methodFromComponent : component.getMethods()) {
				String sigFromCom = methodFromComponent.getSignature();
				sigFromCom = sigFromCom.substring(sigFromCom.indexOf(":"));
				String sigFromDec = methodFromDecorator.getSignature();
				sigFromDec = sigFromDec.substring(sigFromDec.indexOf(":"));
				if (sigFromCom.equals(sigFromDec)) {
					return isInvoked(methodFromDecorator, component);
				}
			}
		}
		return true;
	}

	// TODO: check if method calls component
	private boolean isInvoked(SootMethod method, SootClass component) {
		// Check for invoking a method on component
		this.isFound = false;
		if (method.hasActiveBody()) {
			Body body = method.retrieveActiveBody();
			UnitGraph cfg = new ExceptionalUnitGraph(body);
			cfg.forEach(stmt -> {
				SootMethod inMethod = null;
				if (stmt instanceof AssignStmt || stmt instanceof InvokeStmt) {
					if (stmt instanceof AssignStmt) {
						Value rightOp = ((AssignStmt) stmt).getRightOp();
						if (rightOp instanceof InvokeExpr) {
							InvokeExpr invkExpr = (InvokeExpr) rightOp;
							inMethod = invkExpr.getMethod();
						}
					} else if (stmt instanceof InvokeStmt) {
						InvokeExpr invkExpr = ((Stmt) stmt).getInvokeExpr();
						inMethod = invkExpr.getMethod();
					}
					if (inMethod != null && component.toString().equals(inMethod.getDeclaringClass().toString())) {
						this.isFound = true;
						return;
					}
				}
			});
		} else {
			return true;
		}
		return this.isFound;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}
}
