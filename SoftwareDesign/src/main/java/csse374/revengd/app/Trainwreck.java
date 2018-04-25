package csse374.revengd.app;

import csse374.revengd.app.transformer.Transformer;

public class Trainwreck {
	public void callWorld() {
		Hello hello = new Hello();
		hello.getWorld().doSomething();
	}
	
	public void doA(){
		try {
			@SuppressWarnings("unchecked")
			Class<? extends Transformer> newTrans = (Class<? extends Transformer>) Class
			.forName("csse374.revengd.app.transformer.AdapterTransformer");
			Transformer additionalTransformer = newTrans.newInstance();
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			System.err.println("Fail to load transformer ");
		}
	}
}
