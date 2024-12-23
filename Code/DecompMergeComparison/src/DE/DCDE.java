package DE;

import java.util.Random;

import Main.Coordinate;
import Main.MiscUtil;
import Main.Position;
import func.Function;
import static Main.Main.totalDimensions;

/**
 * This class handles the Decomposition Cooperative Co-evolutionary algorithm
 */
public class DCDE extends Main.Algorithm
{
    //DE
    private static final int POPULATION_SIZE = 50;
    private static final double SCALE_FACTOR = 0.5; //0.5
    private static final double CROSSOVER_PROBABILITY = 0.7; //0.7
    private final double DOMAIN_MIN;
    private final double DOMAIN_MAX;
    private final int MAX_EVAL;

    //Runtime
    public DifferentialEvolution[] DE;
    public int currentGenerationNum;
    public int numSubPopulation;
    public int evalPerDecomp;
    public int nextDecompCount;
    public Function evalFunction;
    public int evalCount;
    private int totalDimensionsPerDE;
    private final Random numGen;
    public double overallBestEval;

    // Util
    public MiscUtil miscUtil;

    public DCDE(Function f)
    {
        evalFunction = f;
        DOMAIN_MIN = evalFunction.getMin();
        DOMAIN_MAX = evalFunction.getMax();
        MAX_EVAL = totalDimensions * 3000;
        numGen = new Random();
        miscUtil = new MiscUtil();
        initializeDCDE();
    }

    public void initializeDCDE()
    {
        assert evalFunction.getDimension() == totalDimensions:
                "Number of DPSO dimensions does not match evaluation function dimensions";

        numSubPopulation = 1;
        totalDimensionsPerDE = totalDimensions;
        evalPerDecomp = (int) (MAX_EVAL / (0 + (Math.log(totalDimensions) / Math.log(2))));
        evalCount = 0;
        overallBestEval = Double.MAX_VALUE;
        nextDecompCount = evalPerDecomp;

        DE = new DifferentialEvolution[1];
        int[] possibleDecisionVarIndices = miscUtil.getRandomDecisionVarsArray(totalDimensions, numGen);

        DE[0] = new DifferentialEvolution(0, totalDimensions, POPULATION_SIZE, SCALE_FACTOR,
                CROSSOVER_PROBABILITY, DOMAIN_MIN, DOMAIN_MAX, possibleDecisionVarIndices, this, numGen);
        DE[0].evaluatePopulation();
    }

    public double start()
    {
        while(true)
        {
            currentGenerationNum++;
            for(int i = 0; i < numSubPopulation; i++)
            {
                DE[i].doEvolution();
                if(nextDecompCount <= 0)
                    break;
                if(evalCount >= MAX_EVAL)
                    break;
            }
            if(evalCount >= MAX_EVAL)
                break;
            if(nextDecompCount <= 0)
                performDecomposition();
            //printGenerationEval();
        }
        assert numSubPopulation == totalDimensions: "DEs were not decomposed completely.";
        return overallBestEval;
    }

    public void performDecomposition()
    {
        nextDecompCount = evalPerDecomp;  // Reset count of fitness evaluations for the next merge

        int newDimPerDE = totalDimensionsPerDE / 2; // This will round down (double -> int)
        boolean isUniformDecomp = (totalDimensions % newDimPerDE == 0); // Will all sub-swarms be same dimension size?
        int minNewTotalDE = totalDimensions / newDimPerDE; // Could possibly +1 more new sub-swarms than this
        int newTotalDE = (isUniformDecomp)?(minNewTotalDE):(minNewTotalDE+1);
        int[] randomDimensions = miscUtil.getRandomDecisionVarsArray(totalDimensions, numGen);
        DifferentialEvolution[] newDE = new DifferentialEvolution[newTotalDE];

        for(int i = 0; i < minNewTotalDE; i++)
        {
            Coordinate[][] newChromosomes = new Coordinate[POPULATION_SIZE][newDimPerDE];
            Coordinate[] newBestVector = new Coordinate[newDimPerDE];

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
                newBestVector[d] = targetDE.getBestPosition().getCoordinate(targetDEDimIndex);
            }
            newDE[i] = new DifferentialEvolution(i, newDimPerDE, POPULATION_SIZE, SCALE_FACTOR, CROSSOVER_PROBABILITY,
                    DOMAIN_MIN, DOMAIN_MAX, this, numGen, newChromosomes, newBestVector);
        }
        // If not uniform, we have extra dimensions, put these in a new extra swarm
        if(!isUniformDecomp)
        {
            int extraDim = totalDimensions % newDimPerDE;
            Coordinate[][] newChromosomes = new Coordinate[POPULATION_SIZE][extraDim];
            Coordinate[] newBestVector = new Coordinate[extraDim];

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
                newBestVector[d] = targetDE.getBestPosition().getCoordinate(targetDEDimIndex);
            }
            newDE[newTotalDE-1] = new DifferentialEvolution(newTotalDE-1, extraDim, POPULATION_SIZE, SCALE_FACTOR,
                    CROSSOVER_PROBABILITY, DOMAIN_MIN, DOMAIN_MAX, this, numGen, newChromosomes, newBestVector);
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
        nextDecompCount--;
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
    public String getName() { return "DCDE"; }
}
