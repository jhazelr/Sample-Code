package csse374.revengd.app.renderer;

import csse374.revengd.app.model.Dependency;
import csse374.revengd.app.model.Pattern;
import soot.SootClass;
import soot.SootMethod;

public interface IRenderer {
	public void renderSkinParam(StringBuilder sb);

	public void renderClassHeader(StringBuilder sb, String role);

	public void renderField(StringBuilder sb);

	public String renderMethod(String code, SootMethod mFromDB, SootMethod mFromPat, Pattern pattern);

	public boolean renderDependency(StringBuilder sb, Dependency dpFromDB, Dependency dpFromPat);

	public void renderSpecial(StringBuilder code, SootClass clazz, Pattern pattern);
}
