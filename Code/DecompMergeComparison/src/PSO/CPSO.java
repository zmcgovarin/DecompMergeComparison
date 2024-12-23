package PSO;

import func.Function;

import java.util.Random;

import Main.Coordinate;
import Main.MiscUtil;
import Main.Position;
import static Main.Main.totalDimensions;

/**
 * This class implements the Cooperative Particle Swarm Optimization algorithm.
 * */
public class CPSO extends Main.Algorithm
{
    // General
    public final double maxPos;
    public final double minPos;
    private final int maxEval;
    private int countEval;
    private final Function evalFunc;
    private final Random numGen;

    // Swarms
    public int totalSubswarms;
    public int totalDimensionsPerSwarm;
    public Swarm[] Swarms;
    private double overallBestFitness;

    // Util
    private final MiscUtil miscUtil;


    public CPSO(Function f)
    {
        assert f.getDimension() == totalDimensions: "Number of dimensions does not match evaluation function dimensions";

        evalFunc = f;
        maxPos = f.getMax();
        minPos = f.getMin();
        countEval = 0;
        overallBestFitness = Double.MAX_VALUE;
        maxEval = totalDimensions * 3000;
        totalSubswarms = 4;
        totalDimensionsPerSwarm = totalDimensions / totalSubswarms;
        assert totalDimensions % totalDimensionsPerSwarm == 0: "Number of subswarms does not evenly divide dimensions";

        Swarms = new Swarm[totalSubswarms];
        numGen = new Random();
        miscUtil = new MiscUtil();

        // Get array of randomly shuffled decision variable indices
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

        // Must wait until all swarms are initialized before calculating fitness
        for(Swarm s : Swarms)
            s.calculateSwarmFitness();
    }

    public double start()
    {
        // Loop until max fitness evaluations are reached
        while(countEval < maxEval)
        {
            for(Swarm s : Swarms)
                s.updateSwarmPositions();
        }
        assert totalSubswarms == totalDimensions: "Swarms were not decomposed completely.";
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
                for(int i = 0; i < Swarms[s].numDimensions; i++)
                {
                    Coordinate coord = Swarms[s].getGlobalBestPosition().getCoordinate(i);
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
    public String getName() { return "CPSO"; }
}
