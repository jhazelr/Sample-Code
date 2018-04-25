package csse374.revengd.examples.fixtures;

public class CalculatorC extends CalculatorA {
	@Override
	public double multiply(double... args) {
		if(args == null || args.length < 1)
			throw new IllegalArgumentException("Arguments cannot be empty for multiplication.");

		return super.multiply(args);
	}
}
