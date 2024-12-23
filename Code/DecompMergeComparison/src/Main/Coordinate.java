package Main;

import java.util.Random;

public class Coordinate
{
    private double value;
    private int dimIndex;

    public Coordinate(double value, int dimIndex)
    {
        this.value = value;
        this.dimIndex = dimIndex;
    }

    public Coordinate clone()
    {
        return new Coordinate(this.value, this.dimIndex);
    }

    public void setRandomValue(double min, double max, Random numGen)
    {
        value = (numGen.nextDouble() * (max - min)) + min;
    }

    public void increaseValue(double amount)
    {
        this.value += amount;
    }

    public double getValue() { return value; }
    public void setValue(double value) { this.value = value; }
    public int getDimIndex() { return dimIndex; }
    public void setDimIndex(int dimIndex) { this.dimIndex = dimIndex; }
}
