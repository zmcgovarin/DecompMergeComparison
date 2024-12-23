package DE;

import Main.Coordinate;
import Main.MiscUtil;
import Main.Position;

import func.Function;

import java.util.Random;

import static Main.Main.totalDimensions;

/**
 * This class implements the Cooperative Co-evolutionary Differential Evolution algorithm.
 */
public class CCDE extends Main.Algorithm
{
    //CC
    private final int NUM_SUBPOPULATION = 2;
    private final int DIMENSION_PER_SUB = totalDimensions / NUM_SUBPOPULATION;
    public double overallBestEval;

    //DE
    private static final int POPULATION_SIZE = 50;
    private static final double SCALE_FACTOR = 0.5; //0.5
    private static final double CROSSOVER_PROBABILITY = 0.7; //0.7
    private final double DOMAIN_MIN;
    private final double DOMAIN_MAX;
    public final double MAX_EVAL;

    //Runtime
    public DifferentialEvolution[] DE;
    public int currentGenerationNum;
    public final Function evalFunction;
    public int evalCount;
    public final MiscUtil miscUtil;
    private final Random numGen;

    public CCDE(Function f)
    {
        evalFunction = f;
        DOMAIN_MIN = evalFunction.getMin();
        DOMAIN_MAX = evalFunction.getMax();
        MAX_EVAL = totalDimensions * 3000;
        numGen = new Random();
        miscUtil = new MiscUtil();
        initializeCCDE();
    }

    public void initializeCCDE()
    {
        DE = new DifferentialEvolution[NUM_SUBPOPULATION];
        evalCount = 0;
        overallBestEval = Double.MAX_VALUE;

        // Get array of randomly shuffled decision variable indices
        int[] possibleDecisionVarIndices = miscUtil.getRandomDecisionVarsArray(totalDimensions, numGen);

        for(int i = 0; i < NUM_SUBPOPULATION; i++)
        {
            // Generate decision variable indices
            int[] decVars = new int[DIMENSION_PER_SUB];
            System.arraycopy(possibleDecisionVarIndices, DIMENSION_PER_SUB * i,
                    decVars, 0, DIMENSION_PER_SUB);

            DE[i] = new DifferentialEvolution(i, DIMENSION_PER_SUB, POPULATION_SIZE, SCALE_FACTOR,
                    CROSSOVER_PROBABILITY, DOMAIN_MIN, DOMAIN_MAX, decVars, this, numGen);
        }
        // Must wait until after all subpopulations are generated before evaluating bests,
        // as fitness evaluation needs all of them
        for(int i = 0; i < NUM_SUBPOPULATION; i++)
        {
            DE[i].evaluatePopulation();
        }
    }

    public double start()
    {
        while(evalCount < MAX_EVAL)
        {
            currentGenerationNum++;
            for(int i = 0; i < NUM_SUBPOPULATION; i++)
            {
                DE[i].doEvolution();
                if(evalCount >= MAX_EVAL)
                    break;
            }
        }
        return getGenerationEval();
    }

    public double calculateFitness(int swarmIndex, Position posToEval)
    {
        double[] vector = new double[totalDimensions];

        for(int s = 0; s < NUM_SUBPOPULATION; s++)
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
                for(int i = 0; i < DIMENSION_PER_SUB; i++)
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
        return evalFunction.compute(vector);
    }

    public double getGenerationEval()
    {
        return overallBestEval;
    }

    private void printRunInformation()
    {
        System.out.println("Cooperative Co-evolutionary Differential Evolution");
        System.out.println("Dimensions: " + totalDimensions);
        System.out.println("Dimensions per subpopulation: " + DIMENSION_PER_SUB);
        System.out.println("Crossover rate: " + CROSSOVER_PROBABILITY + " | Scale factor: " + SCALE_FACTOR);
    }

    private void printGenerationEval()
    {
        System.out.println("Generation #" + currentGenerationNum + " | Eval: " + overallBestEval);
    }

    public double getOverallBestFitness() { return overallBestEval; }
    public void setOverallBestFitness(double fitness)
    {
        overallBestEval = fitness;
    }
    public int getEvalBetweenDecomp() { return -1; }
    public int getCountEvalForDecomp() { return -1; }
    public int getTotalDimensionsPerSwarm() { return DIMENSION_PER_SUB; }
    public String getName() { return "CCDE"; }
}
