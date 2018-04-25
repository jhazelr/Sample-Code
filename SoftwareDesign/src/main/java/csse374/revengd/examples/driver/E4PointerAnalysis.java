package csse374.revengd.examples.driver;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import csse374.revengd.soot.MainMethodMatcher;
import csse374.revengd.soot.SceneBuilder;
import soot.Body;
import soot.Hierarchy;
import soot.MethodOrMethodContext;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.InvokeExpr;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.tagkit.LineNumberTag;
import soot.tagkit.Tag;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;

public class E4PointerAnalysis implements Runnable {
	@Override
	public void run() {
		// See http://www.brics.dk/SootGuide/sootsurvivorsguide.pdf to learn more		
		String dirToLoad = Paths.get(System.getProperty("user.dir"), "src", "main", "java").toString();
		System.out.println(dirToLoad);
		Scene scene = SceneBuilder.create()
				.addDirectory(dirToLoad)												// Add the directory from which to load the file or jars
				.setEntryClass("csse374.revengd.examples.fixtures.CalculatorApp")		// Sets the entry point class for the application under analysis
				.addEntryPointMatcher(new MainMethodMatcher("csse374.revengd.examples.fixtures.CalculatorApp"))	// Matches main method of CalculatorApp
				.addExclusions(Arrays.asList("java.*", "javax.*", "sun.*")) 			// Exclude JDK classes from analysis
				.addExclusions(Arrays.asList("soot.*", "polygot.*"))					// Exclude SOOT classes from analysis 
				.addExclusions(Arrays.asList("org.*", "com.*"))						// Exclude other library classes from analysis 
				.build();															// This creates the SOOT's Scene object which has the result of SOOT analysis

		
		// To start, review the CalculatorApp.performMultiply() method one more time. 
		// When this.calculator.multiply() gets called, which Class does the calculator field resolve to?
		// Is it CalculatorA, or CalculatorB, or CalculatorC? These are all subtypes of ICalculator, so
		// all of them are indeed possible. This is a naive analysis and is also known as Class Hierarchy
		// Analysis (CHA). A more precise analysis would be the one that considers the calling context
		// where the call graph is constructed based on the object initialized in the program. Such analysis
		// is known as Context-Sensitive analysis. SOOT provides support for both.
		
		// Let's  get hold of the CalculatorApp.performMultiply() method first
		SootClass calculatorApp = scene.getSootClass("csse374.revengd.examples.fixtures.CalculatorApp");
		SootMethod performMulMethod = calculatorApp.getMethodByName("performMultiply");
		
		// Let's retrieve the body of the performMultiply() method
		Body body = performMulMethod.retrieveActiveBody();
		UnitGraph cfg = new ExceptionalUnitGraph(body);
		prettyPrint("Statements in the CalculatorApp.performMultiply() method", cfg);
		
		// Let's find calls to the ICalculator.multiply() method
		System.out.println("Looking up calls to the ICalculator.multiply() method ...");
		cfg.forEach(stmt -> {
			if(stmt instanceof AssignStmt) {
				// If a method returns a value, then we should look for AssignStmt whose right hand side is InvokeExpr
				Value rightOp = ((AssignStmt) stmt).getRightOp();
				if(rightOp instanceof InvokeExpr) {
					InvokeExpr invkExpr = (InvokeExpr)rightOp;
					SootMethod method = invkExpr.getMethod();
					if(method.getName().equals("multiply")) {
						System.out.println("Found call to ICalculator.multiply() at line number: " + this.getLineNumber(stmt));
//						this.performCHAPointerAnalysis(scene, method);
						this.performContextSensitivePointerAnalysis(scene, stmt, method);						
					}
				}
			}
		});
		
		// TODO: Can you find which method(s) call CalculatorA.add()?
		// You will need to show the name of the declaring class as well.
	}
	
	// This is context-insensitive analysis
	void performCHAPointerAnalysis(Scene scene, SootMethod method) {
		System.out.println("Performing CHA analysis for " + method.getName() + "() ...");
		Hierarchy hierarchy = scene.getActiveHierarchy();
		Collection<SootMethod> possibleMethods = hierarchy.resolveAbstractDispatch(method.getDeclaringClass(), method);
		this.prettyPrintMethods("CHA resolution for", method, possibleMethods);
	}

	// This is the context-sensitive analysis
	void performContextSensitivePointerAnalysis(Scene scene, Unit stmt, SootMethod method) {
		System.out.println("Performing Context-Sensitive pointer analysis for " + method.getName() + "() ...");
		CallGraph callGraph = scene.getCallGraph();
		
		Collection<SootMethod> targetMethods = new ArrayList<>();
		callGraph.edgesOutOf(stmt).forEachRemaining(edge -> {
			MethodOrMethodContext methodOrCntxt = edge.getTgt();
			SootMethod targetMethod = methodOrCntxt.method();
			if(targetMethod != null) {
				targetMethods.add(targetMethod);
			}
		});
		this.prettyPrintMethods("Context-sensitive anaylysis resolution for", method, targetMethods);
	}

	void prettyPrintMethods(String title, SootMethod originalMethod, Iterable<SootMethod> methods) {
		System.out.println("-------------------------------------------------");
		System.out.format("%s [%s.%s(...)]:%n", title, originalMethod.getDeclaringClass().getName(), originalMethod.getName());
		System.out.println("-------------------------------------------------");
		methods.forEach(method -> {
			System.out.format("[%d] %s.%s(...)%n", method.getJavaSourceStartLineNumber(), 
					method.getDeclaringClass().getName(), 
					method.getName());
		});
		System.out.println("-------------------------------------------------");
	}

	void prettyPrint(String title, Iterable<Unit> stmts) {
		System.out.println("-------------------------------------------------");
		System.out.println(title);
		System.out.println("-------------------------------------------------");
		stmts.forEach(stmt -> {
			System.out.format("[%d] [%s] %s%n", this.getLineNumber(stmt), stmt.getClass().getName(), stmt.toString());
		});
		System.out.println("-------------------------------------------------");
	}

	int getLineNumber(Unit stmt) {
		for(Tag tag: stmt.getTags()) {
			if(tag instanceof LineNumberTag)
				return ((LineNumberTag) tag).getLineNumber();
		}
		return -1;
	}
}
