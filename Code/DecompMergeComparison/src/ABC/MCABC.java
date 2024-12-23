package ABC;

import java.util.Random;

import Main.MiscUtil;
import func.Function;

import static Main.Main.totalDimensions;

import Main.Position;
import Main.Coordinate;

public class MCABC extends Main.Algorithm
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

    // Decomposition
    public int evalBetweenDecomp;
    public int countEvalForDecomp;

    // Util
    private final MiscUtil miscUtil;


    public MCABC(Function f)
    {
        evalFunc = f;
        maxPos = f.getMax();
        minPos = f.getMin();
        countEval = 0;
        overallBestFitness = Double.MAX_VALUE;
        maxEval = totalDimensions * 3000;
        limit = totalDimensions * Swarm.NUM_FOOD_SOURCE;
        evalBetweenDecomp = (int) (maxEval / (1 + (Math.log(totalDimensions) / Math.log(2))));
        numGen = new Random();
        miscUtil = new MiscUtil();

        totalSubswarms = totalDimensions;
        totalDimensionsPerSwarm = 1;
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
        countEvalForDecomp = 0;

        while(countEval < maxEval)
        {
            for(Swarm s : Swarms)
                s.update();

            // Perform decomposition
            if(countEvalForDecomp >= evalBetweenDecomp)
                performMerging();
        }
        assert totalSubswarms == 1: "Swarms were not merged completely.";
        return overallBestFitness;
    }

    private void performMerging()
    {
        countEvalForDecomp = 0; // Reset count of fitness evaluations for the next decomposition
        int newDimPerSubswarm = totalDimensionsPerSwarm * 2;
        if(newDimPerSubswarm > totalDimensions)
            newDimPerSubswarm = totalDimensions;
        boolean isUniformDecomp = (totalDimensions % newDimPerSubswarm == 0); // Will all sub-swarms be same dimension size?
        int minNewTotalSubswarm = totalDimensions / newDimPerSubswarm; // Could possibly +1 more new sub-swarms than this
        int newTotalSubswarm = (isUniformDecomp)?(minNewTotalSubswarm):(minNewTotalSubswarm+1);
        int[] randomDimensions = miscUtil.getRandomDecisionVarsArray(totalDimensions, numGen);
        Swarm[] newSwarms = new Swarm[newTotalSubswarm];

        // Let's initialize the new sub-swarms first, then copy over all the info
        for(int i = 0; i < minNewTotalSubswarm; i++)
        {
            Coordinate[][] foodSourceCoords = new Coordinate[Swarm.NUM_FOOD_SOURCE][newDimPerSubswarm];
            Coordinate[] swarmBestFoodCoords = new Coordinate[newDimPerSubswarm];

            for(int d = 0; d < newDimPerSubswarm; d++)
            {
                int stealFromIndex = randomDimensions[(i * newDimPerSubswarm)+d];
                int swarmIndex = stealFromIndex / totalDimensionsPerSwarm; // This makes sense surely
                int swarmDimIndex = stealFromIndex % totalDimensionsPerSwarm;
                Swarm targetSwarm = Swarms[swarmIndex];

                for(int p = 0; p < Swarm.NUM_FOOD_SOURCE; p++)
                {
                    foodSourceCoords[p][d] = targetSwarm.getFoodSource(p).getPosition().getCoordinate(swarmDimIndex);
                }
                swarmBestFoodCoords[d] = targetSwarm.getBestFoundFoodSource().getPosition().getCoordinate(swarmDimIndex);
            }
            newSwarms[i] = new Swarm(this, foodSourceCoords, swarmBestFoodCoords, newDimPerSubswarm, i, numGen);
        }
        // If not uniform, we have extra dimensions, put these in a new extra swarm
        if(!isUniformDecomp)
        {
            int extraDim = totalDimensions % newDimPerSubswarm;
            Coordinate[][] foodSourceCoords = new Coordinate[Swarm.NUM_FOOD_SOURCE][extraDim];
            Coordinate[] swarmBestFoodCoords = new Coordinate[extraDim];

            for(int d = 0; d < extraDim; d++)
            {
                int stealFromIndex = randomDimensions[(minNewTotalSubswarm * newDimPerSubswarm)+d];
                int swarmIndex = stealFromIndex / totalDimensionsPerSwarm; // This makes sense surely
                int swarmDimIndex = stealFromIndex % totalDimensionsPerSwarm;
                Swarm targetSwarm = Swarms[swarmIndex];

                for(int p = 0; p < Swarm.NUM_FOOD_SOURCE; p++)
                {
                    foodSourceCoords[p][d] = targetSwarm.getFoodSource(p).getPosition().getCoordinate(swarmDimIndex);
                }
                swarmBestFoodCoords[d] = targetSwarm.getBestFoundFoodSource().getPosition().getCoordinate(swarmDimIndex);
            }
            newSwarms[newTotalSubswarm-1] = new Swarm(this, foodSourceCoords, swarmBestFoodCoords, extraDim,
                    newTotalSubswarm-1, numGen);
        }
        Swarms = newSwarms;
        totalSubswarms = newTotalSubswarm;
        totalDimensionsPerSwarm = newDimPerSubswarm;

        for(Swarm s : Swarms)

            s.calculateFoodSourceFitnesses();
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
        countEvalForDecomp++;
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
    public String getName() { return "MCABC"; }
}
