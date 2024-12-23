package ABC;

import java.util.Random;

import Main.MiscUtil;
import Main.Position;
import Main.Coordinate;
import func.Function;
import static Main.Main.totalDimensions;

/**
 * This class implements the Cooperative ABC (CABC) algorithm.
 */
public class CABC extends Main.Algorithm
{
    // General
    public static double maxPos;
    public static double minPos;
    private final int maxEval;
    private int countEval;
    private final int limit;
    private final Function evalFunc;
    private final Random numGen;

    // Swarms
    public int totalSubswarms;
    public int totalDimensionsPerSwarm;
    public Swarm[] Swarms;
    public double overallBestFitness;

    // Util
    private final MiscUtil miscUtil;


    public CABC(Function f)
    {
        evalFunc = f;
        maxPos = f.getMax();
        minPos = f.getMin();
        countEval = 0;
        overallBestFitness = Double.MAX_VALUE;
        maxEval = totalDimensions * 3000;
        limit = totalDimensions * Swarm.NUM_FOOD_SOURCE;
        numGen = new Random();
        miscUtil = new MiscUtil();

        totalSubswarms = 4;
        totalDimensionsPerSwarm = totalDimensions / totalSubswarms;
        Swarms = new Swarm[totalSubswarms];
        int[] possibleDecisionVarIndices = miscUtil.getRandomDecisionVarsArray(totalDimensions, numGen);

        // Initialize the sub-swarms, pass subset of the randomly shuffled decision variable indices
        for(int i = 0; i < totalSubswarms; i++)
        {
            // Generate decision variable indices
            int[] decVars = new int[totalDimensionsPerSwarm];
            System.arraycopy(possibleDecisionVarIndices, totalDimensionsPerSwarm * i,
                    decVars, 0, totalDimensionsPerSwarm);

            Swarms[i] = new Swarm(this, decVars, totalDimensionsPerSwarm, i, numGen);
        }

        assert f.getDimension() == totalDimensions: "Number of ABC dimensions does not match evaluation function dimensions";

        for(Swarm s : Swarms)
        {
            s.calculateFoodSourceFitnesses();
        }
    }

    public double start()
    {
        while(countEval < maxEval)
        {
            for(Swarm s : Swarms)
                s.update();
        }
        return overallBestFitness;
    }

    public double calculateFitness(int swarmIndex, Position posToEval)
    {
        double[] vector = new double[totalDimensions];

        for(int s = 0; s < totalSubswarms; s++)
        {
            if(s == swarmIndex) // The swarm we're evaluating
            {
                for(int i = 0; i < posToEval.getCoordinates().length; i++)
                {
                    Coordinate coord = posToEval.getCoordinate(i);
                    assert vector[coord.getDimIndex()] == 0: "ERROR: Overwriting same dimension in context vector (2)";
                    vector[coord.getDimIndex()] = coord.getValue();
                }
            }
            else // Not the swarm we're evaluating, use global best position
            {
                for(int i = 0; i < Swarms[s].getNumDimensions(); i++)
                {
                    Coordinate coord = Swarms[s].getBestFoundFoodSource().getPosition().getCoordinate(i);
                    assert vector[coord.getDimIndex()] == 0: "ERROR: Overwriting same dimension in context vector (3)";
                    vector[coord.getDimIndex()] = coord.getValue();
                }
            }
        }
        return evaluateVectorFitness(vector);
    }

    private double evaluateVectorFitness(double[] vector)
    {
        countEval++;
        double eval = evalFunc.compute(vector);

        if(eval < overallBestFitness)
            overallBestFitness = eval;
        return eval;
    }

    public int getLimit() { return limit; }
    public double getOverallBestFitness() { return overallBestFitness;}
    public void setOverallBestFitness(double fitness)
    {
        overallBestFitness = fitness;
    }
    public int getEvalBetweenDecomp() { return -1; }
    public int getCountEvalForDecomp() { return -1; }
    public int getTotalDimensionsPerSwarm() { return -1; }
    public String getName() { return "CABC"; }
}
