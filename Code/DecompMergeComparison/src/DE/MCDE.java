package DE;

import java.util.Random;

import Main.Coordinate;
import Main.MiscUtil;
import Main.Position;
import func.Function;

import static Main.Main.totalDimensions;

/**
 * This class implements the Merge Cooperative Co-evolutionary Differential Evolution algorithm.
 */
public class MCDE extends Main.Algorithm
{
    // DE
    private static final int POPULATION_SIZE = 50;
    private static final double SCALE_FACTOR = 0.5; //0.5
    private static final double CROSSOVER_PROBABILITY = 0.7; //0.7
    private final int MAX_EVAL;

    // Runtime
    public DifferentialEvolution[] DE;
    public int currentGenerationNum;
    public int numSubPopulation;
    public int evalPerMerge;
    public int nextMergeCount;
    public int evalCount;
    public Function evalFunction;
    private double DOMAIN_MIN;
    private double DOMAIN_MAX;

    private int totalDimensionsPerDE;
    public double overallBestEval;
    private final Random numGen;
    public final MiscUtil miscUtil;


    public MCDE(Function f)
    {
        evalFunction = f;
        MAX_EVAL = totalDimensions * 3000;
        numGen = new Random();
        miscUtil = new MiscUtil();
        initializeMCDE();
    }

    public void initializeMCDE()
    {
        numSubPopulation = totalDimensions;
        totalDimensionsPerDE = 1;
        evalPerMerge = (int) (MAX_EVAL / (1 + (Math.log(totalDimensions) / Math.log(2))));
        evalCount = 0;
        overallBestEval = Double.MAX_VALUE;
        nextMergeCount = evalPerMerge;
        DOMAIN_MIN = evalFunction.getMin();
        DOMAIN_MAX = evalFunction.getMax();

        int[] possibleDecisionVarIndices = miscUtil.getRandomDecisionVarsArray(totalDimensions, numGen);

        DE = new DifferentialEvolution[numSubPopulation];
        for(int i = 0; i < numSubPopulation; i++)
        {
            int[] decisionVariableIndexes = new int[1];
            decisionVariableIndexes[0] = possibleDecisionVarIndices[i];
            DE[i] = new DifferentialEvolution(i, 1, POPULATION_SIZE, SCALE_FACTOR, CROSSOVER_PROBABILITY,
                    DOMAIN_MIN, DOMAIN_MAX, decisionVariableIndexes, this, numGen);
        }
        // Must wait until after all subpopulations are generated before evaluating bests, as fitness evaluation needs
        // all of them
        for(int i = 0; i < numSubPopulation; i++)
        {
            DE[i].evaluatePopulation();
        }
    }

    public double start()
    {
        while(evalCount < MAX_EVAL)
        {
            currentGenerationNum++;
            for(int i = 0; i < numSubPopulation; i++)
            {
                DE[i].doEvolution();
                if(nextMergeCount <= 0)
                    break;
                if(evalCount >= MAX_EVAL)
                    break;
            }
            if(nextMergeCount <= 0)
                performMerge();
        }
        return overallBestEval;
    }

