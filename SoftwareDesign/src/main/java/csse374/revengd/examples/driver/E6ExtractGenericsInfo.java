package csse374.revengd.examples.driver;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.core.tools.Generate;

import csse374.revengd.soot.MainMethodMatcher;
import csse374.revengd.soot.SceneBuilder;
import edu.rosehulman.jvm.sigevaluator.FieldEvaluator;
import edu.rosehulman.jvm.sigevaluator.GenericType;
import edu.rosehulman.jvm.sigevaluator.MethodEvaluator;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Type;
import soot.tagkit.Tag;

public class E6ExtractGenericsInfo implements Runnable {
	// We will use this class to abstract all types including generic types

	@Override
	public void run() {
		String dirToLoad = Paths.get(System.getProperty("user.dir"), "build", "classes", "java", "main").toString();

		Scene scene = SceneBuilder.create().addDirectory(dirToLoad)
				// Add the directory from which to load the file or jars
				.setEntryClass("csse374.revengd.examples.fixtures.CalculatorApp")
				// Sets the entry point class for the application under analysis
				.addEntryPointMatcher(new MainMethodMatcher("csse374.revengd.examples.fixtures.CalculatorApp"))
				// Matches main method of CalculatorApp
				.addExclusions(Arrays.asList("java.*", "javax.*", "sun.*"))
				// Exclude JDK classes from analysis
				.addExclusions(Arrays.asList("soot.*", "polygot.*"))
				// Exclude SOOT classes from analysis
				.addExclusions(Arrays.asList("org.*", "com.*"))
				// Exclude other library classes from analysis
				.build();
		// This creates the SOOT's Scene object which has the result of SOOT
		// analysis

		// This class has field of type List<String>. See how to extract the
		// Generics info here
		SootClass unrelatedClass = scene.getSootClass("csse374.revengd.examples.fixtures.UnrelatedClass");
		print(unrelatedClass);
	}

	void print(SootClass clazz) {
		System.out.println("----------------- Class -----------------------");
		System.out.println(clazz);

		System.out.println("---------------- Fields -----------------------");
		clazz.getFields().forEach(field -> print(field));

		System.out.println("---------------- Methods ----------------------");
		clazz.getMethods().forEach(method -> print(method));

		System.out.println("-----------------------------------------------");
	}

	void print(SootField field) {
		StringBuilder builder = new StringBuilder();
		if (field.isStatic()) {
			builder.append("static ");
		}
		if (field.isPrivate()) {
			builder.append("private ");
		}
		// Similar checkout other methods available in the SootField API
		builder.append(field.getName());
		builder.append(": ");

		Tag signatureTag = field.getTag("SignatureTag");
		if (signatureTag != null) {
			// Use SignatureEvaluator API for parsing the field signature
			String signature = signatureTag.toString();
			FieldEvaluator fieldEvaluator = new FieldEvaluator(signature);
			GenericType fieldType = fieldEvaluator.getType();
			builder.append(fieldType.toString());

			// TODO: Try playing with the following methods
			// fieldType.getContainerType();
			// fieldType.getElementTypes();
			// fieldType.getAllContainerTypes();
			// fieldType.getAllElementTypes();
			// fieldType.isArray();
			// fieldType.getDimension();
		} else {
			// Bytecode signature for this field is unavailable, so let's use
			// soot API
			builder.append(field.getType().toString());
		}

		System.out.println(builder);
	}

	void print(SootMethod method) {
		StringBuilder builder = new StringBuilder();
		if (method.isStatic()) {
			builder.append("static ");
		}
		if (method.isPublic()) {
			builder.append("public ");
		}
		// Similar checkout other methods available in the SootMethod API
		builder.append(method.getName());

		Tag signatureTag = method.getTag("SignatureTag");
		if (signatureTag != null) {
			String signature = signatureTag.toString();
			MethodEvaluator evaluator = new MethodEvaluator(signature);

			List<GenericType> paramTypes = evaluator.getParameterTypes();
			builder.append("(");
			for (int i = 0; i < paramTypes.size(); ++i) {
				builder.append(paramTypes.get(i));
				if (i != paramTypes.size() - 1) {
					builder.append(", ");
				}
			}
			builder.append("): ");

			GenericType returnType = evaluator.getReturnType();
			builder.append(returnType);
		} else {
			List<Type> argTypes = method.getParameterTypes();
			builder.append("(");
			for (int i = 0; i < argTypes.size(); ++i) {
				builder.append(argTypes.get(i));
				if (i != argTypes.size() - 1) {
					builder.append(", ");
				}
			}
			builder.append("): ");

			Type returnType = method.getReturnType();
			builder.append(returnType);
		}

		System.out.println(builder);
	}
}
