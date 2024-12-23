package ABC;

import java.util.Random;

import Main.Position;
import Main.Algorithm;
import Main.Coordinate;
import static Main.Main.getMaxPos;
import static Main.Main.getMinPos;

public class Swarm
{
    // Food Sources
    private final FoodSource[] foodSources; // Food source objects never change, though their positions may be reset
    private FoodSource bestFoundFoodSource;
    public final static int NUM_FOOD_SOURCE = 10;

    // Misc.
    private final Algorithm parent;
    private final int numDimensions;
    private final int index;
    private final Random numGen;

    // Number of Bees
    private final int NUM_EMPLOYED_BEE = 10; // == num food source
    private final int NUM_ONLOOKER_BEE = 10; // == num food source
    private final int NUM_SCOUT_BEE = 1; // ~5-10% of num food source


    public Swarm(Algorithm parent, int[] decisionVars, int numDimensions, int index, Random numGen)
    {
        assert numDimensions == decisionVars.length: "Number of dimensions != number of decision variable indices";
        assert NUM_FOOD_SOURCE == NUM_EMPLOYED_BEE: "Number of food sources must = number of employed bees";
        assert NUM_FOOD_SOURCE == NUM_ONLOOKER_BEE: "Number of food sources must = number of onlooker bees";

        this.parent = parent;
        this.numDimensions = numDimensions;
        this.index = index;
        this.numGen = numGen;
        foodSources = new FoodSource[NUM_FOOD_SOURCE];

        initializeFoodSources(decisionVars);
    }

    public Swarm(Algorithm parent, Coordinate[][] foodSourceCoords, Coordinate[] swarmBestFoodCoords, int numDimensions,
                 int index, Random numGen)
    {
        assert foodSourceCoords.length == NUM_FOOD_SOURCE: "Number of food sources != NUM_FOOD_SOURCE";
        assert foodSourceCoords[0].length == numDimensions: "Food source dims != numDimensions";

        this.parent = parent;
        this.numDimensions = numDimensions;
        this.index = index;
        this.numGen = numGen;
        foodSources = new FoodSource[NUM_FOOD_SOURCE];
        bestFoundFoodSource = new FoodSource(this, new Position(swarmBestFoodCoords));

        for(int i = 0; i < NUM_FOOD_SOURCE; i++)
        {
            foodSources[i] = new FoodSource(this, new Position(foodSourceCoords[i]));
        }
    }

    public void update()
    {
        // Employee bee stuff
        for(int i = 0; i < NUM_EMPLOYED_BEE; i++)
        {
            FoodSource candidate = produceCandidateFoodSource(i);
            if(candidate.getFitness() < foodSources[i].getFitness()) // If Candidate is better, replace
            {
                foodSources[i] = candidate;
                if(candidate.getFitness() < bestFoundFoodSource.getFitness())
                    bestFoundFoodSource = candidate.clone();
            }
            else
                foodSources[i].incrementTrialCount();
        }

        // Onlooker bee stuff, probabilities
        double[] probabilities = calculateFoodSourceProbabilities();
        for(int i = 0; i < NUM_ONLOOKER_BEE; i++)
        {
            int randomFoodSourceIndex = getRandomFoodSourceViaProbability(probabilities);
            FoodSource candidate = produceCandidateFoodSource(randomFoodSourceIndex);
            if(candidate.getFitness() < foodSources[randomFoodSourceIndex].getFitness()) // If Candidate is better
            {
                foodSources[randomFoodSourceIndex] = candidate;
                if(candidate.getFitness() < bestFoundFoodSource.getFitness())
                    bestFoundFoodSource = candidate.clone();
            }
        }

        // Scout bee stuff
        for(int i = 0; i < NUM_SCOUT_BEE; i++)
        {
            int worstFoodSourceIndex = -1;
            int worstFoodSourceCount = -1;
            for(int j = 0; j < NUM_FOOD_SOURCE; j++)
            {
                if(foodSources[j].getTrialCount() > worstFoodSourceCount)
                {
                    worstFoodSourceCount = foodSources[j].getTrialCount();
                    worstFoodSourceIndex = j;
                }
            }
            if(worstFoodSourceCount > parent.getLimit())
            {
                foodSources[worstFoodSourceIndex].randomizePosition(numGen);
                foodSources[worstFoodSourceIndex].resetTrialCount();
                foodSources[worstFoodSourceIndex].calculateFitness();
                if(foodSources[worstFoodSourceIndex].getFitness() < bestFoundFoodSource.getFitness())
                    bestFoundFoodSource = foodSources[worstFoodSourceIndex].clone();
            }
        }
    }

