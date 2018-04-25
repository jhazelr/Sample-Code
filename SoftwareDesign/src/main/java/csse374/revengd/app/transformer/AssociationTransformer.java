package csse374.revengd.app.transformer;

import java.util.List;

import csse374.revengd.app.filter.IFilter;
import csse374.revengd.app.model.Database;
import edu.rosehulman.jvm.sigevaluator.FieldEvaluator;
import edu.rosehulman.jvm.sigevaluator.GenericType;
import soot.SootClass;
import soot.SootField;
import soot.tagkit.Tag;

public class AssociationTransformer extends Transformer {
	List<IFilter> filters;

	public AssociationTransformer(List<IFilter> filters) {
		super(filters);
		this.filters = filters;

	}

	@SuppressWarnings("unchecked")
	@Override
	public Database transform(Database db) {
		DependencyUpdater updater = new DependencyUpdater(db);
		List<Object> objs = db.getFilteredObjects(List.class, "classes");
		for (SootClass clazz : db.castElements(SootClass.class, objs)) {
			for (SootField field : clazz.getFields()) {
				if (shouldProcess(field)) {
					Tag signatureTag = field.getTag("SignatureTag");
					// use the library to analyze the type of the field
					if (signatureTag != null) {
						String signature = signatureTag.toString();
						FieldEvaluator fieldEvaluator = new FieldEvaluator(signature);
						try {
							GenericType fieldType = fieldEvaluator.getType();
							if (fieldType.getContainerType() != null) {
								for (String element : fieldType.getAllElementTypes()) {
									updater.findDependency(clazz, element, true, true);
								}
							} else {
								updater.findDependency(clazz, fieldType.toString(), true, false);
							}
						} catch (IllegalStateException e) {
							updater.findDependency(clazz, field.getType().toString(), true, false);
						}
					} else {
						// use soot's getType() method if the library isn't
						// working
						updater.findDependency(clazz, field.getType().toString(), true, false);
					}
				}
			}
		}
		return db;
	}

	private boolean shouldProcess(SootField field) {
		for (IFilter filter : this.filters) {
			if (!filter.shouldProcessField(field)) {
				return false;
			}
		}
		return true;
	}
}
