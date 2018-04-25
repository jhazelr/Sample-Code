package csse374.revengd.examples.fixtures;

public class CalculatorB implements ICalculator {
	public CalculatorB() {
	}

	@Override
	public double add(double... args) {
		if(args == null || args.length < 1)
			throw new IllegalArgumentException("Arguments cannot be empty for addition.");

		double sum = 0;
		for(double arg : args) {
			sum += arg;
		}
		return sum;
	}

	@Override
	public double multiply(double... args) {
		if(args == null || args.length < 1)
			throw new IllegalArgumentException("Arguments cannot be empty for multiplication.");

		double product = 1;
		for(double arg : args) {
			product *= arg;
		}
		return product;
	}
}
