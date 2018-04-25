package csse374.revengd.app.transformer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import csse374.revengd.app.filter.IFilter;
import csse374.revengd.app.model.Database;
import csse374.revengd.app.model.Dependency;
import csse374.revengd.app.model.Dependency.RELATIONSHIP;
import csse374.revengd.app.model.Pattern;
import soot.SootClass;
import soot.SootField;

public class FCOITransformer extends Transformer {
	private String name;

	public FCOITransformer() {
		super(new ArrayList<IFilter>());
	}

	public FCOITransformer(List<IFilter> filters) {
		super(filters);
	}

	@Override
	public Database transform(Database db) {
		@SuppressWarnings("unchecked")
		List<Object> objs = db.getFilteredObjects(List.class, "classes");
		Collection<SootClass> clazzes = db.castElements(SootClass.class, objs);
		for (SootClass clazz : clazzes) {
			if (clazz.hasSuperclass() && clazzes.contains(clazz.getSuperclass())
					&& !clazz.getSuperclass().isAbstract()) {

				Collection<SootField> fields = clazz.getFields();
				boolean fieldCheck = false;
				for (SootField field : fields) {
					if (field.getType().toString().equals(clazz.getSuperclass().toString())) {
						fieldCheck = true;
					}
				}

				if (!fieldCheck) {
					Pattern pattern = new Pattern(name);
					pattern.putComponent("class", clazz);
					pattern.putComponent("superclass", clazz.getSuperclass());
					Dependency dependency = new Dependency(clazz, clazz.getSuperclass(), RELATIONSHIP.EXTENDS);
					pattern.addRelation(dependency);

					@SuppressWarnings("unchecked")
					List<Object> pats = db.getFilteredObjects(List.class, "patterns");
					Collection<Pattern> patterns = db.castElements(Pattern.class, pats);
					patterns.add(pattern);
					db.putObject("patterns", patterns);
				}
			}
		}
		return db;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

}