    public void initializeFoodSources(int[] decisionVars)
    {
        for(int i = 0; i < NUM_FOOD_SOURCE; i++)
            foodSources[i] = new FoodSource(this, numDimensions, decisionVars, numGen);
        bestFoundFoodSource = foodSources[0];

    }

    public void calculateFoodSourceFitnesses()
    {
        // We must first initialize all the food source before evaluating them, necessary for CC framework
        for(int i = 0; i < NUM_FOOD_SOURCE; i++)
        {
            foodSources[i].calculateFitness();
            if(foodSources[i].getFitness() < bestFoundFoodSource.getFitness())
                bestFoundFoodSource = foodSources[i].clone();
            if(foodSources[i].getFitness() < parent.getOverallBestFitness())
                parent.setOverallBestFitness(foodSources[i].getFitness());
        }
    }

    public int getRandomFoodSourceViaProbability(double[] probabilities)
    {
        double randomDouble = numGen.nextDouble();
        for(int i = 0; i < probabilities.length; i++)
        {
            if(randomDouble < probabilities[i])
                return i;
        }
        return probabilities.length - 1;
        // Maybe some rounding errors cause last probability sum to be ~0.99, we could possibly generate a random value
        // above ~0.99, so we return last index just to be safe
    }

    // Equation #6 [2]
    public double[] calculateFoodSourceProbabilities()
    {
        double[] probabilities = new double[NUM_FOOD_SOURCE];
        double totalFitnessSum = 0.0;

        for(int i = 0; i < NUM_FOOD_SOURCE; i++)
        {
            probabilities[i] = 1 / (1 + foodSources[i].getFitness());
            totalFitnessSum += probabilities[i];
        }

        for(int i = 0; i < NUM_FOOD_SOURCE; i++)
        {
            probabilities[i] /= totalFitnessSum;
            if(i != 0)
                probabilities[i] += probabilities[i-1];
        }
        return probabilities;
    }

    // Equation #7 [2]
    public FoodSource produceCandidateFoodSource(int currentFoodSourceIndex)
    {
        Position result = foodSources[currentFoodSourceIndex].getPosition().clone();
        double phi = (numGen.nextDouble() * 2) - 1; // (-1, 1)
        int randomDimension = numGen.nextInt(numDimensions);
        int randomNeighbourIndex = numGen.nextInt(NUM_FOOD_SOURCE);

        // Ensure i != j
        while(randomNeighbourIndex == currentFoodSourceIndex)
            randomNeighbourIndex = numGen.nextInt(NUM_FOOD_SOURCE);

        double coordinateToModify = result.getCoordinate(randomDimension).getValue();
        double randomNeighbourCoord = foodSources[randomNeighbourIndex].getPosition().getCoordinate(randomDimension).getValue();
        double newCoordinate = coordinateToModify + phi*(coordinateToModify - randomNeighbourCoord);

        if(newCoordinate > getMaxPos())
            newCoordinate = getMaxPos();
        if(newCoordinate < getMinPos())
            newCoordinate = getMinPos();

        result.getCoordinate(randomDimension).setValue(newCoordinate);
        FoodSource resultFoodSource =  new FoodSource(this, result);
        resultFoodSource.calculateFitness();
        return resultFoodSource;
    }

    public int getNumDimensions() { return numDimensions; }
    public Algorithm getParent() { return parent; }
    public int getIndex() { return index; }
    public FoodSource getBestFoundFoodSource() { return bestFoundFoodSource; }
    public FoodSource getFoodSource(int index) { return foodSources[index]; }
}
