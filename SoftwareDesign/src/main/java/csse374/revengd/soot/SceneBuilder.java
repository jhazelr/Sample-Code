package csse374.revengd.soot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import csse374.revengd.soot.SceneBuilder;
import soot.G;
import soot.PackManager;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.options.Options;

/**
 * This class is a thin wrapper to help with setting up SOOT for whole-program analysis.
 * The API can be used by calling {@link #create()} followed by calls to other config
 * methods, such as, {@link #addDirectory(String)}, {@link #setEntryClass(String)}, and
 * {@link #addEntryMethod(String)}, and then finally calling {@link #build()} to acquire 
 * the {@link soot.Scene} object, which has the result of the analysis. Here is a sample use:
 * <pre>
		Scene scene = SceneBuilder.create()
				.addDirectory(pathToBinOrSrcDir)										
				.setEntryClass("class.with.Main")									
				.addEntryPointMatcher(new MainMethodMatcher("class.with.Main"))
				.addExclusions(Arrays.asList("java.*", "javax.*", "sun.*")) 			
				.addExclusions(Arrays.asList("soot.*", "polygot.*"))					 
				.addExclusions(Arrays.asList("org.*", "com.*"))						 
				.build();

		// Let's list all of the classes we have loaded from the supplied directory. 
		// These classes are called application classes in SOOT
		System.out.println("Application classes loaded by SOOT:");
		scene.getApplicationClasses().forEach(clazz -> {
			System.out.println(clazz.getName() );
		});

		// We can also lookup a class using the Scene API, see below
		SootClass appClass = scene.getSootClass("csse374.revengd.examples.fixtures.CalculatorApp");

		// Do something with the class ...
 * </pre>
 */
public class SceneBuilder {
	private static final Logger logger = LogManager.getLogger(SceneBuilder.class.getName());

	List<String> classPaths;
	List<String> classes;
	List<String> dirsToProcess;
	List<String> exclusions;
	List<IEntryPointMatcher> matchers;
	
	String entryClassToLoad;
	String entryMethodToLoad;
	Map<String, SootClass> nameToClassMap;
	

	/**
	 * Creates an empty SceneBuilder. SceneBuilder is a wrapper class
	 * for setting up soot for he whole-program analysis.
	 *  
	 * @return SceneBuilder
	 * @see #build()
	 */
	public static SceneBuilder create() {
		return new SceneBuilder();
	}

	private SceneBuilder() {
		this.classPaths = new ArrayList<>();
		this.classes = new ArrayList<>();
		this.dirsToProcess = new ArrayList<>();
		this.exclusions = new ArrayList<>();
		this.matchers = new ArrayList<>();
		this.nameToClassMap = new HashMap<>();
	}
	
	/**
	 * Use it to add a custom path to a directory or Jar file as a class path entry
	 * for SOOT to find the classes that need loading.
	 * 
	 * @param classPath A class path entry 
	 * @return {@link SceneBuilder} The builder object being used to build a {@link Scene}.
	 * @see {@link #addDirectory(String)}, {@link #addDirectories(Collection)}
	 */
	public SceneBuilder addClassPath(String classPath) {
		this.classPaths.add(classPath);
		logger.debug("Classpath added - " + classPath);
		return this;
	}
	
	/**
	 * Use it to add custom entries (directories or jars) as class path entries
	 * for SOOT to find the classes that need loading.
	 * 
	 * @param classPaths A list of class path entries.
	 * @return {@link SceneBuilder} The builder object being used to build a {@link Scene}.
	 * @see {@link #addDirectory(String)}, {@link #addDirectories(Collection)}
	 */
	public SceneBuilder addClassPaths(Collection<String> classPaths) {
		this.classPaths.addAll(classPaths);
		logger.debug("Classpaths added - " + classPaths);
		return this;
	}
	
	/**
	 * Use it to add a class to be loaded for analysis by SOOT. You can use this method of
	 * adding class if you want to have a fine-grained control over what classes you want
	 * to load in SOOT for analysis. If you want to load all classes in a folder(s), then use
	 * {@link #addDirectory(String)} or {@link #addDirectories(Collection)} methods. 
	 * 
	 * @param clazz A fully-qualified class class name.
	 * @return {@link SceneBuilder} The builder object being used to build a {@link Scene}.
	 */
	public SceneBuilder addClass(String clazz) {
		this.classes.add(clazz);
		logger.debug("Class added for loading - " + clazz);
		return this;
	}
	
