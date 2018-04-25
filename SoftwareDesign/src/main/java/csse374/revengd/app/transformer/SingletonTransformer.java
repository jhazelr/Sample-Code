package csse374.revengd.app.transformer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import csse374.revengd.app.filter.IFilter;
import csse374.revengd.app.model.Database;
import csse374.revengd.app.model.Pattern;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;

public class SingletonTransformer extends Transformer {
	private boolean constructorCheck;
	private boolean methodCheck;
	private boolean fieldCheck;
	private String name;

	public SingletonTransformer() {
		super(new ArrayList<IFilter>());
	}

	public SingletonTransformer(List<IFilter> filters) {
		super(filters);
		this.constructorCheck = false;
		this.methodCheck = false;
		this.fieldCheck = false;
	}

	@Override
	public Database transform(Database db) {
		@SuppressWarnings("unchecked")
		List<Object> objs = db.getFilteredObjects(List.class, "classes");
		Collection<SootClass> dbClasses = db.castElements(SootClass.class, objs);
		for (SootClass clazz : dbClasses) {
			List<SootMethod> clazzMethods = clazz.getMethods();
			for (SootMethod method : clazzMethods) {
				// Check for private constructor
				if (checkConstructor(method)) {
					this.constructorCheck = true;
				}
				// Check for method with return type of itself
				if (checkMethod(method, clazz)) {
					this.methodCheck = true;
				}
			}
			// Check for private static field of itself
			for (SootField field : clazz.getFields()) {
				if (checkFields(field, clazz)) {
					this.fieldCheck = true;
				}
			}
			if (this.constructorCheck && this.fieldCheck && this.methodCheck) {
				// Found Singelton
				Pattern newPattern = new Pattern(name);
				newPattern.putComponent("Singleton", clazz);

				@SuppressWarnings("unchecked")
				List<Object> pats = db.getFilteredObjects(List.class, "patterns");
				Collection<Pattern> patterns = db.castElements(Pattern.class, pats);
				patterns.add(newPattern);
				db.putObject("patterns", patterns);
			}
			resetChecks();
		}
		return db;
	}

	private void resetChecks() {
		this.constructorCheck = false;
		this.fieldCheck = false;
		this.methodCheck = false;
	}

	private boolean checkConstructor(SootMethod method) {
		return (method.isConstructor() && method.isPrivate());
	}

	private boolean checkMethod(SootMethod method, SootClass clazz) {
		return (method.getReturnType().equals(clazz.getType()) && method.isPublic() && method.isStatic());
	}

	private boolean checkFields(SootField field, SootClass clazz) {
		return field.getType().equals(clazz.getType()) && field.isPrivate() && field.isStatic();
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}
}
