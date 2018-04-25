package csse374.revengd.examples.fixtures;

import java.util.Random;

public class CalculatorApp {
	private ICalculator calculator;
	
	public CalculatorApp(ICalculator calculator) {
		this.calculator = calculator;
	}
	
	public void performAdd() {
		System.out.println("Performing add using: " + this.calculator.getClass());
		double add = this.calculator.add(1,2,3,4);

		System.out.println("Add Result: " + add);
	}
	
	public void performMultiply() {
		System.out.println("Performing multiply using: " + this.calculator.getClass());
		double mul = this.calculator.multiply(1,2,3,4);
		
		System.out.println("Multiply Result: " + mul);
	}
	
	public static void main(String[] args) {
		ICalculator calc = null;
		
		Random randomGen = new Random(System.currentTimeMillis());
		if(randomGen.nextBoolean()) {
			calc = new CalculatorA();
		}
		else {
			calc = new CalculatorB();			
		}
		
		CalculatorApp app = new CalculatorApp(calc);
		app.performAdd();
		app.performMultiply();
	}
}
