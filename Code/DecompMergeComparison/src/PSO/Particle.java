package PSO;

import java.util.Random;

import Main.Position;

/**
 * This class represents a Particle in the Swarm.
 */
public class Particle
{
    //Particle movement variables
    private double cognitiveCoefficient; //AKA c1
    private double socialCoefficient; //AKA c2
    private double inertiaWeight; //AKA w

    private Position personalBest; // The position of this particle's best solution found
    private double personalBestFitness; // The fitness evaluation of the best solution found
    private Position currentPosition; // The current position of the particle on the graph
    private Position velocity; // The current velocity vector of the particle
    private double fitness; // Evaluation of the current position (its fitness / score)
    private final Swarm parentSwarm;
    private final Random numGen;

    public Particle(Swarm parentSwarm, int[] decisionVariables, Random numGen)
    {
        this.parentSwarm = parentSwarm;
        this.numGen = numGen;
        currentPosition = new Position(decisionVariables, parentSwarm.numDimensions);
        currentPosition.randomize(numGen);
        velocity = new Position(decisionVariables, parentSwarm.numDimensions);
        personalBestFitness = Double.MAX_VALUE;
        personalBest = currentPosition.clone();

        cognitiveCoefficient = 1.49618;
        socialCoefficient = 1.49618;
        inertiaWeight = 0.729844;
    }

    public Particle(Swarm parentSwarm, Position currentPos, Position velocity, Position best, Random numGen)
    {
        this.parentSwarm = parentSwarm;
        this.numGen = numGen;
        this.currentPosition = currentPos.clone();
        this.velocity = velocity.clone();
        this.personalBest = best.clone();
        personalBestFitness = Double.MAX_VALUE;
        //calculateFitness(); < Don't do this, need all swarms initialized first
    }

    public void updatePosition()
    {
        double r1, r2, currentCoord, currentVelocity, newVelocity;

        for(int d = 0; d < parentSwarm.numDimensions; d++)
        {
            r1 = numGen.nextDouble();
            r2 = numGen.nextDouble();

            currentCoord = currentPosition.getCoordinate(d).getValue();
            currentVelocity = velocity.getCoordinate(d).getValue();
            newVelocity = (inertiaWeight * currentVelocity) +
                    (cognitiveCoefficient*r1*(personalBest.getCoordinate(d).getValue() - currentCoord)) +
                    (socialCoefficient*r2*(parentSwarm.getGlobalBestPosition().getCoordinate(d).getValue() - currentCoord));

            velocity.getCoordinate(d).setValue(newVelocity);
            currentPosition.getCoordinate(d).increaseValue(newVelocity);
        }
    }

    public void calculateFitness()
    {
        fitness = parentSwarm.parent.calculateFitness(parentSwarm.getIndex(), currentPosition);
        if(fitness < personalBestFitness)
        {
            personalBestFitness = fitness;
            personalBest = currentPosition.clone();
        }
    }

    public Position getPersonalBest() { return personalBest; }
    public void setPersonalBest(Position personalBest) { this.personalBest = personalBest; }

    public Position getPosition() { return currentPosition; }
    public void setPosition(Position position) { this.currentPosition = position; }

    public Position getVelocity() { return velocity; }
    public void setVelocity(Position velocity) { this.velocity = velocity; }

    public double getFitness() { return fitness; }
    public void setFitness(double fitness) { this.fitness = fitness; }
}
