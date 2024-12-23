package DE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import Main.Coordinate;
import Main.Algorithm;
import Main.Position;

public class DifferentialEvolution
{
    //DE
    private final int numDimension;
    private final int POPULATION_SIZE;
    private final double SCALE_FACTOR;
    private final double CROSSOVER_PROBABILITY;
    private final double DOMAIN_MIN;
    private final double DOMAIN_MAX;

    //Runtime variables
    private Position[] currentPopulation;
    private int[] decisionVariablesIndexes;
    private Position bestPosition;
    private final int idNum;
    private final Algorithm parent;
    private final Random numGen;


    public DifferentialEvolution(int idNum, int numDimensions, int populationSize, double scaleFactor,
                                 double crossoverProbability, double domainMin, double domainMax,
                                 int[] decisionVariableIndexes, Algorithm parent, Random numGen)
    {
        numDimension = numDimensions;
        POPULATION_SIZE = populationSize;
        SCALE_FACTOR = scaleFactor;
        CROSSOVER_PROBABILITY = crossoverProbability;
        DOMAIN_MIN = domainMin;
        DOMAIN_MAX = domainMax;
        this.idNum = idNum;
        this.decisionVariablesIndexes = decisionVariableIndexes;
        this.parent = parent;
        currentPopulation = new Position[populationSize];
        this.numGen = numGen;

        initializePopulation(decisionVariableIndexes);
    }

    public DifferentialEvolution(int idNum, int numDimensions, int populationSize, double scaleFactor,
                                 double crossoverProbability, double domainMin, double domainMax,
                                 Algorithm parent, Random numGen,
                                 Coordinate[][] chromosomes, Coordinate[] bestChromosome)
    {
        numDimension = numDimensions;
        POPULATION_SIZE = populationSize;
        SCALE_FACTOR = scaleFactor;
        CROSSOVER_PROBABILITY = crossoverProbability;
        DOMAIN_MIN = domainMin;
        DOMAIN_MAX = domainMax;
        this.idNum = idNum;
        this.parent = parent;
        this.numGen = numGen;
        currentPopulation = new Position[populationSize];

        assert POPULATION_SIZE == chromosomes.length: "Population size != # of passed chromosomes";
        assert numDimension == chromosomes[0].length: "Number of dimensions != passed chromosome length";
        assert numDimension == bestChromosome.length: "Number of dimensions != number of decision variable indices";

        for(int p = 0; p < POPULATION_SIZE; p++)
        {
            currentPopulation[p] = new Position(chromosomes[p]);
        }

        bestPosition = new Position(bestChromosome);
    }

    public void setPopulation(Position[] population)
    {
        currentPopulation = population;
    }
    public void setBest(Position best) { bestPosition = best; }

    public void doEvolution()
    {
        for(int parent = 0; parent < POPULATION_SIZE; parent++)
        {
            //Mutation
            Position trialVector = getTrialVector(parent);

            //Crossover
            Position offspring = getOffspring(parent, trialVector);

            //Decide if parent or offspring survives
            double parentEval = evaluatePosition(currentPopulation[parent]);
            double offspringEval = evaluatePosition(offspring);

            if(offspringEval < parentEval) //if offspring is better than parent replace it, otherwise keep parent
                currentPopulation[parent] = offspring;

            if(offspringEval < this.parent.getOverallBestFitness()) //if offspring is better than population's best, replace it
            {
                this.parent.setOverallBestFitness(offspringEval);
                bestPosition = offspring.clone();
            }

        }
        //System.out.println("Subpopulation: " + idNum + " | Best evaluation: " + bestEvaluation + " (Vector #" + bestIndex + ")");
    }

    private void initializePopulation(int[] decisionVars)
    {
        assert numDimension == decisionVars.length: "Number of dimensions != number of decision variable indices";

        for(int i = 0; i < POPULATION_SIZE; i++)
        {
            currentPopulation[i] = new Position(decisionVars, numDimension);
            currentPopulation[i].randomize(numGen);
        }

        // Must be initialized to calculate fitness via context vector
        setBest(currentPopulation[0].clone());
    }

    public void evaluatePopulation()
    {
        for(int i = 0; i < POPULATION_SIZE; i++)
        {
            double eval = parent.calculateFitness(idNum, currentPopulation[i]);
            if(eval < parent.getOverallBestFitness())
            {
                bestPosition = currentPopulation[i].clone();
                parent.setOverallBestFitness(eval);
            }
        }
    }

    private Position getOffspring(int parentIndex, Position trialVector)
    {
        Position offspringVector = currentPopulation[parentIndex].clone();

        int randomIndex = numGen.nextInt(numDimension);

        //Select 1 random crossover point, ensures never empty set
        offspringVector.getCoordinate(randomIndex).setValue(trialVector.getCoordinate(randomIndex).getValue());

        for(int dimension = 0; dimension < numDimension; dimension++)
        {
            if(numGen.nextDouble() < CROSSOVER_PROBABILITY)
                offspringVector.getCoordinate(dimension).setValue(trialVector.getCoordinate(dimension).getValue());
        }

        return offspringVector;
    }

    private Position getTrialVector(int parentIndex)
    {
        Position trialVector = currentPopulation[parentIndex].clone();
        ArrayList<Position> availablePopulation = new ArrayList<Position>(Arrays.asList(currentPopulation));
        availablePopulation.remove(parentIndex);

        //Random select target vector
        int targetVectorIndex = numGen.nextInt(POPULATION_SIZE-1); //-1 because we have removed the parent vector already
        Position targetVector = availablePopulation.get(targetVectorIndex);
        availablePopulation.remove(targetVector);

        //Randomly select two solution vectors
        int randomVectorIndex = numGen.nextInt(POPULATION_SIZE-2); //-2 because we have now also removed the target vector
        Position firstRandomVector = availablePopulation.get(randomVectorIndex);
        availablePopulation.remove(randomVectorIndex);

        randomVectorIndex = numGen.nextInt(POPULATION_SIZE-3);
        Position secondRandomVector = availablePopulation.get(randomVectorIndex);
        availablePopulation.remove(randomVectorIndex);

        //Perform differential on each dimension
        for(int dimension = 0; dimension < numDimension; dimension++)
        {
            double newVal = targetVector.getCoordinate(dimension).getValue() + (SCALE_FACTOR *
                    (firstRandomVector.getCoordinate(dimension).getValue() -
                            secondRandomVector.getCoordinate(dimension).getValue()));

            //Ensure trial vector does not exceed bounds of evaluation function
            if(newVal > DOMAIN_MAX)
                newVal = DOMAIN_MAX;
            else if(newVal < DOMAIN_MIN)
                newVal = DOMAIN_MIN;

            trialVector.getCoordinate(dimension).setValue(newVal);
        }
        return trialVector;
    }

    private double evaluatePosition(Position pos)
    {
        return parent.calculateFitness(idNum, pos);
    }

    public Position getBestPosition() { return bestPosition; }
    public Position getPosition(int index) { return currentPopulation[index]; }
    public int getNumDimension() { return numDimension; }
    public int[] getDecisionVariablesIndexes() { return decisionVariablesIndexes; }
    public Position[] getPopulation() { return currentPopulation; }
}

