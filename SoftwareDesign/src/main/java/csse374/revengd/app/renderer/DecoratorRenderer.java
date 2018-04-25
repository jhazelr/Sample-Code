package csse374.revengd.app.renderer;

import java.util.List;

import csse374.revengd.app.model.Dependency;
import csse374.revengd.app.model.Dependency.RELATIONSHIP;
import csse374.revengd.app.model.Pattern;
import edu.rosehulman.jvm.sigevaluator.GenericType;
import edu.rosehulman.jvm.sigevaluator.MethodEvaluator;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.tagkit.Tag;

public class DecoratorRenderer implements IRenderer {

	@Override
	public void renderSkinParam(StringBuilder sb) {

	}

	@Override
	public void renderClassHeader(StringBuilder sb, String role) {
		sb.append(" <<" + role + ">>");
		sb.append(" #green");
	}

	@Override
	public void renderField(StringBuilder sb) {

	}

	@Override
	public String renderMethod(String code, SootMethod mFromDB, SootMethod mFromPat, Pattern pattern) {
		return "";
	}

	@Override
	public boolean renderDependency(StringBuilder sb, Dependency dpFromDB, Dependency dpFromPat) {
		if (dpFromDB.getFrom().toString().equals(dpFromPat.getFrom().toString())
				&& dpFromDB.getTo().toString().equals(dpFromPat.getTo().toString())
				&& dpFromDB.getType() == RELATIONSHIP.ASSOCIATES && dpFromPat.getType() == RELATIONSHIP.ASSOCIATES) {
			sb.append(dpFromPat.getFrom().toString());
			sb.append("-->");
			sb.append(dpFromPat.getTo().toString());
			sb.append(":<<decorates>>");
			return true;
		}
		return false;
	}

	@Override
	public void renderSpecial(StringBuilder code, SootClass clazz, Pattern pattern) {
		boolean isPresent = false;
		if (pattern.getComponents().containsKey("Bad decorator")) {
			if (pattern.getComponents().get("Bad decorator").contains(clazz)) {
				for (SootMethod superMethod : pattern.getMethods()) {
					if (!superMethod.getName().contains("<clinit>")) {
						isPresent = false;
						for (SootMethod meth : clazz.getMethods()) {
							String sigFromPat = superMethod.getSignature();
							sigFromPat = sigFromPat.substring(sigFromPat.indexOf(":"));
							String sigFromClass = meth.getSignature();
							sigFromClass = sigFromClass.substring(sigFromClass.indexOf(":"));
							if (sigFromClass.equals(sigFromPat) && !meth.isConstructor()) {
								isPresent = true;
								break;
							}
						}
						if (!isPresent) {
							String newString = renderMissingMethod(superMethod, "<font color=\"#FF0000\">", "</font>");
							code.append(newString);
						}
					}
				}
			}
		}
	}

	public String renderMissingMethod(SootMethod method, String begString, String endString) {
		String umlCode = "";

		if (method.isPublic())
			umlCode += "+ ";
		if (method.isPrivate())
			umlCode += "- ";
		if (method.isProtected())
			umlCode += "# ";
		if (method.isAbstract() && !method.getDeclaringClass().isInterface())
			umlCode += "{abstract} ";
		if (method.isStatic())
			umlCode += "{static} ";

		Tag signatureTag = method.getTag("SignatureTag");
		if (signatureTag != null) {
			String signature = signatureTag.toString();
			MethodEvaluator evaluator = new MethodEvaluator(signature);

			if (method.isConstructor()) {
				String className = method.getDeclaringClass().getName();
				umlCode += className.substring(className.lastIndexOf(".") + 1);
			} else {
				try {
					umlCode += evaluator.getReturnType();
				} catch (IllegalStateException e) {
					umlCode += method.getReturnType();
				}
				umlCode += begString; // Add color here
				umlCode += " " + method.getName();
				umlCode += endString; // Add color here
			}

			umlCode += "(";
			try {
				List<GenericType> paramTypes = evaluator.getParameterTypes();
				for (int i = 0; i < paramTypes.size(); ++i) {
					umlCode += paramTypes.get(i);
					if (i != paramTypes.size() - 1) {
						umlCode += ", ";
					}
				}
			} catch (IllegalStateException e) {
				List<Type> argTypes = method.getParameterTypes();
				for (int i = 0; i < argTypes.size(); ++i) {
					umlCode += argTypes.get(i);
					if (i != argTypes.size() - 1) {
						umlCode += ", ";
					}
				}
			}
		} else {
			if (method.isConstructor()) {
				String className = method.getDeclaringClass().getName();
				umlCode += className.substring(className.lastIndexOf(".") + 1);
			} else {
				umlCode += method.getReturnType();
				umlCode += begString;
				umlCode += " " + method.getName();
				umlCode += endString;
			}

			umlCode += "(";
			List<Type> argTypes = method.getParameterTypes();
			for (int i = 0; i < argTypes.size(); ++i) {
				umlCode += argTypes.get(i);
				if (i != argTypes.size() - 1) {
					umlCode += ", ";
				}
			}
		}
		umlCode += ")\n";
		return umlCode;
	}
}
