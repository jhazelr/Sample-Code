package csse374.revengd.examples.fixtures;

public class UnrelatedClass {

	public UnrelatedClass() {
	}

	public void sayConditionalHello(boolean goodMood) {
		if(goodMood)
			System.out.println("Aloha! What a beautiful day!");
		else
			System.out.println("Hey!");
		
		System.out.println("Just adding some numbers: " + new CalculatorA().add(1,2,3));
	}
}
