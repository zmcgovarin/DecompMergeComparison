package Main;
/**
 * This package implements several popular meta-heuristics, namely:
 * Artificial Bee Colony (ABC)
 * Differential Evolution (DE)
 * Particle Swarm Optimization (PSO)
 *
 * The Cooperative Co-evolutionary (CC) variants of these meta-heuristics are also implemented:
 * Cooperative ABC (CABC)
 * Cooperative Co-evolutionary DE (CCDE)
 * Cooperative PSO (CPSO)
 *
 * These CC variants are then additionally modified to use decomposition and merging techniques, creating the following
 * algorithms:
 * Decomposition CABC (DCABC) [New]
 * Merging CABC (MCABC) [New]
 * Decomposition CCDE (DCDE) [New]
 * Merging CCDE (MCDE) [New]
 * Decomposition CPSO (DCPSO)
 * Merging CPSO (MCPSO)
 *
 * See README.md and LICENSES for further information.
 *
 * @author Zachary McGovarin
 * @version 1.0
 * @since 2024/06/05
 * */


import ABC.ABC;
import ABC.CABC;
import ABC.DCABC;
import ABC.MCABC;
import DE.CCDE;
import DE.DCDE;
import DE.DE;
import DE.MCDE;
import PSO.CPSO;
import PSO.DCPSO;
import PSO.MCPSO;
import PSO.PSO;
import func.Defaults;
import func.Function;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Main
{
    private static final int TOTAL_TESTS = 20; // 30
    private static final double[] RUN_RESULTS = new double[TOTAL_TESTS];

    public static boolean isBenchmark2013 = false; // Use 2013 benchmark suite if true, else use 2010 benchmarks
    public static int totalDimensions = 30;
    public static AlgorithmType currentAlgorithm = AlgorithmType.DCPSO;
    private static int resultCount = 0; // Count number of completed runs
    public static int funcId = 1; // Current benchmark function number
    private static int startingFunction = 1;
    private static double maxPos;
    private static double minPos;

    public Main()
    {
        runAlgorithmBenchmarks();
    }

    public void runAlgorithmBenchmarks()
    {
        try
        {
            String fileName = createFileName();
            String path = "Output/" + ((isBenchmark2013)?"2013":"2010") + "/" + totalDimensions;

            File directory = new File(path);
            if (!directory.exists())
                directory.mkdirs();

            File file = new File(path + "/" + fileName);
            FileWriter writer = new FileWriter(file);

            int numFunctions = (isBenchmark2013)?15:20;
            for (int i = startingFunction; i <= numFunctions; i++) //For each function
            {
                long startTime = System.currentTimeMillis();
                resultCount = 0;
                funcId = i;
                Function f = functionSelection(i);
                minPos = f.getMin();
                maxPos = f.getMax();
                System.out.println("Starting tests on function " + i + ": " + f);
                System.out.println("Domain min: " + minPos + " | Domain max: " + maxPos + " | Time: " + startTime);
                Thread[] threads = new Thread[TOTAL_TESTS];

                for(int x = 0; x < TOTAL_TESTS; x++) // For each test run
                {
                    // All of our algorithm implementations are designed to be thread-safe
                    threads[x] = new Thread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            try
                            {
                                Function f = functionSelection(funcId);
                                Algorithm algo = algorithmSelection(currentAlgorithm, f);
                                double result = algo.start();
                                saveResult(result);
                                System.out.println("Result: " + result); // This is thread-safe per JDK source
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                            }
                        }
                    });
                    threads[x].start();
                }

                // Wait until all threads are finished before continuing
                for(Thread aThread : threads)
                {
                    aThread.join();
                }
                System.out.println("All threads have finished.");

                double testAvg = 0;
                double min = Double.MAX_VALUE;

                for (int j = 0; j < TOTAL_TESTS; j++)
                {
                    writer.append(Double.toString(RUN_RESULTS[j]));
                    writer.append(',');
                    testAvg += RUN_RESULTS[j];
                    if(RUN_RESULTS[j] < min)
                        min = RUN_RESULTS[j];
                }
                testAvg = testAvg / TOTAL_TESTS;

                System.out.println("F" + i + " | Min: " + min + " | Average: " + testAvg +
                        " | Duration: " + convertMilliseconds(System.currentTimeMillis() - startTime));
                writer.append(Double.toString(min));
                writer.append(",");
                writer.append(Double.toString(testAvg));
                writer.append('\n');
                writer.flush();
            }
            writer.close();
        }
        catch(Exception e)
        {
            System.out.println("Error: " + e);
        }
    }

    public String createFileName()
    {
        StringBuilder fileName = new StringBuilder();
        fileName.append("./");
        fileName.append(currentAlgorithm.name());

        fileName.append("_");
        fileName.append(totalDimensions);
        fileName.append("dim");

        if(isBenchmark2013)
            fileName.append("_2013");
        else
            fileName.append("_2010");

        fileName.append("_");
        fileName.append(new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss").format(Calendar.getInstance().getTime()));
        fileName.append(".csv");
        return fileName.toString();
    }

    public static synchronized void saveResult(double result)
    {
        RUN_RESULTS[resultCount] = result;
        resultCount++;
    }

    public static AlgorithmType getAlgorithmTypeFromName(String name)
    {
        if(name.equalsIgnoreCase("Main.RandomSearch"))
            return AlgorithmType.RandomSearch;
        else if (name.equalsIgnoreCase("PSO"))
            return AlgorithmType.PSO;
        else if (name.equalsIgnoreCase("CPSO"))
            return AlgorithmType.CPSO;
        else if (name.equalsIgnoreCase("DCPSO"))
            return AlgorithmType.DCPSO;
        else if (name.equalsIgnoreCase("MCPSO"))
            return AlgorithmType.MCPSO;
        else if (name.equalsIgnoreCase("CCDE"))
            return AlgorithmType.CCDE;
        else if (name.equalsIgnoreCase("DCDE"))
            return AlgorithmType.DCDE;
        else if (name.equalsIgnoreCase("MCDE"))
            return AlgorithmType.MCDE;
        else if (name.equalsIgnoreCase("ABC"))
            return AlgorithmType.ABC;
        else if (name.equalsIgnoreCase("CABC"))
            return AlgorithmType.CABC;
        else if (name.equalsIgnoreCase("DCABC"))
            return AlgorithmType.DCABC;
        else if (name.equalsIgnoreCase("MCABC"))
            return AlgorithmType.MCABC;
        return null;
    }

    public Algorithm algorithmSelection(AlgorithmType algo, func.Function func)
    {
        switch(algo)
        {
            case RandomSearch:
                return new RandomSearch(func);
            case PSO:
                return new PSO(func);
            case CPSO:
                return new CPSO(func);
            case DCPSO:
                return new DCPSO(func);
            case MCPSO:
                return new MCPSO(func);
            case DE:
                return new DE(func);
            case CCDE:
                return new CCDE(func);
            case DCDE:
                return new DCDE(func);
            case MCDE:
                return new MCDE(func);
            case ABC:
                return new ABC(func);
            case CABC:
                return new CABC(func);
            case DCABC:
                return new DCABC(func);
            case MCABC:
                return new MCABC(func);
        }
        System.out.println("ERROR: Invalid algorithm specified.");
        return null;
    }

    public func.Function functionSelection(int function)
    {
        func.Function f = new func.F1();
        switch (function)
        {
            case 1:
                f = new func.F1();
                break;
            case 2:
                f = new func.F2();
                break;
            case 3:
                f = new func.F3();
                break;
            case 4:
                f = new func.F4();
                break;
            case 5:
                f = new func.F5();
                break;
            case 6:
                f = new func.F6();
                break;
            case 7:
                f = new func.F7();
                break;
            case 8:
                f = new func.F8();
                break;
            case 9:
                f = new func.F9();
                break;
            case 10:
                f = new func.F10();
                break;
            case 11:
                f = new func.F11();
                break;
            case 12:
                f = new func.F12();
                break;
            case 13:
                f = new func.F13();
                break;
            case 14:
                f = new func.F14();
                break;
            case 15:
                f = new func.F15();
                break;
            case 16:
                f = new func.F16();
                break;
            case 17:
                f = new func.F17();
                break;
            case 18:
                f = new func.F18();
                break;
            case 19:
                f = new func.F19();
                break;
            case 20:
                f = new func.F20();
                break;
        }
        return f;
    }

    public String convertMilliseconds(long ms)
    {
        int remaining = (int) (ms % 1000);
        int seconds = (int) (ms / 1000) % 60 ;
        int minutes = (int) ((ms / (1000*60)) % 60);
        int hours = (int) ((ms / (1000*60*60)) % 24);
        return String.format("%d:%d:%d.%d", hours, minutes, seconds, remaining);
    }

    public static void main(String[] args)
    {
        if((args.length < 3 || args.length > 4) && args.length > 0)
        {
            System.out.println("Command args: [Algorithm] [Dimensions] [Benchmark Year] (Starting function number)");
            System.exit(0);
        }
        if(args.length > 0)
        {
            currentAlgorithm = getAlgorithmTypeFromName(args[0]);
            totalDimensions = Integer.parseInt(args[1]);
            if(Integer.parseInt(args[2]) == 2013)
                isBenchmark2013 = true;
            if(args.length == 4)
                startingFunction = Integer.parseInt(args[3]);
        }

        if(startingFunction < 0 || startingFunction > 20)
        {
            System.out.println("Invalid starting function number specified (" + startingFunction + ").");
            System.exit(0);
        }
        if(totalDimensions < 25 || totalDimensions > 10000)
        {
            System.out.printf("Invalid number of dimensions specified (" + totalDimensions + " ).");
            System.exit(0);
        }

        if(currentAlgorithm != null)
            System.out.println("ALGORITHM: " + currentAlgorithm.name());
        System.out.println("DIMENSIONS: " + totalDimensions);
        System.out.println("BENCHMARK SUITE YEAR: " + ((isBenchmark2013)?"2013":"2010"));
        func.Defaults.DEFAULT_DIM = totalDimensions;
        Defaults.benchmark2013 = isBenchmark2013;
        new Main();
    }

    public static double getMaxPos() { return maxPos; }
    public static double getMinPos() { return minPos; }
}

