package csse374.revengd.examples.driver;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

public class ExamplesDriver {
	List<Runnable> examples;
	
	public ExamplesDriver() {
		this.examples = Arrays.asList(
				new E1SimpleDirectoryLoading(),
				new E2TypeHierarchy(),
				new E3ControlFlowGraph(),
				new E4PointerAnalysis(),
				new E6ExtractGenericsInfo()
		);
	}
	
	void repl() throws Exception {
		String input = null;
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		while(true) {
			System.out.println("========================= Your choices ========================= ");
			for(int i = 0; i < this.examples.size(); ++i){
				System.out.format("%d - %s%n", i+1, this.examples.get(i).getClass().getSimpleName());
			}
			System.out.println("========================== End choices ========================= ");
			System.out.print("Please enter your choice (number) or press q to quit: ");

			input = reader.readLine().trim().toLowerCase();
			if(input.startsWith("q") || input.startsWith("e") || input.startsWith("x"))
				return;
			
			int choice = -1;
			try {
				choice = Integer.parseInt(input);
			}
			catch (Exception e) {
				System.out.println("Invalide choice! Please try again!");
				continue;
			}
			
			if(choice < 1 || choice > this.examples.size()) {
				System.out.println("Invalide choice! Please try again!");
				continue;				
			}
			
			Runnable runnable = this.examples.get(choice-1);
			System.out.println("Running " + runnable.getClass().getSimpleName() + " ...");
			runnable.run();
			System.out.println("All done!");
		}		
	}
	
	public static void main(String[] args) throws Exception {
		ExamplesDriver driver = new ExamplesDriver();
		driver.repl();
	}
}
