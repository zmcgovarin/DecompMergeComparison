package PSO;


import java.util.Random;

import Main.Coordinate;
import Main.MiscUtil;
import Main.Position;
import func.Function;
import static Main.Main.*;


public class DCPSO extends Main.Algorithm
{
    // General
    public final double maxPos;
    public final double minPos;
    private final int maxEval;
    private int countEval;
    private final Function evalFunc;
    private final Random numGen;
    private final MiscUtil miscUtil;

    // Swarms
    public int totalSubswarms;
    public int totalDimensionsPerSwarm;
    public Swarm[] Swarms;
    private double overallBestFitness;

    // Decomposition
    public int evalBetweenDecomp;
    public int countEvalForDecomp;

    public DCPSO(Function f)
    {
        evalFunc = f;
        maxPos = f.getMax();
        minPos = f.getMin();
        countEval = 0;
        overallBestFitness = Double.MAX_VALUE;
        maxEval = totalDimensions * 3000;
        totalSubswarms = 1; // Always 1 initially for DCPSO
        totalDimensionsPerSwarm = totalDimensions / totalSubswarms;
        evalBetweenDecomp = (int) (maxEval / (0 + (Math.log(totalDimensions) / Math.log(2))));
        Swarms = new Swarm[totalSubswarms];
        numGen = new Random();
        miscUtil = new MiscUtil();

        assert f.getDimension() == totalDimensions: "Number of DPSO dimensions does not match evaluation function dimensions";

        // Get array of randomly shuffled decision variable indices
        int[] possibleDecisionVarIndices = getRandomDecisionVarsArray();

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

    /**
     * This method starts the CPSO, having it begin searching for the best solution to the problem.
     */
    public double start()
    {
        countEvalForDecomp = 0;

        // Loop until max fitness evaluations are reached
        while(countEval < maxEval)
        {
            //System.out.println("Generation: " + generation + " | Best eval: " + getOverallBestEval());
            for(Swarm s : Swarms)
                s.updateSwarmPositions();

            // Perform decomposition
            if(countEvalForDecomp >= evalBetweenDecomp)
                performDecomposition();
        }
        assert totalSubswarms == totalDimensions: "Swarms were not decomposed completely.";
        return overallBestFitness;
    }

    private void performDecomposition()
    {
        countEvalForDecomp = 0; // Reset count of fitness evaluations for the next decomposition
        int newDimPerSubswarm = totalDimensionsPerSwarm / 2; // This will round down (double -> int)
        boolean isUniformDecomp = (totalDimensions % newDimPerSubswarm == 0); // Will all sub-swarms be same dimension size?
        int minNewTotalSubswarm = totalDimensions / newDimPerSubswarm; // Could possibly +1 more new sub-swarms than this
        int newTotalSubswarm = (isUniformDecomp)?(minNewTotalSubswarm):(minNewTotalSubswarm+1);
        int[] randomDimensions = getRandomDecisionVarsArray();
        Swarm[] newSwarms = new Swarm[newTotalSubswarm];

        // Let's initialize the new sub-swarms first, then copy over all the info
        for(int i = 0; i < minNewTotalSubswarm; i++)
        {
            Coordinate[][] posCoords = new Coordinate[Swarm.numParticles][newDimPerSubswarm];
            Coordinate[][] velocityCoords = new Coordinate[Swarm.numParticles][newDimPerSubswarm];
            Coordinate[][] particleBestCoords = new Coordinate[Swarm.numParticles][newDimPerSubswarm];
            Coordinate[] swarmBestCoords = new Coordinate[newDimPerSubswarm];

            for(int d = 0; d < newDimPerSubswarm; d++)
            {
                int stealFromIndex = randomDimensions[(i * newDimPerSubswarm)+d];
                int swarmIndex = stealFromIndex / totalDimensionsPerSwarm; // This makes sense surely
                int swarmDimIndex = stealFromIndex % totalDimensionsPerSwarm;
                Swarm targetSwarm = Swarms[swarmIndex];

                for(int p = 0; p < Swarm.numParticles; p++)
                {
                    posCoords[p][d] = targetSwarm.getParticle(p).getPosition().getCoordinate(swarmDimIndex);
                    velocityCoords[p][d] = targetSwarm.getParticle(p).getVelocity().getCoordinate(swarmDimIndex);
                    particleBestCoords[p][d] = targetSwarm.getParticle(p).getPersonalBest().getCoordinate(swarmDimIndex);
                }
                swarmBestCoords[d] = targetSwarm.getGlobalBestPosition().getCoordinate(swarmDimIndex);
            }
            newSwarms[i] = new Swarm(this, posCoords, velocityCoords, particleBestCoords,
                    swarmBestCoords, newDimPerSubswarm, i, numGen);
        }

        // If not uniform, we have extra dimensions, put these in a new extra swarm
        if(!isUniformDecomp)
        {
            int extraDim = totalDimensions % newDimPerSubswarm;
            Coordinate[][] posCoords = new Coordinate[Swarm.numParticles][extraDim];
            Coordinate[][] velocityCoords = new Coordinate[Swarm.numParticles][extraDim];
            Coordinate[][] particleBestCoords = new Coordinate[Swarm.numParticles][extraDim];
            Coordinate[] swarmBestCoords = new Coordinate[extraDim];

            for(int d = 0; d < extraDim; d++)
            {
                int stealFromIndex = randomDimensions[(minNewTotalSubswarm * newDimPerSubswarm)+d];
                int swarmIndex = stealFromIndex / totalDimensionsPerSwarm; // This makes sense surely
                int swarmDimIndex = stealFromIndex % totalDimensionsPerSwarm;
                Swarm targetSwarm = Swarms[swarmIndex];

                for(int p = 0; p < Swarm.numParticles; p++)
                {
                    posCoords[p][d] = targetSwarm.getParticle(p).getPosition().getCoordinate(swarmDimIndex);
                    velocityCoords[p][d] = targetSwarm.getParticle(p).getVelocity().getCoordinate(swarmDimIndex);
                    particleBestCoords[p][d] = targetSwarm.getParticle(p).getPersonalBest().getCoordinate(swarmDimIndex);
                }
                swarmBestCoords[d] = targetSwarm.getGlobalBestPosition().getCoordinate(swarmDimIndex);
            }
            newSwarms[newTotalSubswarm-1] = new Swarm(this, posCoords, velocityCoords, particleBestCoords,
                    swarmBestCoords, extraDim, newTotalSubswarm-1, numGen);
        }
        Swarms = newSwarms;
        totalSubswarms = newTotalSubswarm;
        totalDimensionsPerSwarm = newDimPerSubswarm;

        for(Swarm s : Swarms)
            s.calculateSwarmFitness();
    }

    public int[] getRandomDecisionVarsArray()
    {
        int[] temp = new int[totalDimensions];
        for (int i = 0; i < totalDimensions; ++i)
            temp[i] = i;
        miscUtil.shuffleArray(temp, numGen);
        return temp;
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
        countEvalForDecomp++;
        return evalFunc.compute(vector);
    }

    public double getOverallBestFitness() { return overallBestFitness;}
    public void setOverallBestFitness(double fitness)
    {
        overallBestFitness = fitness;
    }
    public int getEvalBetweenDecomp() { return evalBetweenDecomp; }
    public int getCountEvalForDecomp() { return countEvalForDecomp; }
    public int getTotalDimensionsPerSwarm() { return totalDimensionsPerSwarm; }
    public String getName() { return "DCPSO"; }
}
