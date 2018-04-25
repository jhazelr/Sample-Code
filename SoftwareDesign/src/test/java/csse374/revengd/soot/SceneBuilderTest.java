package csse374.revengd.soot;

import static org.junit.Assert.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.*;

import csse374.revengd.soot.SceneBuilder;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;

public class SceneBuilderTest {
	SceneBuilder builder;
	List<String> allFixtureClasses;
	List<String> allRechableClassesFromEntry;
	List<String> allUnRechableClassesFromEntry;

	@Before
	public void setUp() throws Exception {
		this.builder = SceneBuilder.create();
		
		this.allRechableClassesFromEntry = Arrays.asList(
				"csse374.revengd.examples.fixtures.CalculatorApp",
				"csse374.revengd.examples.fixtures.CalculatorA",
				"csse374.revengd.examples.fixtures.CalculatorB",
				"csse374.revengd.examples.fixtures.ICalculator"
		);
		
		this.allUnRechableClassesFromEntry = Arrays.asList(
				"csse374.revengd.examples.fixtures.CalculatorC",
				"csse374.revengd.examples.fixtures.UnrelatedClass"				
		);
		
		this.allFixtureClasses = new ArrayList<>();
		this.allFixtureClasses.addAll(this.allRechableClassesFromEntry);
		this.allFixtureClasses.addAll(this.allUnRechableClassesFromEntry);

		// Needed for running tests from Gradle
		Path buildResourcesTestPath = Paths.get("build", "resources", "test");
		if(!buildResourcesTestPath.toFile().exists()) {
			buildResourcesTestPath.toFile().mkdirs();
		}
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testCreate() {
		assertNotNull(builder);
	}

	@Test
	public void testAddClassPath() {
		List<String> classPaths = Arrays.asList("abc", "def");		
		classPaths.forEach(path -> builder.addClassPath(path));
		
		assertEquals(classPaths, builder.classPaths);
	}

	@Test
	public void testAddClassPaths() {
		List<String> classPaths = Arrays.asList("abc", "def");		
		builder.addClassPaths(classPaths);
		
		assertEquals(classPaths, builder.classPaths);
	}

	@Test
	public void testAddClass() {
		List<String> classes = Arrays.asList("abc", "def");		
		classes.forEach(clazz -> builder.addClass(clazz));
		
		assertEquals(classes, builder.classes);
	}

	@Test
	public void testAddClasses() {
		List<String> classes = Arrays.asList("abc", "def");		
		builder.addClasses(classes);
		
		assertEquals(classes, builder.classes);
	}
	
	@Test
	public void testAddDirectory() {
		List<String> dirs = Arrays.asList("abc", "def");		
		dirs.forEach(dir -> builder.addDirectory(dir));
		
		assertEquals(dirs, builder.dirsToProcess);
	}

	@Test
	public void testAddDirectories() {
		List<String> dirs = Arrays.asList("abc", "def");		
		builder.addDirectories(dirs);
		
		assertEquals(dirs, builder.dirsToProcess);
	}

	@Test
	public void testSetEntryClass() {
		String entryClass = "package.Class";

		builder.setEntryClass(entryClass);
		assertEquals(entryClass, builder.entryClassToLoad);
	}

	@Test
	public void testAddEnterPointMatcher() {
		List<IEntryPointMatcher> matchers = Arrays.asList(mock(IEntryPointMatcher.class), mock(IEntryPointMatcher.class));
		matchers.forEach(matcher -> builder.addEntryPointMatcher(matcher));
		
		assertEquals(matchers, builder.matchers);
	}

	@Test
	public void testAddEnterPointMatchers() {
		List<IEntryPointMatcher> matchers = Arrays.asList(mock(IEntryPointMatcher.class), mock(IEntryPointMatcher.class));
		builder.addEntryPointMatchers(matchers);
		
		assertEquals(matchers, builder.matchers);
	}
	
	@Test
	public void testAddExclusion() {
		List<String> exclusions = Arrays.asList("abc.*", "def.abc.*");
		exclusions.forEach(exclusion -> builder.addExclusion(exclusion));
		
		assertEquals(exclusions, builder.exclusions);
	}

	@Test
	public void testAddExclusions() {
		List<String> exclusions = Arrays.asList("abc.*", "def.abc.*");
		builder.addExclusions(exclusions);
		
		assertEquals(exclusions, builder.exclusions);
	}
	
	@Test
	public void testShouldLoadClassesOnlyRechableFromMain() {
		final Scene scene = SceneBuilder.create()
				.addExclusions(Arrays.asList("sun.*", "soot.*", "polygot.*"))
				.setEntryClass("csse374.revengd.examples.fixtures.CalculatorApp")
				.addEntryPointMatcher(new MainMethodMatcher("csse374.revengd.examples.fixtures.CalculatorApp"))
				.build();

		this.allRechableClassesFromEntry.forEach(clazz -> {
			assertTrue(scene.containsClass(clazz));
		});

		this.allUnRechableClassesFromEntry.forEach(clazz -> {
			assertFalse(scene.containsClass(clazz));
		});
	}
	
	@Test
	public void testShouldLoadClassesRechableFromMainAndManuallyLoadedClasses() {
		final Scene scene = SceneBuilder.create()
				.addExclusions(Arrays.asList("sun.*", "soot.*", "polygot.*"))
				.setEntryClass("csse374.revengd.examples.fixtures.CalculatorApp")
				.addClass("csse374.revengd.examples.fixtures.CalculatorC")
				.addClass("csse374.revengd.examples.fixtures.UnrelatedClass")
				.addEntryPointMatcher(new MainMethodMatcher("csse374.revengd.examples.fixtures.CalculatorApp"))
				.build();

		this.allFixtureClasses.forEach(clazz -> {
			assertTrue(scene.containsClass(clazz));
		});
	}	

	@Test
	public void testShouldLoadClassesFromJDK() {
		final String jComponentName = "javax.swing.JComponent";
		final Scene scene = SceneBuilder.create()
				.addExclusions(Arrays.asList("soot.*", "polygot.*"))
				.setEntryClass("csse374.revengd.examples.fixtures.CalculatorApp")
				.addClass(jComponentName)
				.addEntryPointMatcher(new MainMethodMatcher("csse374.revengd.examples.fixtures.CalculatorApp"))
				.build();

		// Check if the JComponent is loaded successfully
		SootClass jComponentClass = scene.getSootClass(jComponentName);
		assertNotNull(jComponentClass);
		
		// Check if the "void paint(Graphics)" method is present
		SootMethod method = jComponentClass.getMethodByName("paint");
		assertTrue(method.isPublic());
		assertEquals(method.getParameterCount(), 1);
		
		// Check if the body of the paint method is also loaded
		assertNotNull(method.retrieveActiveBody());
		
		// Check if the hierarchy information is preserved
		SootClass containerClass = jComponentClass.getSuperclass();
		assertNotNull(containerClass);
		
		// Check if the "void getComponent(int)" method is present in the Container class
		method = containerClass.getMethodByName("getComponent");
		assertTrue(method.isPublic());
		assertEquals(method.getParameterCount(), 1);
				
		// Check that the fields of the java.awt.Container class is also loaded
		// Looks up the layoutMgr field of the Container class
		assertNotNull(containerClass.getFieldByName("layoutMgr"));
		
		// Check that the super class of the super is also loaded correctly
		assertEquals("java.awt.Component", containerClass.getSuperclass().getName());
		
		// At this point it would be safe to conclude that the JDK class can be loaded correctly!
	}

	@Test
	public void testProcessDirShouldAnalyzeAllClassesInDir() {
		Path path = Paths.get("build", "classes", "main");
		if(!path.toFile().exists()) {
			path = Paths.get("bin");
			
			if(!path.toFile().exists()) {
				fail("Could not find the classes directory - [" + path.toAbsolutePath().toString() + "]");
			}
		}
		
		String dirToProcess = path.toAbsolutePath().toString();
		System.out.println("Processing test analysis from: " + dirToProcess);
		
		final Scene scene = SceneBuilder.create()
				.addExclusions(Arrays.asList("sun.*", "soot.*", "polygot.*"))
				.setEntryClass("csse374.revengd.examples.fixtures.CalculatorApp")
				.addEntryPointMatcher(new MainMethodMatcher("csse374.revengd.examples.fixtures.CalculatorApp"))
				.addDirectory(path.toAbsolutePath().toString())
				.build();

		
		this.allFixtureClasses.forEach(clazz -> {
			assertTrue(scene.containsClass(clazz));
		});
	}
}
