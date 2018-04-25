package csse374.revengd.app.filter;

import soot.SootClass;
import soot.SootField;
import soot.SootMethod;

public class PublicFilter implements IFilter {

	@Override
	public boolean shouldProcessClass(SootClass clazz) {
		return !(clazz.isPrivate() || clazz.isProtected());
	}

	@Override
	public boolean shouldProcessField(SootField field) {
		return !(field.isPrivate() || field.isProtected());
	}

	@Override
	public boolean shouldProcessMethod(SootMethod method) {
		return !(method.isPrivate() || method.isProtected());
	}

}
