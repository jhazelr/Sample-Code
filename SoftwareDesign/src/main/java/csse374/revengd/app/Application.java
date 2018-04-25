package csse374.revengd.app;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import csse374.revengd.app.algorithms.AggregateAlgorithm;
import csse374.revengd.app.algorithms.HierarchyAlgorithm;
import csse374.revengd.app.algorithms.IAlgorithm;
import csse374.revengd.app.algorithms.IResolutionStragy;
import csse374.revengd.app.algorithms.SingleAlgorithm;
import csse374.revengd.app.filter.IFilter;
import csse374.revengd.app.renderer.IRenderer;
import csse374.revengd.app.transformer.Transformer;

public class Application extends Hello {

	public static void main(String[] args)
			throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
		String rootPath = System.getProperty("user.dir") + "/src/main/java/csse374/revengd/app/settings/";
		String defaultConfigPath = rootPath + "default.properties";
		Properties defaultProps = new Properties();
		defaultProps.load(new FileInputStream(defaultConfigPath));

		HashMap<String, String> map = new HashMap<>();

		Set<Object> keys = defaultProps.keySet();
		Iterator<Object> itr = keys.iterator();

		while (itr.hasNext()) {
			String key = (String) itr.next();
			map.put(key, defaultProps.getProperty(key));
		}

		for (int i = 0; i < args.length; i++) {
			String arg = args[i];
			if (arg.charAt(0) == '-') {
				if (!arg.contains("sfile")) {
					break;
				}
				continue;
			}

			defaultProps = new Properties();
			defaultProps.load(new FileInputStream(arg));
			keys = defaultProps.keySet();
			itr = keys.iterator();

			while (itr.hasNext()) {
				String key = (String) itr.next();
				map.put(key, defaultProps.getProperty(key));
			}

		}

		System.out.println(map.get("-irt"));

		if (!map.containsKey("-dir") && !map.containsKey("-entry")) {
			throw new Error("Invalid command, you must include -dir,  -entry, and -save");
		}
		if (!map.containsKey("-p")) {
			map.put("-p", "private");
		}
		String p = map.get("-p").toLowerCase();
		map.put("-p", p);

		if (map.containsKey("-sd") && !map.containsKey("-sdn")) {
			map.put("-sdn", "5");
		}

		Preprocessor preproc = new Preprocessor(map);

		if (map.containsKey("-aTransformers")) {
			if (!map.get("-aTransformers").isEmpty()) {
				String[] transformers = map.get("-aTransformers").split(",");
				for (String transformer : transformers) {
					try {
						@SuppressWarnings("unchecked")
						Class<? extends Transformer> newTrans = (Class<? extends Transformer>) Class
								.forName(transformer);
						Transformer additionalTransformer = newTrans.newInstance();
						preproc.addTransformer(additionalTransformer);
					} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
						System.err.println("Fail to load transformer " + transformer);
					}
				}
			}
		}

		if (map.containsKey("-afilters")) {
			if (!map.get("-afilters").isEmpty()) {
				String[] filters = map.get("-afilters").split(",");
				for (String filter : filters) {
					try {
						IFilter newFilter = (IFilter) Class.forName(filter).newInstance();
						preproc.addFilter("", newFilter);
					} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
						System.err.println("Fail to load filter " + filter);
					}
				}
			}
		}

		if (map.containsKey("-irt") && map.containsKey("-sd")) {
			IResolutionStragy resolution = null;
			String resolutionType = map.get("-irt");

			resolution = (IResolutionStragy) Class.forName(resolutionType).newInstance();
			List<IAlgorithm> algorithms = new ArrayList<>();
			if (!map.get("-aAlg").isEmpty()) {
				String[] algs = map.get("-aAlg").split(",");
				for (String alg : algs) {
					try {
						IAlgorithm newAlg = (IAlgorithm) Class.forName(alg).newInstance();
						algorithms.add(newAlg);
					} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
						System.err.println("Fail to load algorithm " + alg);
					}
				}
			}

			if ((algorithms.size() < 2) && !resolutionType.equals("csse374.revengd.app.algorithms.SingleAlgorithm")) {
				throw new Error("Can't Run WIthout A provided Algorithm");
			}

			resolution.addAlgorithmList(algorithms);

			AggregateAlgorithm agAlg = new AggregateAlgorithm(resolution);
			preproc.addItemToDB("aggAlg", agAlg);
		} else if (!map.containsKey("-irt") && map.containsKey("-sd")) {

			IResolutionStragy resolution = new SingleAlgorithm();
			resolution.addAlgorithm(new HierarchyAlgorithm());
			AggregateAlgorithm agAlg = new AggregateAlgorithm(resolution);
			preproc.addItemToDB("aggAlg", agAlg);
		}

		Map<String, IRenderer> rendererMap = new HashMap<>();
		if (map.containsKey("-pdetectors")) {
			String[] detectorsArray = map.get("-pdetectors").split(",");
			StringBuilder renderLogic = new StringBuilder();
			for (String block : detectorsArray) {
				String[] blockArray = block.split(":");
				try {
					@SuppressWarnings("unchecked")
					Class<? extends Transformer> detector = (Class<? extends Transformer>) Class.forName(blockArray[0]);
					Transformer additionalTransformer = detector.newInstance();
					String[] renderBlock = blockArray[1].split(";");
					additionalTransformer.setName(renderBlock[0]);
					preproc.addTransformer(additionalTransformer);
				} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
					System.err.println("Fail to load detector " + blockArray[0]);
				}
				renderLogic.append(blockArray[1]);
				renderLogic.append(",");
			}
			String logic = renderLogic.toString();
			logic = logic.substring(0, logic.length() - 1);
			map.put("-renderLogic", logic);
			//////////////////////////////////////////////////////////////
			if (map.containsKey("-renderLogic")) {
				for (String renderBlock : map.get("-renderLogic").split(",")) {
					String[] blockArray = renderBlock.split(";");
					try {

						@SuppressWarnings("unchecked")
						Class<? extends IRenderer> rend = (Class<? extends IRenderer>) Class.forName(blockArray[1]);
						IRenderer newRend = rend.newInstance();
						rendererMap.put(blockArray[0], newRend);
					} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
						System.err.println("Fail to load " + blockArray[0] + " and/or " + blockArray[1]);
					}
				}
			}
		}

		preproc.makeUML(rendererMap);

		if (map.containsKey("-sd")) {
			if (map.containsKey("-sdsave")) {
				PlantUmlMaker plant = new PlantUmlMaker(map.get("-sdsave"), true);
				plant.run();
			} else {
				PlantUmlMaker plant = new PlantUmlMaker(true);
				plant.run();
			}
		}

		if (map.containsKey("-save")) {
			PlantUmlMaker plant = new PlantUmlMaker(map.get("-save"), false);
			plant.run();
		} else {
			PlantUmlMaker plant = new PlantUmlMaker(false);
			plant.run();
		}
	}

}