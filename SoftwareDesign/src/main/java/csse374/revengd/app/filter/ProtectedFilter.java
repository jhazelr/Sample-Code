package csse374.revengd.app.filter;

import soot.SootClass;
import soot.SootField;
import soot.SootMethod;

public class ProtectedFilter implements IFilter {

	@Override
	public boolean shouldProcessClass(SootClass clazz) {
		return !clazz.isPrivate();
	}

	@Override
	public boolean shouldProcessField(SootField field) {
		return !field.isPrivate();
	}

	@Override
	public boolean shouldProcessMethod(SootMethod method) {
		return !method.isPrivate();
	}

}
