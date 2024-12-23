package Main;

import func.Function;


import java.util.Random;

import static Main.Main.totalDimensions;

public class RandomSearch extends Algorithm
{
    private double overallBestFitness;
    private double[] bestPosition;
    private double MAX_EVAL;
    private int countEval;
    private Function evalFunc;
    private Random numGen;

    public RandomSearch(Function f)
    {
        evalFunc = f;
        overallBestFitness = Double.MAX_VALUE;
        MAX_EVAL = Main.totalDimensions * 3000;
        numGen = new Random();
        bestPosition = new double[Main.totalDimensions];
    }

    public double start()
    {
        double[] randomPosition = new double[Main.totalDimensions];
        double max = evalFunc.getMax();
        double min = evalFunc.getMin();

        while(countEval < MAX_EVAL)
        {
            for(int d = 0; d < Main.totalDimensions; d++)
            {
                randomPosition[d] = (numGen.nextDouble() * (max - min)) + min;
            }
            double eval = evalFunc.compute(randomPosition);

            if(eval < overallBestFitness)
            {
                System.arraycopy(randomPosition, 0, bestPosition, 0, Main.totalDimensions);
                overallBestFitness = eval;
            }
            countEval++;
        }
        return overallBestFitness;
    }

    public double calculateFitness(int swarmIndex, Position posToEval)
    {
        return 0.0;
    }

    public double getOverallBestFitness() { return overallBestFitness;}
    public void setOverallBestFitness(double fitness)
    {
        overallBestFitness = fitness;
    }
    public int getEvalBetweenDecomp() { return -1; }
    public int getCountEvalForDecomp() { return -1; }
    public int getTotalDimensionsPerSwarm() { return -1; }
    public String getName() { return "RandomSearch"; }
}
