package csse374.revengd.app.transformer;

import java.util.Collection;
import java.util.List;

import csse374.revengd.app.model.Database;
import csse374.revengd.app.model.Dependency;
import csse374.revengd.app.model.Dependency.RELATIONSHIP;
import soot.SootClass;

public class DependencyUpdater {

	private Database db;

	public DependencyUpdater(Database db) {
		this.db = db;
	}

	public void findDependency(SootClass clazz, String type, boolean associates, boolean oneToMany) {
		// check if the type is an array
		if (type.endsWith("[]")) {
			type = type.substring(0, type.length() - 2);
			oneToMany = true;
		}
		// ignore if clazz depends on itself
		if (clazz.toString().equals(type) && !associates) {
			return;
		}
		@SuppressWarnings("unchecked")
		List<Object> objs = db.getFilteredObjects(List.class, "classes");
		Collection<SootClass> clazzes = this.db.castElements(SootClass.class, objs);

		SootClass assocClass = this.getClass(clazzes, type);
		if (assocClass != null) {
			this.updateDependency(clazz, assocClass, associates, oneToMany);
		}
	}

	private void updateDependency(SootClass from, SootClass to, boolean associates, boolean oneToMany) {
		boolean needsAddDepend = true;
		@SuppressWarnings("unchecked")
		List<Object> objs = db.getFilteredObjects(List.class, "dependencies");
		Collection<Dependency> dependencies = this.db.castElements(Dependency.class, objs);
		for (Dependency dependency : dependencies) {
			if (dependency.getFrom().toString().equals(from.toString())
					&& dependency.getTo().toString().equals(to.toString())) {
				if (dependency.getType() == RELATIONSHIP.ASSOCIATES && associates && oneToMany) {
					dependency.setType(RELATIONSHIP.ASSOCIATES_MANY);
				} else if (dependency.getType() == RELATIONSHIP.DEPENDS && !associates && oneToMany) {
					dependency.setType(RELATIONSHIP.DEPENDS_MANY);
				} else if (dependency.getType() == RELATIONSHIP.ASSOCIATES
						|| dependency.getType() == RELATIONSHIP.ASSOCIATES_MANY) {
					needsAddDepend = false;
					break;
				} else if (dependency.getType() == RELATIONSHIP.DEPENDS && !associates) {
					needsAddDepend = false;
					break;
				}
			}
		}
		if (needsAddDepend) {
			if (associates) {
				if (oneToMany) {
					addDependency(dependencies, from, to, RELATIONSHIP.ASSOCIATES_MANY);
				} else {
					addDependency(dependencies, from, to, RELATIONSHIP.ASSOCIATES);
				}
			} else {
				if (oneToMany) {
					addDependency(dependencies, from, to, RELATIONSHIP.DEPENDS_MANY);
				} else {
					addDependency(dependencies, from, to, RELATIONSHIP.DEPENDS);
				}
			}
		}
	}

	private SootClass getClass(Collection<SootClass> clazzes, String type) {
		for (SootClass clazz : clazzes) {
			if (clazz.toString().equals(type)) {
				return clazz;
			}
		}
		return null;
	}

	public void addDependency(Collection<Dependency> dependencies, SootClass from, SootClass to,
			RELATIONSHIP relation) {
		dependencies.add(new Dependency(from, to, relation));
		this.db.putObject("dependencies", dependencies);
	}
}