    public void performMerge()
    {
        nextMergeCount = evalPerMerge; // Reset count of fitness evaluations for the next merge

        int newDimPerDE = totalDimensionsPerDE * 2; // This will round down (double -> int)
        if(newDimPerDE > totalDimensions)
            newDimPerDE = totalDimensions;
        boolean isUniformDecomp = (totalDimensions % newDimPerDE == 0); // Will all sub-swarms be same dimension size?
        int minNewTotalDE = totalDimensions / newDimPerDE; // Could possibly +1 more new sub-swarms than this
        int newTotalDE = (isUniformDecomp)?(minNewTotalDE):(minNewTotalDE+1);
        int[] randomDimensions = miscUtil.getRandomDecisionVarsArray(totalDimensions, numGen);
        DifferentialEvolution[] newDE = new DifferentialEvolution[newTotalDE];

        for(int i = 0; i < minNewTotalDE; i++)
        {
            Coordinate[][] newChromosomes = new Coordinate[POPULATION_SIZE][newDimPerDE];
            Coordinate[] newBest = new Coordinate[newDimPerDE];

            for(int d = 0; d < newDimPerDE; d++)
            {
                int stealFromIndex = randomDimensions[(i * newDimPerDE)+d];
                int targetDEIndex = stealFromIndex / totalDimensionsPerDE; // This makes sense surely
                int targetDEDimIndex = stealFromIndex % totalDimensionsPerDE;
                DifferentialEvolution targetDE = DE[targetDEIndex];

                for(int p = 0; p < POPULATION_SIZE; p++)
                {
                    newChromosomes[p][d] = targetDE.getPosition(p).getCoordinate(targetDEDimIndex);
                }
                newBest[d] = targetDE.getBestPosition().getCoordinate(targetDEDimIndex);
            }
            newDE[i] = new DifferentialEvolution(i, newDimPerDE, POPULATION_SIZE, SCALE_FACTOR, CROSSOVER_PROBABILITY,
                    DOMAIN_MIN, DOMAIN_MAX, this, numGen, newChromosomes, newBest);
        }

        // If not uniform, we have extra dimensions, put these in a new extra swarm
        if(!isUniformDecomp)
        {
            int extraDim = totalDimensions % newDimPerDE;
            Coordinate[][] newChromosomes = new Coordinate[POPULATION_SIZE][extraDim];
            Coordinate[] newBest = new Coordinate[extraDim];

            for(int d = 0; d < extraDim; d++)
            {
                int stealFromIndex = randomDimensions[(minNewTotalDE * newDimPerDE)+d];
                int targetDEIndex = stealFromIndex / totalDimensionsPerDE; // This makes sense surely
                int targetDEDimIndex = stealFromIndex % totalDimensionsPerDE;
                DifferentialEvolution targetDE = DE[targetDEIndex];

                for(int p = 0; p < POPULATION_SIZE; p++)
                {
                    newChromosomes[p][d] = targetDE.getPosition(p).getCoordinate(targetDEDimIndex);
                }
                newBest[d] = targetDE.getBestPosition().getCoordinate(targetDEDimIndex);
            }
            newDE[newTotalDE-1] = new DifferentialEvolution(newTotalDE-1, extraDim, POPULATION_SIZE, SCALE_FACTOR,
                    CROSSOVER_PROBABILITY, DOMAIN_MIN, DOMAIN_MAX, this, numGen, newChromosomes, newBest);
        }

        DE = newDE;
        numSubPopulation = newTotalDE;
        totalDimensionsPerDE = newDimPerDE;

        // Recalculate fitness
        for(DifferentialEvolution d : DE)
            d.evaluatePopulation();
    }

    public double calculateFitness(int swarmIndex, Position posToEval)
    {
        double[] vector = new double[totalDimensions];

        for(int s = 0; s < numSubPopulation; s++)
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
                for(int i = 0; i < DE[s].getNumDimension(); i++)
                {
                    Coordinate coord = DE[s].getBestPosition().getCoordinate(i);
                    assert vector[coord.getDimIndex()] == 0: "ERROR: Overwriting same dimension in context vector (3)";
                    vector[coord.getDimIndex()] = coord.getValue();
                }
            }
        }
        return evaluateVectorFitness(vector);
    }

    public double evaluateVectorFitness(double[] vector)
    {
        evalCount++;
        nextMergeCount--;
        return evalFunction.compute(vector);
    }

    public double getOverallBestFitness() { return overallBestEval; }
    public void setOverallBestFitness(double fitness)
    {
        overallBestEval = fitness;
    }
    public int getEvalBetweenDecomp() { return -1; }
    public int getCountEvalForDecomp() { return -1; }
    public int getTotalDimensionsPerSwarm() { return totalDimensionsPerDE; }
    public String getName() { return "MCDE"; }
}
