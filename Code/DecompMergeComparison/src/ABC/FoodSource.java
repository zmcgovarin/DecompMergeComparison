package ABC;

import java.util.Random;

import Main.Position;

public class FoodSource
{
    private Position position;
    private double fitness;
    private final Swarm parentSwarm;
    private int trialCount;

    public FoodSource(Swarm parent, int numDim, int[] decisionVars, Random numGen)
    {
        this.fitness = Double.MAX_VALUE;
        this.parentSwarm = parent;
        this.position = new Position(decisionVars, numDim);
        this.trialCount = 0;
        randomizePosition(numGen);
    }

    public FoodSource(Swarm parent, Position pos)
    {
        this.parentSwarm = parent;
        this.position = pos.clone();
        this.trialCount = 0;
    }

    public FoodSource(Swarm parent, Position pos, double fitness)
    {
        this.parentSwarm = parent;
        this.position = pos.clone();
        this.fitness = fitness;
        this.trialCount = 0;
    }

    public FoodSource clone() { return new FoodSource(this.parentSwarm, this.position, this.fitness); }

    public void randomizePosition(Random numGen)
    {
        position.randomize(numGen);
        // Do not evaluate the fitness of the position yet, we wait until all Swarms are initialized in ABC class,
        // then call each Swarm to evaluate their food sources
    }

    public void calculateFitness()
    {
        fitness = parentSwarm.getParent().calculateFitness(parentSwarm.getIndex(), position);
    }


    public Position getPosition() { return position; }
    public void setPosition(Position pos) { this.position = pos; }
    public double getFitness() { return fitness; }
    public int getTrialCount() { return trialCount; }
    public void incrementTrialCount() { trialCount++; }
    public void resetTrialCount() { trialCount = 0; }
}

