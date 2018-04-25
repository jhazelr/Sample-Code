package csse374.revengd.app;

public class Single {

	private static Single single;
	private Hello hello;
	private Single() {
		
	}
	
	public static Single getInstance() {
		return single;
	}
}
