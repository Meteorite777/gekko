import java.util.LinkedList;

public class Main {
	
	int populationSize = 12;
	int numberOfGenerations = 3;
	double mutationRate = 0.05;
	
	LinkedList<Individual> population;
	Individual bestIndividual = null;
	
	
	Main(){
		initializePopulation();
		
		for(int i = 1; i <= numberOfGenerations; i++) {
			System.out.println("Generation #" + i);
			selection();
			mutation();
			for(Individual in : population) {
				System.out.println(in);
			}
		}
		
		System.out.println("\n\nBest: " + bestIndividual.toString());
		
		System.out.println("\nOTHERS:");
		for(Individual i : population) {
			System.out.println(i);
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
	
	private void selection() {
		LinkedList<Individual> nextPopulation = new LinkedList<Individual>();
		
		setBestIndividual();
		nextPopulation.add(bestIndividual);
		
		for(int i = 0; i < populationSize / 2 - 1; i++) {
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
	
	private void mutation() {
		for(Individual ind : population) {
			if(Math.random() < mutationRate) {
				//Mutate.
				Individual indMutant = new Individual();
				
				indMutant.stopLossPercentage = ind.stopLossPercentage;
				indMutant.deltaCloseBelowEMA = ind.deltaCloseBelowEMA;
				
				if(Math.random() > 0.5) {
					indMutant.stopLossPercentage += indMutant.stopLossPercentageMutation;
				}else {
					indMutant.stopLossPercentage -= indMutant.stopLossPercentageMutation;
				}
				
				if(Math.random() > 0.5) {
					indMutant.deltaCloseBelowEMA += indMutant.deltaCloseBelowEMAMutation;
				}else {
					indMutant.deltaCloseBelowEMA -= indMutant.deltaCloseBelowEMAMutation;
				}
				population.add(indMutant);
			}
		}
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
