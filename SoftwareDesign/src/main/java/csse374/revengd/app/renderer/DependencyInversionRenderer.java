package csse374.revengd.app.renderer;

import csse374.revengd.app.model.Dependency;
import csse374.revengd.app.model.Dependency.RELATIONSHIP;
import csse374.revengd.app.model.Pattern;
import soot.SootClass;
import soot.SootMethod;

public class DependencyInversionRenderer implements IRenderer {

	@Override
	public void renderSkinParam(StringBuilder sb) {

	}

	@Override
	public void renderClassHeader(StringBuilder sb, String role) {
		if (role.equals("class")) {
			sb.append(" #purple");
		}
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
				&& dpFromDB.getTo().toString().equals(dpFromPat.getTo().toString())) {
			if (dpFromDB.getType() == RELATIONSHIP.EXTENDS || dpFromDB.getType() == RELATIONSHIP.IMPLEMENTS) {
				return false;
			}
			sb.append(dpFromPat.getFrom().toString());
			if (dpFromPat.getType() == RELATIONSHIP.ASSOCIATES) {
				sb.append("-[#purple]->");
			} else if (dpFromPat.getType() == RELATIONSHIP.ASSOCIATES_MANY) {
				sb.append("\"1\"-[#purple]->\"*\"");
			} else if (dpFromPat.getType() == RELATIONSHIP.DEPENDS) {
				sb.append(".[#purple].>");
			} else {
				sb.append("\"1\".[#purple].>\"*\"");
			}
			sb.append(dpFromPat.getTo().toString());
			return true;
		}
		return false;
	}

	@Override
	public void renderSpecial(StringBuilder code, SootClass clazz, Pattern pattern) {
	}
}
