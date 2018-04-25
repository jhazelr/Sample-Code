package csse374.revengd.app.transformer;

import java.util.Collection;
import java.util.List;

import csse374.revengd.app.filter.IFilter;
import csse374.revengd.app.model.Database;
import csse374.revengd.app.model.Dependency;
import csse374.revengd.app.model.Dependency.RELATIONSHIP;
import soot.SootClass;

public class SuperDependencyTransformer extends Transformer {

	public SuperDependencyTransformer(List<IFilter> filters) {
		super(filters);
	}

	@Override
	public Database transform(Database db) {
		DependencyUpdater updater = new DependencyUpdater(db);

		@SuppressWarnings("unchecked")
		List<Object> objs = db.getFilteredObjects(List.class, "classes");
		Collection<SootClass> dbClasses = db.castElements(SootClass.class, objs);

		@SuppressWarnings("unchecked")
		List<Object> deps = db.getFilteredObjects(List.class, "dependencies");
		Collection<Dependency> dependencies = db.castElements(Dependency.class, deps);
		for (SootClass clazz : dbClasses) {
			for (SootClass interfaze : clazz.getInterfaces()) {
				if (dbClasses.contains(interfaze)) {
					updater.addDependency(dependencies, clazz, interfaze, RELATIONSHIP.IMPLEMENTS);
				}
			}
			if (clazz.hasSuperclass() && dbClasses.contains(clazz.getSuperclass())) {
				updater.addDependency(dependencies, clazz, clazz.getSuperclass(), RELATIONSHIP.EXTENDS);
			}
		}
		return db;
	}
}