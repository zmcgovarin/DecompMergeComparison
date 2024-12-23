package PSO;

import java.util.Random;

import Main.Coordinate;
import Main.MiscUtil;
import Main.Position;
import func.Function;
import static Main.Main.totalDimensions;

public class PSO extends Main.Algorithm
{
    // General
    public final double maxPos;
    public final double minPos;
    private final int maxEval;
    private int countEval;
    private final Function evalFunc;
    private final Random numGen;
    private final Swarm theSwarm;
    private double overallBestFitness;

    // Util
    private final MiscUtil miscUtil;

    public PSO(Function f)
    {
        evalFunc = f;
        maxPos = f.getMax();
        minPos = f.getMin();
        countEval = 0;
        overallBestFitness = Double.MAX_VALUE;
        maxEval = totalDimensions * 3000;
        miscUtil = new MiscUtil();
        numGen = new Random();

        assert f.getDimension() == totalDimensions: "Number of DPSO dimensions does not match evaluation function dimensions";

        // Get array of randomly shuffled decision variable indices
        int[] possibleDecisionVarIndices = miscUtil.getRandomDecisionVarsArray(totalDimensions, numGen);

        theSwarm = new Swarm(this, possibleDecisionVarIndices, totalDimensions, 0, numGen);
    }

    public double start()
    {
        // Loop until max fitness evaluations are reached
        while(countEval < maxEval)
        {
            theSwarm.updateSwarmPositions();
        }
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

        return evalFunc.compute(vector);
    }

    public double getOverallBestFitness() { return overallBestFitness;}
    public void setOverallBestFitness(double fitness)
    {
        overallBestFitness = fitness;
    }
    public int getEvalBetweenDecomp() { return -1; }
    public int getCountEvalForDecomp() { return -1; }
    public int getTotalDimensionsPerSwarm() { return -1; }
    public String getName() { return "PSO"; }
}