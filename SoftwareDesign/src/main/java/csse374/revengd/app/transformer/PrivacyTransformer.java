package csse374.revengd.app.transformer;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import csse374.revengd.app.filter.IFilter;
import csse374.revengd.app.model.Database;
import soot.SootClass;

public class PrivacyTransformer extends Transformer {
	List<IFilter> filters;

	public PrivacyTransformer(List<IFilter> filters) {
		super(filters);
		this.filters = filters;
	}

	/**
	 * For each filter, check if the classes in the Database object need to be
	 * processed. If they are not contained in the privacy setting specified by
	 * the user, they are removed from the Database object.
	 * 
	 * @param db
	 *            Database object that contains the SootClasses to analyze
	 * @return the altered Database object
	 */
	@Override
	public Database transform(Database db) {
		Set<SootClass> classesToRemoved = new HashSet<>();

		@SuppressWarnings("unchecked")
		List<Object> objs = db.getFilteredObjects(List.class, "classes");
		Collection<SootClass> dbClasses = db.castElements(SootClass.class, objs);
		for (IFilter filter : this.filters) {
			for (SootClass clazz : dbClasses) {
				if (!filter.shouldProcessClass(clazz)) {
					classesToRemoved.add(clazz);
				}
			}
		}
		dbClasses.removeAll(classesToRemoved);
		db.putObject("classes", dbClasses);
		return db;
	}
}
