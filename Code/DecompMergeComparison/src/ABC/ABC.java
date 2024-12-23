/**
 * This package implements several Artificial Bee Colony (ABC) [1] [2] algorithms:
 * the vanilla ABC, Cooperative ABC, Decomposition ABC, and Merging ABC.
 *
 * 1. Karaboga, D. (2005). An idea based on honey bee swarm for numerical optimization (Vol. 200, pp. 1-10).
 * Technical report-tr06, Erciyes university, engineering faculty, computer engineering department.
 * 2. Karaboga, Dervis, and Bahriye Akay. "A comparative study of artificial bee colony algorithm."
 * Applied mathematics and computation 214.1 (2009): 108-132.
 */

package ABC;

import java.util.Random;

import Main.Coordinate;
import Main.MiscUtil;
import Main.Position;
import func.Function;
import static Main.Main.totalDimensions;

/**
 * This class implements the vanilla ABC algorithm.
 */
public class ABC extends Main.Algorithm
{
    // General
    public static double maxPos;
    public static double minPos;
    private final int maxEval;
    private int countEval;
    private final int limit;
    private final Function evalFunc;
    private final Random numGen;

    // Swarm
    public Swarm theSwarm;
    public double overallBestFitness;

    // Util
    Main.MiscUtil miscUtil;


    public ABC(Function f)
    {
        assert f.getDimension() == totalDimensions: "Number of ABC dimensions does not match evaluation function dimensions";

        evalFunc = f;
        maxPos = f.getMax();
        minPos = f.getMin();
        countEval = 0;
        overallBestFitness = Double.MAX_VALUE;
        maxEval = totalDimensions * 3000;
        limit = totalDimensions * Swarm.NUM_FOOD_SOURCE; // Taken from [2]
        numGen = new Random();
        miscUtil = new MiscUtil();

        int[] possibleDecisionVarIndices = miscUtil.getRandomDecisionVarsArray(totalDimensions, numGen);

        theSwarm = new Swarm(this, possibleDecisionVarIndices, totalDimensions, 0, numGen);
        theSwarm.calculateFoodSourceFitnesses();
    }

    public double start()
    {
        while(countEval < maxEval)
        {
            theSwarm.update();
        }
        assert overallBestFitness == theSwarm.getBestFoundFoodSource().getFitness(): "Best fit does not match";
        return overallBestFitness;
    }

    public double calculateFitness(int swarmIndex, Position posToEval)
    {
        double[] vector = new double[totalDimensions];

        for(int i = 0; i < posToEval.getCoordinates().length; i++)
        {
            Coordinate coord = posToEval.getCoordinate(i);
            assert vector[coord.getDimIndex()] == 0: "ERROR: Overwriting same dimension in evaluation vector (1)";
            vector[coord.getDimIndex()] = coord.getValue();
        }
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
    public String getName() { return "ABC"; }
}
