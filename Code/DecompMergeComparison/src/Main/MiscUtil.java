package Main;

import java.util.Random;

public class MiscUtil
{
    public int[] getRandomDecisionVarsArray(int totalDimensions, Random numGen)
    {
        int[] temp = new int[totalDimensions];
        for (int i = 0; i < totalDimensions; ++i)
            temp[i] = i;
        shuffleArray(temp, numGen);
        return temp;
    }

    // Modern Fisher–Yates shuffle to randomly permute the array
    // https://en.wikipedia.org/wiki/Fisher%E2%80%93Yates_shuffle#The_modern_algorithm
    public void shuffleArray(int[] array, Random numGen)
    {
        for (int i = array.length - 1; i > 0; i--)
        {
            int index = numGen.nextInt(i + 1);
            int temp = array[index];
            array[index] = array[i];
            array[i] = temp;
        }
    }
}
