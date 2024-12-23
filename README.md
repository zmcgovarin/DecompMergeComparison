# Decomposition and Merging Evolutionary Algorithms
This repository contains the implementation for the paper: Comparative Study of Decomposition and Merging Evolutionary Algorithms for Large-Scale Optimization Problems.

## Overview
Implementations for several existing and several new evolutionary algorithm variations are provided. The goal of this project is to take the decomposition and merging techniques previously seen in Decomposition- and Merging-Cooperative Particle Swarm Optimization and apply them to Cooperative Artificial Bee Colony and Cooperatiev Co-evolutionary Differential Evolution. We then perform a comparative study between the new and existing algorithms. The code for the existing algorithms is implemented from scratch according their respective papers. Otherwise, the new algorithm variations presented are "DCDE", "MCDE", "DCABC", and "MCABC". 

## Compiling
The project is built using Java version 6 (jdk1.6.0_45), available at: https://www.oracle.com/ca-en/java/technologies/javase-java-archive-javase6-downloads.html. This older version of Java is used because some functions within the benchmarking packages are not supported in later Java versions. The project should otherwise compile without issue. 

## Running the Project
The project can be ran using the provided JAR file in the "Build" folder or by compiling the source yourself and running the "Main" class. To run the JAR, simply open a terminal within the "Build" folder and run:

java -jar ...

Most relevant parameters can be passed as arguments or manually modified in the "Main" class. 

NOTE: The 2013 benchmark functions can only be run on Linux or MacOS. This is becuase these functions load an external library (found in the "lib" folder) for which only Linux and MacOS versions are provided. 

## Data
The data obtained by the experiments presented in our work is provided in the "Data" folder as "Data/Benchmark Year/Number of Dimensions/". The CSV files are formatted as follows:
- Each row indicates a different benchmarking function (first row -> first benchmarking function)
- The values presented in the first 30 columns are the final fitness evaluations of the best solution found by the algorithm
- Each of the first 30 columns indicates a different run of the algorithm (first column -> first execution of the algorithm)
- The 31st column is the minimum ("best") value contained within the previous 30 columns
- The 32nd column is the average of the first 30 columns  

## Statistical Analysis
Results from the Kruskal-Wallis and Iman-Conover tests can be found within the "Stats" folder. These tests were comleted using the SciPy (scipy.org) and SciKit (scikit-learn.org) Python libraries. The scores of the algorithms based on the number of functions won/lost/tied are also provided. 

## Benchmarking Soure Code
The benchmarking functions used in this project are from the the CEC’2010 Special Session and Competition on Large-Scale Global Optimization and CEC’2013 Special Session and Competition on Large-Scale Global Optimization. The original source code for the 2010 benchmark functions is credited to Thomas Weise (tweise@gmx.de) while the 2013 functions are credited to Giovanni Iacca (giovanniiaccca@incas3.eu). The code for both the 2010 and 2013 functions has been slightly modified for ease of use within this project.
