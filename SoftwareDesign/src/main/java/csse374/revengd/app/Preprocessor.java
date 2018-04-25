package csse374.revengd.app;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import csse374.revengd.app.filter.IFilter;
import csse374.revengd.app.filter.PrivateFilter;
import csse374.revengd.app.filter.ProtectedFilter;
import csse374.revengd.app.filter.PublicFilter;
import csse374.revengd.app.filter.SyntheticFilter;
import csse374.revengd.app.model.Database;
import csse374.revengd.app.model.Dependency;
import csse374.revengd.app.model.Pattern;
import csse374.revengd.app.renderer.IRenderer;
import csse374.revengd.app.transformer.AssociationTransformer;
import csse374.revengd.app.transformer.DependencyTransformer;
import csse374.revengd.app.transformer.PrivacyTransformer;
import csse374.revengd.app.transformer.RecursiveTransformer;
import csse374.revengd.app.transformer.SDRenderingTransformer;
import csse374.revengd.app.transformer.SuperDependencyTransformer;
import csse374.revengd.app.transformer.Transformer;
import csse374.revengd.app.transformer.UMLRenderingTransformer;
import csse374.revengd.soot.MainMethodMatcher;
import csse374.revengd.soot.SceneBuilder;
import soot.Scene;
import soot.SootClass;

public class Preprocessor {
	private HashMap<String, String> config;
	private HashMap<String, IFilter> filters;
	private Database db;
	private Scene scence;
	private List<Transformer> transform;
	private List<IFilter> filter;

	public Preprocessor(HashMap<String, String> config)
			throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		this.config = config;
		this.filters = new HashMap<>();
		this.filters.put("private", new PrivateFilter());
		this.filters.put("public", new PublicFilter());
		this.filters.put("protected", new ProtectedFilter());
		this.filters.put("synthetic", new SyntheticFilter());
		this.makeDB();
		this.filter = new ArrayList<>();

		this.transform = initializeTransformers();
	}

	private void makeDB() {
		makeScene(this.config.get("-dir"), config.get("-entry"));
		Collection<SootClass> folderClasses = getFolder(config.get("-entry"));
		List<SootClass> classes = getClasses(config.get("-class"));

		if (folderClasses != null) {
			for (SootClass folderClazz : folderClasses) {
				boolean found = false;
				for (SootClass clazz : classes) {
					if (clazz.toString().equals(folderClazz.toString())) {
						found = true;
					}
				}
				if (!found) {
					classes.add(folderClazz);
				}
			}
		}

		// System.err.println(classes);

		this.db = new Database(config, this.scence);
		this.db.putObject("classes", classes);
		this.db.putObject("dependencies", new ArrayList<Dependency>());
		this.db.putObject("patterns", new ArrayList<Pattern>());
		if (config.containsKey("-blist")) {
			this.db.putObject("blist", getBlist(config.get("-blist")));
		} else {
			this.db.putObject("blist", new ArrayList<String>());
		}
		// Set classes manually
	}

	private Collection<SootClass> getFolder(String entryClass) {
		SceneBuilder builder = SceneBuilder.create();
		if (this.config.containsKey("-folder")) {
			Scene tempScene = builder
					// Add the directory from which to load the file or jars
					.addDirectory(this.config.get("-folder"))
					// Sets the entry point class for the application under
					// analysis
					.setEntryClass(entryClass)
					// Matches the main method
					.addEntryPointMatcher(new MainMethodMatcher(entryClass))
					// Exclude SOOT classes from analysis
					.addExclusions(Arrays.asList("soot.*", "polygot.*"))
					// Exclude other library classes from analysis
					.addExclusions(Arrays.asList("org.*", "com.*")).build();

			return tempScene.getApplicationClasses();
		}
		return null;
	}

	private void makeScene(String dir, String entryClass) {
		// dir = dir + "/build/classes/main";
		System.out.println(dir);
		this.scence = SceneBuilder.create()
				// Add the directory from which to load the file or jars
				.addDirectory(dir)
				// Sets the entry point class for the application under analysis
				.setEntryClass(entryClass)
				// Matches the main method
				.addEntryPointMatcher(new MainMethodMatcher(entryClass))
				// Exclude SOOT classes from analysis
				.addExclusions(Arrays.asList("soot.*", "polygot.*"))
				// Exclude other library classes from analysis
				.addExclusions(Arrays.asList("org.*", "com.*")).build();
	}

	private List<Transformer> initializeTransformers()
			throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		this.filter.addAll(initializeFilters());

		List<Transformer> transform = new ArrayList<>();
		if (this.config.containsKey("-r")) {
			transform.add(new RecursiveTransformer(new ArrayList<IFilter>()));
		}

		transform.add(new PrivacyTransformer(filter));
		transform.add(new SuperDependencyTransformer(new ArrayList<IFilter>()));
		transform.add(new AssociationTransformer(filter));
		transform.add(new DependencyTransformer(filter));

		// this.initializeDetectors();

		return transform;
	}

	private List<IFilter> initializeFilters() {
		List<IFilter> filter = new ArrayList<IFilter>();
		for (String k : this.config.keySet()) {
			if (this.filters.containsKey(this.config.get(k).toLowerCase())) {
				filter.add(this.filters.get(this.config.get(k).toLowerCase()));
			}
		}
		return filter;
	}

	private List<SootClass> getClasses(String classes) {
		if (classes.length() < 1) {
			return new ArrayList<>();
		}

		List<SootClass> classList = new ArrayList<>();

		String[] classesArray = classes.split(",");
		for (String className : classesArray) {
			classList.add(this.scence.getSootClass(className));
		}

		System.out.println(classList.size());
		return classList;
	}

	private List<String> getBlist(String blist) {
		if (blist.length() < 1) {
			return new ArrayList<>();
		}

		List<String> bList = new ArrayList<>();

		String[] bListArray = blist.split(",");
		for (String prefix : bListArray) {
			bList.add(prefix);
		}
		return bList;
	}

	public void makeUML(Map<String, IRenderer> rendererMap)
			throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		this.transform.add(new UMLRenderingTransformer(filter, rendererMap));
		// initializeRenderLogic();

		if (this.config.containsKey("-sd")) {
			transform.add(new SDRenderingTransformer(filter));
		}

		Database dbTemp = this.db;
		for (int i = 0; i < transform.size(); i++) {
			dbTemp = transform.get(i).transform(dbTemp);
		}
	}

	public void addFilter(String key, IFilter filter) {
		this.filters.put(key, filter);
		for (Transformer t : this.transform) {
			t.addFilter(filter);
		}
	}

	public void addTransformer(Transformer transform) {
		this.transform.add(transform);
	}

	public void addItemToDB(String key, Object obj) {
		this.db.putObject(key, obj);
	}

}
