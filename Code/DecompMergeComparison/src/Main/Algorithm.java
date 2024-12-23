package Main;

public abstract class Algorithm
{
//    private int totalDimensions;
//    private int maxEval;
//    private int populationSize;

    // Add some sort of default static class instead to change population sizes,
    // Ex. set default swarm size before initializing PSO

//    public Main.Algorithm(int totalDimensions, int populationSize)
//    {
//        this.totalDimensions = totalDimensions;
//        this.populationSize = populationSize;
//
//        maxEval = totalDimensions * 3000;
//    }
//
//    public int getMaxEval() { return maxEval; }
//    public void setMaxEval(int maxEval) { this.maxEval = maxEval; }
//
//    public int getPopulationSize() { return populationSize; }
//    public void setPopulationSize(int populationSize) { this.populationSize =  populationSize; }
//
//    public int getTotalDimensions() { return totalDimensions; }
//    public void setTotalDimensions(int totalDimensions) { this.totalDimensions = totalDimensions; }

    abstract public double getOverallBestFitness();
    abstract public void setOverallBestFitness(double fitness);
    abstract public double calculateFitness(int swarmIndex, Position posToEval);
    abstract public int getEvalBetweenDecomp();
    abstract public int getCountEvalForDecomp();
    abstract public int getTotalDimensionsPerSwarm();
    abstract public double start();
    abstract public String getName();

    public int getLimit() { return -1; };
    // -> Abstract class and put these variables in it ?
}
