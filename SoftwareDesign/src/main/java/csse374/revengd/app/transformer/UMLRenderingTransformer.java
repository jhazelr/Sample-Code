package csse374.revengd.app.transformer;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import csse374.revengd.app.filter.IFilter;
import csse374.revengd.app.model.Database;
import csse374.revengd.app.model.Dependency;
import csse374.revengd.app.model.Dependency.RELATIONSHIP;
import csse374.revengd.app.model.Pattern;
import csse374.revengd.app.renderer.IRenderer;
import edu.rosehulman.jvm.sigevaluator.FieldEvaluator;
import edu.rosehulman.jvm.sigevaluator.GenericType;
import edu.rosehulman.jvm.sigevaluator.MethodEvaluator;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Type;
import soot.tagkit.Tag;

public class UMLRenderingTransformer extends Transformer {
	private List<IFilter> filters;
	private Map<RELATIONSHIP, String> dependMap;
	private Map<String, IRenderer> rendererMap;
	private Collection<Pattern> patterns;

	public UMLRenderingTransformer(List<IFilter> filters, Map<String, IRenderer> rendererMap)
			throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		super(filters);
		this.filters = filters;
		this.dependMap = new HashMap<>();
		this.dependMap.put(RELATIONSHIP.IMPLEMENTS, "..|>");
		this.dependMap.put(RELATIONSHIP.EXTENDS, "--|>");
		this.dependMap.put(RELATIONSHIP.ASSOCIATES, "-->");
		this.dependMap.put(RELATIONSHIP.DEPENDS, "..>");
		this.dependMap.put(RELATIONSHIP.ASSOCIATES_MANY, "\"1\"-->\"*\"");
		this.dependMap.put(RELATIONSHIP.DEPENDS_MANY, "\"1\"..>\"*\"");
		this.rendererMap = rendererMap;
	}

	@Override
	public Database transform(Database db) {
		render(db);
		return db;
	}

	private void render(Database db) {
		@SuppressWarnings("unchecked")
		List<Object> objs = db.getFilteredObjects(List.class, "patterns");
		this.patterns = db.castElements(Pattern.class, objs);
		StringBuilder umlCode = new StringBuilder();
		umlCode.append("@startuml\n");
		umlCode.append("skinparam linetype ortho\n");
		renderSkinParam(umlCode);
		for (SootClass clazz : getFilteredClasses(db)) {
			renderClassHeader(umlCode, clazz);
			for (SootField field : getFilteredFields(clazz)) {
				renderField(umlCode, field);
			}
			umlCode.append("\n");
			for (SootMethod method : getFilteredMethods(clazz)) {
				renderMethod(umlCode, method);
			}
			renderSpecial(umlCode, clazz);
			umlCode.append("\n}\n\n");

		}

		@SuppressWarnings("unchecked")
		List<Object> deps = db.getFilteredObjects(List.class, "dependencies");
		for (Dependency dependency : db.castElements(Dependency.class, deps)) {
			renderDependency(umlCode, dependency);
		}
		umlCode.append("@enduml");
		saveCode(umlCode.toString());
	}

	private void renderSkinParam(StringBuilder umlCode) {
		Set<IRenderer> visitedRenderers = new HashSet<>();
		for (Pattern pattern : this.patterns) {
			IRenderer renderer = this.rendererMap.get(pattern.getName());
			if (!visitedRenderers.contains(renderer)) {
				renderer.renderSkinParam(umlCode);
				visitedRenderers.add(renderer);
			}
		}
	}

	private void renderClassHeader(StringBuilder umlCode, SootClass clazz) {
		if (clazz.isAbstract()) {
			if (clazz.isInterface()) {
				umlCode.append("interface ");
			} else {
				umlCode.append("abstract class ");
			}
		} else {
			umlCode.append("class ");
		}

		umlCode.append(clazz.getName());
		for (Pattern pattern : this.patterns) {
			for (String role : pattern.getComponents().keySet()) {
				for (SootClass cla : pattern.getComponents().get(role)) {
					if (clazz.toString().equals(cla.toString())) {
						this.rendererMap.get(pattern.getName()).renderClassHeader(umlCode, role);
					}
				}
			}
		}

		umlCode.append(" {\n");
	}

	private void renderField(StringBuilder umlCode, SootField field) {
		if (field.isPublic())
			umlCode.append("+ ");
		if (field.isPrivate())
			umlCode.append("- ");
		if (field.isProtected())
			umlCode.append("# ");
		if (field.isStatic())
			umlCode.append("{static} ");

		Tag signatureTag = field.getTag("SignatureTag");
		if (signatureTag != null) {
			// Use SignatureEvaluator API for parsing the field signature
			try {
				String signature = signatureTag.toString();
				FieldEvaluator fieldEvaluator = new FieldEvaluator(signature);
				GenericType fieldType = fieldEvaluator.getType();
				umlCode.append(fieldType.toString());
			} catch (IllegalStateException e) {
				umlCode.append(field.getType().toString());
			}
		} else {
			// Bytecode signature for this field is unavailable, so let's use
			// soot API
			String typeName = field.getType().toString();
			umlCode.append(typeName);
		}

		umlCode.append(" " + field.getName() + "\n");
	}

	private void renderMethod(StringBuilder restOfUml, SootMethod method) {
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
				umlCode += " " + method.getName();
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
				umlCode += " " + method.getName();
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

		// System.err.println(this.patterns.size());
		for (Pattern pattern : this.patterns) {
			if (pattern.getMethods() != null) {
				for (SootMethod meth : pattern.getMethods()) {
					String result = this.rendererMap.get(pattern.getName()).renderMethod(umlCode, method, meth,
							pattern);
					if (!result.isEmpty()) {
						umlCode = result;
						break;
					}
				}
			}
		}
		restOfUml.append(umlCode);
	}

	private void renderDependency(StringBuilder umlCode, Dependency dependency) {
		boolean hasRendererd = false;
		boolean doneRendering = false;
		for (Pattern pattern : this.patterns) {
			if (pattern.getRelations() != null) {
				for (Dependency depend : pattern.getRelations()) {
					hasRendererd = this.rendererMap.get(pattern.getName()).renderDependency(umlCode, dependency,
							depend);
					if (hasRendererd) {
						doneRendering = true;
					}
				}
			}
		}
		if (!doneRendering) {
			String from = dependency.getFrom().toString();
			umlCode.append(from + " ");
			umlCode.append(this.dependMap.get(dependency.getType()) + " ");
			String to = dependency.getTo().toString();
			umlCode.append(to);
		}

		umlCode.append("\n");
	}

	private void renderSpecial(StringBuilder umlCode, SootClass clazz) {
		for (Pattern pattern : this.patterns) {
			if (pattern.getMethods() != null) {
				this.rendererMap.get(pattern.getName()).renderSpecial(umlCode, clazz, pattern);
			}
		}
	}

	private List<SootClass> getFilteredClasses(Database db) {
		List<SootClass> classesToRender = new ArrayList<>();

		@SuppressWarnings("unchecked")
		List<Object> objs = db.getFilteredObjects(List.class, "classes");
		for (SootClass clazz : db.castElements(SootClass.class, objs)) {
			boolean shouldProcess = true;
			for (IFilter filter : this.filters) {
				if (!filter.shouldProcessClass(clazz)) {
					shouldProcess = false;
					break;
				}
			}
			if (shouldProcess) {
				classesToRender.add(clazz);
			}
		}
		return classesToRender;
	}

	private List<SootField> getFilteredFields(SootClass clazz) {
		List<SootField> fieldsToRender = new ArrayList<>();
		for (SootField field : clazz.getFields()) {
			boolean shouldProcess = true;
			for (IFilter filter : this.filters) {
				if (!filter.shouldProcessField(field)) {
					shouldProcess = false;
					break;
				}
			}
			if (shouldProcess) {
				fieldsToRender.add(field);
			}
		}
		return fieldsToRender;
	}

	private List<SootMethod> getFilteredMethods(SootClass clazz) {
		List<SootMethod> methodsToRender = new ArrayList<>();
		for (SootMethod method : clazz.getMethods()) {
			boolean shouldProcess = true;
			for (IFilter filter : this.filters) {
				if (!filter.shouldProcessMethod(method)) {
					shouldProcess = false;
					break;
				}
			}
			if (shouldProcess) {
				methodsToRender.add(method);
			}
		}
		return methodsToRender;
	}

	private void saveCode(String umlCode) {
		try {
			OutputStream out = new FileOutputStream("./plant_uml_code/code.txt");
			out.write(umlCode.getBytes());
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
