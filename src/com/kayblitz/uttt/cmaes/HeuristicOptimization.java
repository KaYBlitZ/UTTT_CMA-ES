package com.kayblitz.uttt.cmaes;

import com.theaigames.uttt.UTTTStarter;

import fr.inria.optimization.cmaes.CMAEvolutionStrategy;
import fr.inria.optimization.cmaes.fitness.IObjectiveFunction;

public class HeuristicOptimization {
	private static final int GAMES_PER_SAMPLE = 300;
	private static final int POPULATION_SIZE = 20;
	private static final int NUM_GENERATIONS = 500;
	
	private static class FitnessFunction implements IObjectiveFunction {
		@Override
		public double valueOf(double[] x) {
			UTTTStarter starter = new UTTTStarter(true);
			starter.enableHalfAndHalfMode(true);
			starter.seedBots(true, true);
			starter.disableOutput(true);
			starter.updateHeuristics(x); // update heuristics for bot using evaluation type 3
			starter.setNumConcurrentGames(2);
			starter.setNumGamesPerSample(GAMES_PER_SAMPLE);
			starter.setSampleSize(1);
			starter.setBots("java -cp D:\\Users\\Kenneth\\workspace\\UTTT_CMA-ES\\bin com.kayblitz.uttt.bot.IterativeDeepeningMinimaxBot 3", 
					"java -cp D:\\Users\\Kenneth\\workspace\\UTTT_CMA-ES\\bin com.kayblitz.uttt.bot.IterativeDeepeningMinimaxBot 2");
			starter.start();
			while (!starter.isFinished()) {
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			System.out.printf("P1 Wins/P2 Wins/Ties/Timeouts: %.1f/%.1f/%.1f/%.1f\n", starter.getAverageP1Wins(), starter.getAverageP2Wins(),
					starter.getAverageTies(), starter.getAverageTimeouts());
			return GAMES_PER_SAMPLE - (starter.getAverageP1Wins() + 0.5 * starter.getAverageTies());
		}

		@Override
		public boolean isFeasible(double[] x) {
			return true; // it's all feasible
		}
	}
	
	public static void main(String[] args) {
		IObjectiveFunction fitfun = new FitnessFunction();

		// new a CMA-ES and set some initial values
		CMAEvolutionStrategy cma = new CMAEvolutionStrategy();
		//cma.readProperties(); // read options, see file CMAEvolutionStrategy.properties
		cma.setDimension(8); // overwrite some loaded properties
		// set best point from depth 5
		cma.setInitialX(new double[] {
				1.481980523838704, 
				1.140677320506129, 
				1.4066889502905648, 
				0.9989940865792346, 
				0.1993119083419216, 
				-0.8167288928897225, 
				0.05977267391700242, 
				0.4042137326923306});
		//cma.setInitialX(0.5); // in each dimension, also setTypicalX can be used
		cma.setInitialStandardDeviation(0.01); // also a mandatory setting
		cma.parameters.setPopulationSize(POPULATION_SIZE);
		cma.options.stopFitness = 0;       // optional setting
		cma.options.stopMaxFunEvals = POPULATION_SIZE * NUM_GENERATIONS;

		// initialize cma and get fitness array to fill in later
		double[] fitness = cma.init();  // new double[cma.parameters.getPopulationSize()];

		// initial output to files
		cma.writeToDefaultFilesHeaders(0); // 0 == overwrites old files

		// iteration loop
		while(cma.stopConditions.getNumber() == 0) {

            // --- core iteration step ---
			double[][] pop = cma.samplePopulation(); // get a new population of solutions
			for(int i = 0; i < pop.length; ++i) {    // for each candidate solution i
            	// a simple way to handle constraints that define a convex feasible domain  
            	// (like box constraints, i.e. variable boundaries) via "blind re-sampling" 
            	                                       // assumes that the feasible domain is convex, the optimum is  
				while (!fitfun.isFeasible(pop[i]))     //   not located on (or very close to) the domain boundary,  
					pop[i] = cma.resampleSingle(i);    //   initialX is feasible and initialStandardDeviations are  
                                                       //   sufficiently small to prevent quasi-infinite looping here
                // compute fitness/objective value	
				fitness[i] = fitfun.valueOf(pop[i]); // fitfun.valueOf() is to be minimized
			}
			cma.updateDistribution(fitness);         // pass fitness array to update search distribution
            // --- end core iteration step ---

			// output to files and console 
			cma.writeToDefaultFiles();
			int outmod = 5;
			if (cma.getCountIter() % (15*outmod) == 1)
				cma.printlnAnnotation(); // might write file as well
			if (cma.getCountIter() % outmod == 1) {
				cma.println();
				System.out.print("Mean X: ");
				double[] x = cma.getMeanX();
				for (int i = 0; i < x.length; i++) {
					System.out.print(x[i]);
					if (i != x.length - 1)
						System.out.print(", ");
				}
				System.out.print('\n');
				System.out.print("Best X: ");
				x = cma.getBestX();
				for (int i = 0; i < x.length; i++) {
					System.out.print(x[i]);
					if (i != x.length - 1)
						System.out.print(", ");
				}
				System.out.print('\n');
			}
		}
		// evaluate mean value as it is the best estimator for the optimum
		cma.setFitnessOfMeanX(fitfun.valueOf(cma.getMeanX())); // updates the best ever solution 

		// final output
		cma.writeToDefaultFiles(1);
		cma.println();
		cma.println("Terminated due to");
		for (String s : cma.stopConditions.getMessages())
			cma.println("  " + s);
		cma.println("best function value " + cma.getBestFunctionValue() 
				+ " at evaluation " + cma.getBestEvaluationNumber());
		
		System.out.println(cma.sb.toString());
		// we might return cma.getBestSolution() or cma.getBestX()
		System.out.println("Mean X");
		double[] x = cma.getMeanX();
		for (int i = 0; i < x.length; i++) {
			System.out.print(x[i]);
			if (i != x.length - 1)
				System.out.print(", ");
		}
		System.out.print('\n');
		
		System.out.println("Best X");
		x = cma.getBestX();
		for (int i = 0; i < x.length; i++) {
			System.out.print(x[i]);
			if (i != x.length - 1)
				System.out.print(", ");
		}
		System.out.print('\n');
	} // main  
} // class