	/**
	 * Use it to add a collection of classes to be loaded for analysis by SOOT. 
	 * You can use this method of adding classes if you want to have a fine-grained control 
	 * over what classes you want to load in SOOT for analysis. If you want to load all classes 
	 * in a folder(s), then use {@link #addDirectory(String)} or {@link #addDirectories(Collection)} 
	 * methods. 
	 * 
	 * @param classes A collection of class names.
	 * @return {@link SceneBuilder} The builder object being used to build a {@link Scene}.
	 */
	public SceneBuilder addClasses(Collection<String> classes) {
		this.classes.addAll(classes);
		logger.debug("Class added for loading - " + classes);
		return this;
	}

	/**
	 * Use it to process all java file or class files (including packages) in a directory.
	 * SOOT will try to load all of the classes (and dependent classes) in this directory.
	 * If the dependent classes are not found in the directory, then SOOT will look for them
	 * in the supplied class path. 
	 * 
	 * @param dir A directory to be loaded in SOOT.
	 * @return {@link SceneBuilder} The builder object being used to build a {@link Scene}.
	 * @see {@link #addClassPath(String)}, {@link #addClassPaths(Collection)}
	 */
	public SceneBuilder addDirectory(String dir) {
		this.dirsToProcess.add(dir);
		logger.debug("Directory added for analysis - " + dir);		
		return this;
	}

	/**
	 * Use it to process all java file or class files (including packages) in the supplied directories.
	 * SOOT will try to load all of the classes (and dependent classes) in these directories.
	 * If the dependent classes are not found in the directories, then SOOT will look for them
	 * in the supplied class path.
	 *  
	 * @param dirs A collection of directories to be loaded in SOOT.
	 * @return {@link SceneBuilder} The builder object being used to build a {@link Scene}.
	 * @see {@link #addClassPath(String)}, {@link #addClassPaths(Collection)}
	 */
	public SceneBuilder addDirectories(Collection<String> dirs) {
		this.dirsToProcess.addAll(dirs);
		logger.debug("Directories added for analysis - " + dirs);		
		return this;
	}
	
	/**
	 * The {@link SceneBuilder} API is designed to help setup SOOT for the whole program analysis.
	 * The whole-program analysis needs a class(s) with the <tt>main</tt> method as the starting point.
	 * Use this method to set the main class.
	 * 
	 * @param clazz The fully-qualified name of the main class. e.g., <tt>my.package.MainClass</tt>
	 * @return {@link SceneBuilder} The builder object being used to build a {@link Scene}.
	 */
	public SceneBuilder setEntryClass(String clazz) {
		this.entryClassToLoad = clazz;
		logger.debug("Entry class set - " + clazz);		
		return this;
	}
	
	/**
	 * Adds an {@link IEntryPointMatcher} to {@link SceneBuilder}. The entry points ({@link SootMethod}s)
	 * is used to construct call-graph.
	 * 
	 * @param matcher An {@link IEntryPointMatcher} to be added
	 * @return {@link SceneBuilder} The builder object being used to build a {@link Scene}.
	 */
	public SceneBuilder addEntryPointMatcher(IEntryPointMatcher matcher) {
		this.matchers.add(matcher);
		logger.debug("Entry method matcher added");		
		return this;
	}
	
	/**
	 * Adds a collection of {@link IEntryPointMatcher} to {@link SceneBuilder}. The entry points 
	 * ({@link SootMethod}s) is used to construct call-graph.
	 * 
	 * @param matchers A collection of {@link IEntryPointMatcher} to be added
	 * @return {@link SceneBuilder} The builder object being used to build a {@link Scene}.
	 */
	public SceneBuilder addEntryPointMatchers(Collection<IEntryPointMatcher> matchers) {
		this.matchers.addAll(matchers);
		logger.debug("Entry method matchers added");
		return this;
	}

	/**
	 * The whole-program analysis can run slow if the user-defined classes import some of the default 
	 * JDK classes. If the analysis does not need the JDKs classes or for that matter classes from
	 * dependent libraries, then they can be excluded from analysis using this method. Doing so
	 * will improve SOOT's performance. 
	 * 
	 * @param exclusion The libraries (pattern) to be excluded. e.g., <tt>java.* or javax.*</tt>
	 * @return {@link SceneBuilder} The builder object being used to build a {@link Scene}.
	 */
	public SceneBuilder addExclusion(String exclusion) {
		this.exclusions.add(exclusion);
		logger.debug("Exclusion pattern added - " + exclusion);		
		return this;
	}
	
