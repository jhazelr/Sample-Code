package csse374.revengd.app.filter;

import soot.SootClass;
import soot.SootField;
import soot.SootMethod;

public class PrivateFilter implements IFilter {

	@Override
	public boolean shouldProcessClass(SootClass clazz) {
		return true;
	}

	@Override
	public boolean shouldProcessField(SootField field) {
		return true;
	}

	@Override
	public boolean shouldProcessMethod(SootMethod method) {
		return true;
	}

}
