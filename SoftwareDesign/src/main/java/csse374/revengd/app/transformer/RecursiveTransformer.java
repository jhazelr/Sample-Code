package csse374.revengd.app.transformer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import csse374.revengd.app.filter.IFilter;
import csse374.revengd.app.model.Database;
import soot.SootClass;

public class RecursiveTransformer extends Transformer {

	public RecursiveTransformer(List<IFilter> filters) {
		super(filters);
	}

	@Override
	public Database transform(Database db) {
		List<SootClass> classesToAdd = new ArrayList<>();

		@SuppressWarnings("unchecked")
		List<Object> objs = db.getFilteredObjects(List.class, "classes");
		Collection<SootClass> dbClasses = db.castElements(SootClass.class, objs);
		for (SootClass clazz : dbClasses) {
			Collection<SootClass> allSuperTypes = new HashSet<>();
			this.computeAllSuperTypes(clazz, allSuperTypes, db);
			for (SootClass supertype : allSuperTypes) {
				if (!dbClasses.contains(supertype) && !classesToAdd.contains(supertype)) {
					classesToAdd.add(supertype);
				}
			}
		}
		dbClasses.addAll(classesToAdd);
		db.putObject("classes", dbClasses);
		return db;
	}

	// credit: csse374.revengd.examples.driver.E2TypeHierarchy.java
	private void computeAllSuperTypes(final SootClass clazz, final Collection<SootClass> allSuperTypes, Database db) {
		if (clazz.getName().equals("java.lang.Object") || isBlackListed(db, clazz))
			return;

		Collection<SootClass> directSuperTypes = new ArrayList<SootClass>();

		SootClass superClazz = clazz.getSuperclass();
		if (superClazz != null)
			directSuperTypes.add(superClazz);

		if (clazz.getInterfaceCount() > 0)
			directSuperTypes.addAll(clazz.getInterfaces());

		directSuperTypes.forEach(aType -> {
			if (!allSuperTypes.contains(aType)) {
				allSuperTypes.add(aType);
				this.computeAllSuperTypes(aType, allSuperTypes, db);
			}
		});
	}

	private boolean isBlackListed(Database db, SootClass clazz) {
		@SuppressWarnings("unchecked")
		List<Object> objs = db.getFilteredObjects(List.class, "blist");
		Collection<String> blackList = db.castElements(String.class, objs);
		for (String prefix : blackList) {
			if (clazz.getName().startsWith(prefix)) {
				return true;
			}
		}
		return false;
	}
}