	/**
	 * The whole-program analysis can run slow if the user-defined classes import some of the default 
	 * JDK classes. If the analysis does not need the JDKs classes or for that matter classes from
	 * dependent libraries, then they can be excluded from analysis using this method. Doing so
	 * will improve SOOT's performance.
	 *  
	 * @param exclusions A collection of libraries to be included.
	 * @return {@link SceneBuilder} The builder object being used to build a {@link Scene}.
	 * @see SceneBuilder#addExclusion(String)
	 */
	public SceneBuilder addExclusions(Collection<String> exclusions) {
		this.exclusions.addAll(exclusions);
		logger.debug("Exclusion patterns added - " + exclusions);		
		return this;
	}

	/**
	 * Configures SOOT for whole-program analysis based on the intermediate configuration
	 * method calls. Runs all of the SOOT analysis and finally, returns the {@link soot.Scene}
	 * object, which contains the result of the analysis.
	 * @return
	 */
	public Scene build() {
		String classPath = this.buildClassPath();
		logger.info("Setting up classpath for analysis- " + classPath);		
		
		G.reset();

		Options options = Options.v();

		if(!this.dirsToProcess.isEmpty()) {
			options.set_process_dir(this.dirsToProcess);
			logger.info("Soot is going to process directory(s) - " + this.dirsToProcess);
		}
		
		options.set_output_dir("build/sootOutput");
		options.set_output_format(Options.output_format_jimple);
		
		options.set_no_bodies_for_excluded(true);
		if(!this.exclusions.isEmpty())
			options.set_exclude(this.exclusions);
		options.set_allow_phantom_refs(true);

		options.set_keep_line_number(true);
		options.set_whole_program(true);
		options.setPhaseOption("jb","use-original-names:true");
		options.setPhaseOption("cg","verbose:true");
		options.setPhaseOption("cg.spark", "on");

		options.set_soot_classpath(classPath);
		options.set_prepend_classpath(true);
		
		Scene scene = Scene.v();

		for(String className: this.classes) {
			scene.addBasicClass(className, SootClass.BODIES);
		}
		
		this.loadMainClass(this.entryClassToLoad);
		try {
			scene.loadNecessaryClasses();
		}
		catch(Exception e) {
			logger.error("SOOT is unable to load necessarry classes.", e);
			throw e;
		}

		scene.setEntryPoints(this.computeEntryPoints(scene));

		logger.info("Running SOOT analysis ...");
		PackManager manager = PackManager.v(); 
		manager.runPacks();
		logger.info("SOOT analysis complete!");

		return scene;
	}
	
	private List<SootMethod> computeEntryPoints(Scene scene) {
		List<SootMethod> methods = new ArrayList<SootMethod>();
		for(SootClass clazz: scene.getApplicationClasses()) {
			for(IEntryPointMatcher matcher: this.matchers) {
				if(matcher.match(clazz) && clazz.getMethodCount() > 0) {
					for(SootMethod method: clazz.getMethods()) {
						if(matcher.match(method)) {
							methods.add(method);
						}
					}
				}
			}
		}
		return methods;
	}
	
	private String buildClassPath() {
		StringBuilder builder = new StringBuilder();

		String projectClassPath = System.getProperty("java.class.path");		
		builder.append(projectClassPath);
		
		final String sep = System.getProperty("os.name").toLowerCase().contains("windows") ? ";" : ":";
		
		if(!projectClassPath.contains("rt.jar")) {
			String defaultClassPath = Scene.v().defaultClassPath();
			builder.append(sep);
			builder.append(defaultClassPath);
		}
		
		this.classPaths.forEach(path -> {
			builder.append(sep);
			builder.append(path);
		});

		return builder.toString();
	}
	
	// Load class here returns null so we might expect a null pointer exception somewhere
	private SootClass loadAppClass(String name) {
		try {
			logger.info("Loading " + name + " ...");
			SootClass c = Scene.v().loadClassAndSupport(name);
			c.setApplicationClass();
			return c;
		}
		catch(Exception e) {
			logger.error("SOOT is unable to load " + name, e);
			throw e;
		}
	}
	
	// Load class here returns null so we might expect a null pointer exception somewhere
	private SootClass loadMainClass(String name) {
		SootClass c = this.loadAppClass(name);
		if(c != null) {
			try {
				Scene.v().setMainClass(c);
			}
			catch(Exception e) {
				logger.error("Cannot set [" + c.getName() + "] as main class.", e);
				throw e;
			}
			logger.info(name + " is set to be the main class");
		}
		return c;
	}
}
