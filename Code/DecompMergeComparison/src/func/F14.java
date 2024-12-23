package func;
/*
 * Copyright (c) 2009 Thomas Weise for NICAL
 * http://www.it-weise.de/
 * tweise@gmx.de
 *
 * GNU LESSER GENERAL PUBLIC LICENSE (Version 2.1, February 1999)
 */

import javacec2013.JNIfgeneric2013;

import static func.Defaults.benchmark2013;

/**
 * <p>
 * The D/m-group group Shifted and m-rotated Elliptic Function: F14.
 * </p>
 * <p>
 * This function is not <warning>not threadsafe</warning> because it uses
 * internal temporary variables. Therefore, you should always use each
 * instance of this function for one single {#link java.lang.Thread} only.
 * You may clone or serialize function instances to use multiple threads.
 * <p>
 * 
 * @author Thomas Weise
 */
public final class F14 extends ShiftedPermutatedRotatedFunction {

  /** the serial version id */
  private static final long serialVersionUID = 1;

  /** the maximum value */
  public static final double MAX = 100d;

  /** the minimum value */
  public static final double MIN = (-MAX);

  /** the lookup table */
  private final double[] m_lookup;

  private JNIfgeneric2013 func;

  /**
   * Create a new D/m-group group Shifted and m-rotated Elliptic Function
   * 
   * @param o
   *          the shifted global optimum
   * @param p
   *          the permutation vector
   * @param m
   *          the rotation matrix
   */
  public F14(final double[] o, final int[] p, final double[] m) {
    super(o, p, m, MIN, MAX);
    this.m_lookup = Kernel.createPowLookup(this.m_matDim);

    if(benchmark2013)
    {
      func = new JNIfgeneric2013(14, Defaults.DEFAULT_DIM);
    }
  }

  /**
   * Create a default instance of F14.
   * 
   * @param r
   *          the randomizer to use
   */
  public F14(final Randomizer r) {
    this(r.createShiftVector(Defaults.DEFAULT_DIM, MIN, MAX),//
        r.createPermVector(Defaults.DEFAULT_DIM),//
        r.createRotMatrix1D(Defaults.DEFAULT_M));//
  }

  /**
   * Create a default instance of F14.
   */
  public F14() {
    this(Defaults.getRandomizer(F14.class));
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

    final int max, gs, d;
    double s;
    int i, e;

    gs = this.m_matDim;
    d = this.m_dimension;
    max = (d / gs);

    s = 0d;
    e = 0;
    for (i = 0; i < max; i++) {
      s += Kernel.shiftedPermRotElliptic(x, this.m_o, this.m_p, this.m_m,//
          e, gs, this.m_tmp, this.m_lookup); //
      e += gs;
    }

    return (s /*
               * + Kernel.shiftedPermElliptic(x, this.m_o, this.m_p, e,
               * this.m_dimension - e)
               */);
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
    return "D/m-group group Shifted and m-rotated Elliptic Function";//$NON-NLS-1$
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
    return "F14"; //$NON-NLS-1$
  }
}
