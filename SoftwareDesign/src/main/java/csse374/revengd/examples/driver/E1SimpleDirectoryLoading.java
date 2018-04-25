package csse374.revengd.examples.driver;

import java.nio.file.Paths;
import java.util.Arrays;

import csse374.revengd.soot.MainMethodMatcher;
import csse374.revengd.soot.SceneBuilder;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;

public class E1SimpleDirectoryLoading implements Runnable {
	@Override
	public void run() {
		String dirToLoad = Paths.get(System.getProperty("user.dir"), "build", "classes", "main").toString();
		
		Scene scene = SceneBuilder.create()
				.addDirectory(dirToLoad)												// Add the directory from which to load the file or jars
				.setEntryClass("csse374.revengd.examples.fixtures.CalculatorApp")		// Sets the entry point class for the application under analysis
				.addEntryPointMatcher(new MainMethodMatcher("csse374.revengd.examples.fixtures.CalculatorApp"))	// Matches main method of CalculatorApp
				.addExclusions(Arrays.asList("java.*", "javax.*", "sun.*")) 			// Exclude JDK classes from analysis
				.addExclusions(Arrays.asList("soot.*", "polygot.*"))					// Exclude SOOT classes from analysis 
				.addExclusions(Arrays.asList("org.*", "com.*"))						// Exclude other library classes from analysis 
				.build();															// This creates the SOOT's Scene object which has the result of SOOT analysis
		
		// TODO: 1. Can you make it load only the classes in the csse374.revengd.examples.fixtures package?
		
		// Let's list all of the classes we have loaded from the supplied directory. 
		// These classes are called application classes in SOOT
		System.out.println("==============================================================");
		System.out.println("Application classes loaded by SOOT:");
		scene.getApplicationClasses().forEach(clazz -> {
			System.out.println(clazz.getName() );
		});

		// We can also lookup a class using the Scene API, see below
		SootClass appClass = scene.getSootClass("csse374.revengd.examples.fixtures.CalculatorApp");
		print(appClass);
		
		// TODO: 2. Can you print methods of CalculatorB? 
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
		if(field.isStatic()) {
			builder.append("static ");			
		}
		if(field.isPrivate()) {
			builder.append("private ");			
		}
		// Similar checkout other methods available in the SootField API
		builder.append(field.getName());
		builder.append(": ");
		builder.append(field.getType());
		
		System.out.println(builder);
	}
	
	void print(SootMethod method) {
		StringBuilder builder = new StringBuilder();
		if(method.isStatic()) {
			builder.append("static ");			
		}
		if(method.isPublic()) {
			builder.append("public ");			
		}
		// Similar checkout other methods available in the SootMethod API
		builder.append(method.getName());
		builder.append("(...): ");
		builder.append(method.getReturnType());
		System.out.println(builder);		
	}
}
