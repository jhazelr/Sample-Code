package csse374.revengd.app.filter;

import soot.SootClass;
import soot.SootField;
import soot.SootMethod;

public interface IFilter {
	public boolean shouldProcessClass(SootClass clazz);

	public boolean shouldProcessField(SootField field);

	public boolean shouldProcessMethod(SootMethod method);
}
