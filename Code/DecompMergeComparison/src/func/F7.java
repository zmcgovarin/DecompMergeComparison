package func;
/*
 * Copyright (c) 2009 Thomas Weise for NICAL
 * http://www.it-weise.de/
 * tweise@gmx.de
 *
 * GNU LESSER GENERAL PUBLIC LICENSE (Version 2.1, February 1999)
 */

import javacec2013.JNIfgeneric2013;

import java.io.BufferedWriter;

import static func.Defaults.benchmark2013;

/**
 * The Single-group Shifted m-dimensional Schw Problem 1.2: F7.
 * 
 * @author Thomas Weise
 */
public final class F7 extends ShiftedPermutatedFunction {

  /** the serial version id */
  private static final long serialVersionUID = 1;

  /** the maximum value */
  public static final double MAX = 100d;

  /** the minimum value */
  public static final double MIN = (-MAX);

  /** the m-value */
  private final int m_m;

  private JNIfgeneric2013 func;

  /**
   * Create a new Single-group Shifted m-dimensional Schwefe Problem 1.2
   * 
   * @param o
   *          the shifted global optimum
   * @param p
   *          the permutation vector
   * @param m
   *          the fraction of nonseparability
   */
  public F7(final double[] o, final int[] p, final int m) {
    super(o, p, MIN, MAX);
    this.m_m = m;

    if(benchmark2013)
    {
      func = new JNIfgeneric2013(7, Defaults.DEFAULT_DIM);
    }
  }

  /**
   * Create a default instance of F7.
   * 
   * @param r
   *          the randomizer to use
   */
  public F7(final Randomizer r) {
    this(r.createShiftVector(Defaults.DEFAULT_DIM, MIN, MAX),//
        r.createPermVector(Defaults.DEFAULT_DIM),//
        Defaults.DEFAULT_M);//
  }

  /**
   * Create a default instance of F7.
   */
  public F7() {
    this(Defaults.getRandomizer(F7.class));
  }

  /**
   * Compute the value of the elliptic function. This function takes into
   * consideration only the first {{@link #getDimension()} elements of the
   * candidate vector.
   * 
   * @param x
   *          the candidate solution vector
   * @return the value of the function
   */
  // @Override
  public final double compute(final double[] x)
  {
    if(benchmark2013)
      return func.evaluate(x);

    return (Kernel.shiftedPermSchwefel12(x, this.m_o, this.m_p, //
        0, this.m_m) * 1e6) + //
        Kernel.shiftedPermSphere(x, this.m_o, this.m_p, this.m_m,//
            this.m_dimension - this.m_m);
  }

  /**
   * Obtain the full name of the benchmark function (according to
   * &quot;Benchmark Functions for the CEC�2010 Special Session and
   * Competition on Large-Scale Global Optimization&quot; Ke Tang, Xiaodong
   * Li, P. N. Suganthan, Zhenyu Yang, and Thomas Weise CEC'2010)
   * 
   * @return the full name of the benchmark function
   */
  // @Override
  public final String getFullName() {
    return "Single-group Shifted m-dimensional Schwefel�s Problem 1.2";//$NON-NLS-1$
  }

  /**
   * Obtain the short name of the benchmark function (according to
   * &quot;Benchmark Functions for the CEC�2010 Special Session and
   * Competition on Large-Scale Global Optimization&quot; Ke Tang, Xiaodong
   * Li, P. N. Suganthan, Zhenyu Yang, and Thomas Weise CEC'2010)
   * 
   * @return the short name of the benchmark function
   */
  // @Override
  public final String getShortName() {
    return "F7"; //$NON-NLS-1$
  }

  /**
   * Store the function information
   * 
   * @param w
   *          the writer to store
   * @throws Throwable
   *           a possible io exception
   */
  // @Override
  public void storeFunctionInfo(final BufferedWriter w)
      throws Throwable {

    super.storeFunctionInfo(w);

    w.newLine();
    w.write("m-value  : ");//$NON-NLS-1$
    w.write(Integer.toString(this.m_m));
  }
}
