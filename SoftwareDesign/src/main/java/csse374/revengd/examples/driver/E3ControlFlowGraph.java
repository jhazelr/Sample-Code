package csse374.revengd.examples.driver;

import java.nio.file.Paths;
import java.util.Arrays;

import csse374.revengd.soot.MainMethodMatcher;
import csse374.revengd.soot.SceneBuilder;
import soot.Body;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.InvokeExpr;
import soot.tagkit.LineNumberTag;
import soot.tagkit.Tag;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;

public class E3ControlFlowGraph implements Runnable {
	@Override
	public void run() {
		// IMPORTANT: Notice that instead of using the class file directory, we are processing the java code directory here.
		// As a result, the Jimple Intermediate Representation in the output uses the same variable names as your Java code.
		// To learn more about SOOT and Jimple, please review the SOOT Survival Guide: 
		// http://www.brics.dk/SootGuide/sootsurvivorsguide.pdf		
		String dirToLoad = Paths.get(System.getProperty("user.dir"), "src", "main", "java").toString();
		
		Scene scene = SceneBuilder.create()
				.addDirectory(dirToLoad)												// Add the directory from which to load the file or jars
				.setEntryClass("csse374.revengd.examples.fixtures.CalculatorApp")		// Sets the entry point class for the application under analysis
				.addEntryPointMatcher(new MainMethodMatcher("csse374.revengd.examples.fixtures.CalculatorApp"))	// Matches main method of CalculatorApp
				.addExclusions(Arrays.asList("java.*", "javax.*", "sun.*")) 			// Exclude JDK classes from analysis
				.addExclusions(Arrays.asList("soot.*", "polygot.*"))					// Exclude SOOT classes from analysis 
				.addExclusions(Arrays.asList("org.*", "com.*"))						// Exclude other library classes from analysis 
				.build();															// This creates the SOOT's Scene object which has the result of SOOT analysis
		
		// Let's print the statements in the body of the main method of the CalculatorApp, 
		// make sure you review the Java code for CalculatorApp.main() and compare with the output
		SootClass calculatorApp = scene.getSootClass("csse374.revengd.examples.fixtures.CalculatorApp");
		SootMethod mainMethod = calculatorApp.getMethodByName("main");
		
		// Let's retrieve the body of the main method
		Body body = mainMethod.retrieveActiveBody();
		UnitGraph cfg = new ExceptionalUnitGraph(body);						// Creates a Control-Flow Graph from the body
		prettyPrint("Statements in the Calculator.main() method", cfg);		// Prints the statements in the control flow graph
		
		// Suppose we want to find a call to Random.nextBoolean(), how do we do that? Here is how:
		System.out.println("Looking up call to the Random.nextBoolean() method ...");
		cfg.forEach(stmt -> {
			if(stmt instanceof AssignStmt) {
				// If a method returns a value, then we should look for AssignStmt whose right hand side is InvokeExpr
				Value rightOp = ((AssignStmt) stmt).getRightOp();
				if(rightOp instanceof InvokeExpr) {
					InvokeExpr invkExpr = (InvokeExpr)rightOp;
					SootMethod method = invkExpr.getMethod();
					if(method.getName().equals("nextBoolean") &&
							method.getDeclaringClass().getName().equals("java.util.Random")) {
						System.out.println("Found call to Random.nextBoolean() at line number: " + this.getLineNumber(stmt));
					}					
				}
			}
		});
		
		// TODO: Can you find (line number) call to CalculatorApp.performAdd()?
		// HINT: performAdd() does not return any value, so you should be looking for InvokeStmt (not AssignStmt)
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
