package PSO;

import java.util.Random;

import Main.Algorithm;
import Main.Position;
import Main.Coordinate;

/**
 * This class creates a swarm consisting of particle objects.
 */
public class Swarm
{
    //Swarm variables
    private final int index;
    public int numDimensions;
    public static final int numParticles = 20; // The total number of particles in the swarm
    private final Particle[] particles;
    private Position globalBestPosition;
    public final Algorithm parent;
    private final Random numGen;

    public Swarm(Algorithm parent, int[] decisionVars, int numDimensions, int index, Random numGen)
    {
        this.parent = parent;
        this.numDimensions = numDimensions;
        this.index = index;
        this.numGen = numGen;
        particles = new Particle[numParticles];

        initializeSwarm(decisionVars);
    }

    public Swarm(Algorithm parent, Coordinate[][] currentPos, Coordinate[][] velocity, Coordinate[][] particleBest,
                 Coordinate[] swarmBest, int numDimensions, int index, Random numGen)
    {
        assert currentPos[0].length == numDimensions: "Swarm initialization failed (Num coordinates != num particles).";
        assert currentPos.length == numParticles: "Swarm initialization failed (Num coordinates != num dimensions).";

        this.parent = parent;
        this.numDimensions = numDimensions;
        this.index = index;
        this.numGen = numGen;
        particles = new Particle[numParticles];
        this.globalBestPosition = new Position(swarmBest);

        for(int p = 0; p < numParticles; p++)
        {
            particles[p] = new Particle(this, new Position(currentPos[p]), new Position(velocity[p]),
                    new Position(particleBest[p]), numGen);
        }
    }

    private void initializeSwarm(int[] decisionVars)
    {
        assert numDimensions == decisionVars.length: "Number of dimensions != number of decision variable indices";
        for(int i = 0; i < numParticles; i++)
            particles[i] = new Particle(this, decisionVars, numGen);

        // Must be initialized to calculate fitness via context vector
        globalBestPosition = particles[0].getPosition().clone();
    }

    public void calculateSwarmFitness()
    {
        for(Particle p : particles)
        {
            p.calculateFitness();

            if(p.getFitness() < parent.getOverallBestFitness())
            {
                globalBestPosition = p.getPosition().clone();
                //globalBestFitness = p.getFitness();
                parent.setOverallBestFitness(p.getFitness());
            }
        }
    }

    public void updateSwarmPositions()
    {

        for(Particle p: particles)
        {
            p.updatePosition();
            p.calculateFitness();

            if(p.getFitness() < parent.getOverallBestFitness()) // Check if new particle is fitness better than swarm's best
            {
                globalBestPosition = p.getPosition().clone();
                parent.setOverallBestFitness(p.getFitness());
            }
        }
    }

    public Position getGlobalBestPosition() { return globalBestPosition; }
    public Particle getParticle(int index) { return particles[index]; }
    public int getIndex() { return index; }
}
