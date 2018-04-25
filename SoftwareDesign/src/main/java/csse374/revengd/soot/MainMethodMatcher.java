package csse374.revengd.soot;

import soot.ArrayType;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;

/**
 * Matches the main method of the supplied Java class.
 */
public class MainMethodMatcher implements IEntryPointMatcher {
	String className;

	public MainMethodMatcher(String className) {
		this.className = className;
	}

	@Override
	public boolean match(SootClass clazz) {
		return clazz.getName().equals(this.className);
	}

	@Override
	public boolean match(SootMethod method) {
		if(!method.getName().equals("main"))
			return false;

		if(method.getParameterCount() != 1)
			return false;
		
		Type paramType = method.getParameterType(0);
		if(!(paramType instanceof ArrayType))
			return false;
		
		ArrayType arrayType = (ArrayType)paramType;
		return arrayType.baseType.toString().equals("java.lang.String");
	}
}
