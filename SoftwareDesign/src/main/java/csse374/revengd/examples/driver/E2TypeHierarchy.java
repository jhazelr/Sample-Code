package csse374.revengd.examples.driver;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;

import csse374.revengd.soot.MainMethodMatcher;
import csse374.revengd.soot.SceneBuilder;
import soot.Hierarchy;
import soot.Scene;
import soot.SootClass;

public class E2TypeHierarchy implements Runnable {
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
		
		System.out.println("==============================================================");
		System.out.println("Application classes loaded by SOOT:");
		scene.getApplicationClasses().forEach(clazz -> {
			System.out.println(clazz.getName() );
		});

		// Let's ask the Scene API for TypeHierarchy
		Hierarchy typeHierarchy = scene.getActiveHierarchy();

		// Now let's query the typeHierarchy for all the implementers of the ICalculator interface
		SootClass iCalculator = scene.getSootClass("csse374.revengd.examples.fixtures.ICalculator");
		Collection<SootClass> implementors = typeHierarchy.getImplementersOf(iCalculator);
		this.prettyPrint("Implementors of ICalculator", implementors);

		// TODO: Can you list all (direct and indirect) supertypes, including 
		// classes and interfaces, of CalculatorC?
		// HINT: This is not as straightforward as it sounds using the Hierarchy API.
		// See if there are methods in the SootClass that can help.
	}

	<T> void prettyPrint(String title, Iterable<T> iterable) {
		System.out.println("-------------------------------------------------");
		System.out.println(title);
		System.out.println("-------------------------------------------------");
		iterable.forEach(item -> System.out.println(item));
		System.out.println("-------------------------------------------------");
	}
}
