package Main;

import java.util.Random;

public class Position
{
    private Coordinate[] coords;

    public Position(int[] decisionVars, int numDims)
    {
        coords = new Coordinate[numDims];

        for(int i = 0; i < numDims; i++)
        {
            coords[i] = new Coordinate(0, decisionVars[i]);
        }
    }

    public void randomize(Random numGen)
    {
        for(int i = 0; i < coords.length; i++)
        {
            coords[i].setRandomValue(Main.getMinPos(), Main.getMaxPos(), numGen);
        }
    }

    public Position(Coordinate[] c)
    {
        this.coords = new Coordinate[c.length];
        for(int i = 0; i < c.length; i++)
        {
            this.coords[i] = c[i].clone();
        }
    }

    public Position clone()
    {
        return new Position(this.coords);
    }

    public double[] getValueVector()
    {
        double[] temp = new double[coords.length];

        for(int i = 0; i < coords.length; i++)
            temp[i] = coords[i].getValue();

        return temp;
    }

    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < coords.length; i++)
        {
            sb.append(coords[i].getValue());
            sb.append(" [");
            sb.append(coords[i].getDimIndex());
            sb.append("]");
            if(i != coords.length - 1)
                sb.append(", ");
        }
        return sb.toString();
    }

    public Coordinate getCoordinate(int index) { return coords[index]; }
    public Coordinate[] getCoordinates() { return coords; }
}
