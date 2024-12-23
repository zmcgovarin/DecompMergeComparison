package DE;

import Main.Coordinate;
import Main.MiscUtil;
import Main.Position;

import func.Function;

import java.util.Random;

import static Main.Main.totalDimensions;

public class DE extends Main.Algorithm
{
    //DE
    private final static int POPULATION_SIZE = 50;
    private final static double SCALE_FACTOR = 0.5; //0.5
    private final static double CROSSOVER_PROBABILITY = 0.7; //0.7
    private final double DOMAIN_MIN;
    private final double DOMAIN_MAX;
    public double MAX_EVAL;

    //Runtime
    public DifferentialEvolution theDE;
    public int currentGenerationNum;
    public Function evalFunction;
    public int evalCount;
    public MiscUtil miscUtil;
    private final Random numGen;
    public double overallBestEval;

    public DE(Function f)
    {
        evalFunction = f;
        DOMAIN_MIN = evalFunction.getMin();
        DOMAIN_MAX = evalFunction.getMax();
        MAX_EVAL = totalDimensions * 3000;
        numGen = new Random();
        miscUtil = new MiscUtil();
        initializeDE();
    }

    public void initializeDE()
    {
        evalCount = 0;
        overallBestEval = Double.MAX_VALUE;

        assert evalFunction.getDimension() == totalDimensions: "Number of DPSO dimensions does not match evaluation function dimensions";

        // Get array of randomly shuffled decision variable indices
        int[] possibleDecisionVarIndices = miscUtil.getRandomDecisionVarsArray(totalDimensions, numGen);

        theDE = new DifferentialEvolution(0, totalDimensions, POPULATION_SIZE, SCALE_FACTOR,
                CROSSOVER_PROBABILITY, DOMAIN_MIN, DOMAIN_MAX, possibleDecisionVarIndices, this, numGen);

        theDE.evaluatePopulation();
    }

    public double start()
    {
        while(evalCount < MAX_EVAL)
        {
            currentGenerationNum++;
            theDE.doEvolution();
        }
        return overallBestEval;
    }

    @Override
    public double calculateFitness(int swarmIndex, Position posToEval)
    {
        double[] vector = new double[totalDimensions];

        for(int i = 0; i < posToEval.getCoordinates().length; i++)
        {
            Coordinate coord = posToEval.getCoordinate(i);
            assert vector[coord.getDimIndex()] == 0: "ERROR: Overwriting same dimension in evaluation vector (1)";
            vector[coord.getDimIndex()] = coord.getValue();
        }
        evalCount++;

        return evalFunction.compute(vector);
    }

    public double getOverallBestFitness() { return overallBestEval; }
    public void setOverallBestFitness(double fitness)
    {
        overallBestEval = fitness;
    }
    public int getEvalBetweenDecomp() { return -1; }
    public int getCountEvalForDecomp() { return -1; }
    public int getTotalDimensionsPerSwarm() { return -1; }
    public String getName() { return "DE"; }
}
