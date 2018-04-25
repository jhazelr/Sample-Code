package csse374.revengd.app.renderer;

import csse374.revengd.app.model.Dependency;
import csse374.revengd.app.model.Dependency.RELATIONSHIP;
import csse374.revengd.app.model.Pattern;
import soot.SootClass;
import soot.SootMethod;

public class AdapterRenderer implements IRenderer {

	@Override
	public void renderSkinParam(StringBuilder sb) {

	}

	@Override
	public void renderClassHeader(StringBuilder sb, String role) {
		sb.append(" <<" + role + ">>");
		sb.append(" #red");
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
				&& dpFromPat.getType() == RELATIONSHIP.ASSOCIATES && dpFromDB.getType() == RELATIONSHIP.ASSOCIATES) {
			sb.append(dpFromPat.getFrom().toString());
			sb.append("-->");
			sb.append(dpFromPat.getTo().toString());
			sb.append(":<<adapts>>");
			return true;
		}
		return false;
	}

	@Override
	public void renderSpecial(StringBuilder code, SootClass clazz, Pattern pattern) {
	}

}
