package csse374.revengd.app.renderer;

import csse374.revengd.app.model.Dependency;
import csse374.revengd.app.model.Pattern;
import soot.SootClass;
import soot.SootMethod;

public class SingletonRenderer implements IRenderer {

	@Override
	public void renderSkinParam(StringBuilder sb) {
		sb.append("skinparam class {\nBorderColor<<Singleton>> blue\n}\n");
	}

	@Override
	public void renderClassHeader(StringBuilder sb, String role) {
		sb.append(" <<" + role + ">>");
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
		return false;
	}

	@Override
	public void renderSpecial(StringBuilder code, SootClass clazz, Pattern pattern) {

	}
}
