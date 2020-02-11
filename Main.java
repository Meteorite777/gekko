import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.LinkedList;

public class Main {
	
	int populationSize = 33;
	int numberOfGenerations = 33;
	
	double mutationRate = 0.90;
	
	LinkedList<Individual> population;
	Individual bestIndividual = null;
	
	
	Main(){
		initializePopulation();
		
		//Main loop
		for(int i = 1; i <= numberOfGenerations; i++) {
			System.out.println("\n->\n-> Generation #" + i);
			selection();
			mutation();
			removeNullTrades();
			for(Individual in : population) {
				System.out.println(in);
			}
		}
		
		for(Individual i : population) {
			i.getFitness();
		}
		
		setBestIndividual();
		
		System.out.println("\n\nBest: " + bestIndividual.toString());
		
		System.out.println("\nOTHERS:");
		for(Individual i : population) {
			System.out.println(i);
		}
		System.out.println("-------------------------------------------------------");
		
		exportBest();
		
	}
	
	private void exportBest() {	
		
		bestIndividual.writeConfigFile();
		
		try {
			PrintWriter out = new PrintWriter(new FileOutputStream("/home/osboxes/Documents/Gekko/gekko/best_run.txt", false));
			out.print(bestIndividual.terminal);
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

	private void mutation() {
		LinkedList<Individual> mutatants = new LinkedList<Individual>();
		
		for(Individual ind : population) {
			if(Math.random() < mutationRate) {
				mutatants.add(ind.mutate());
			}
		}
		
	}

	private void initializePopulation() {
		population = new LinkedList<Individual>();
		
		for(int i = 0; i < populationSize; i++) {
			Individual ind = new Individual();
			ind.randomize();
			population.add(ind);
		}
	}
	
	private void removeNullTrades() {
		LinkedList<Individual> remove = new LinkedList<Individual>();
		
		for(Individual ind : population) {
			if(ind.fitness == Double.MIN_VALUE + 1) {
				remove.add(ind);
			}
		}
		
		population.removeAll(remove);
		
		//If the population became small. Add new random individuals.
		
		for(int i = population.size(); i < populationSize; i++) {
			Individual ind = new Individual();
			ind.randomize();
			population.add(ind);
		}
	}
	
	private void selection() {
		LinkedList<Individual> nextPopulation = new LinkedList<Individual>();
		
		setBestIndividual();
		nextPopulation.add(bestIndividual);
		
		if(populationSize / 4 - 1 <= 0) {
			System.err.print("The population size is too small");
			System.exit(-1);
		}
		
		for(int i = 0; i < populationSize / 4 - 1; i++) {
			//Select 2 random individuals
			Individual ind1 = population.remove((int) Math.random() * population.size());
			Individual ind2 = population.remove((int) Math.random() * population.size());
			Individual ind3 = population.remove((int) Math.random() * population.size());
			
			if(ind1.getFitness() > ind2.getFitness() && ind2.getFitness() > ind3.getFitness()) {
				//cross 1 and 2
				population.add(ind3); //put 3 back in.
				nextPopulation.add(ind1);
				nextPopulation.add(ind2);
				nextPopulation.addAll(ind1.cross(ind2));
			}else if(ind2.getFitness() > ind3.getFitness() && ind3.getFitness() > ind1.getFitness()) {
				//cross 2 and 3
				population.add(ind1); //put 1 back in
				nextPopulation.add(ind3);
				nextPopulation.add(ind2);
				nextPopulation.addAll(ind3.cross(ind2));
			}else {
				//cross 1 and 3
				population.add(ind2); //put 2 back in
				nextPopulation.add(ind1);
				nextPopulation.add(ind3);
				nextPopulation.addAll(ind1.cross(ind3));
			}
		}
		
		population = nextPopulation;
	}
	
	
	
	private void setBestIndividual() {
		for(Individual ind : population) {
			if(bestIndividual == null) {
				bestIndividual = ind;
			}else {
				if(ind.fitness > bestIndividual.fitness) {
					bestIndividual = ind;
				}
			}
		}
	}
	
	public static void main(String [] args) {
		new Main();
	}
}
