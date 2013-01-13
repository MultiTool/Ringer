/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package genwave;

/**
 *
 * @author MultiTool
 * 
 * Handy functions.  I like cats.
 * 
 */
public class Cats {

  public static java.util.Random rand = new java.util.Random();
  /* **************************************************************************** */
  public static double Sigmoid(double run) {
    double rise;
    rise = run / Math.sqrt(1.0 + run * run);
    return rise;
  }
  /* **************************************************************************** */
  public static double InvSigmoid(double run) {
    double rise;//x = y/ sqrt(1-yy)
    rise = run / Math.sqrt(1.0 - run * run);
    return rise;
  }
  /* **************************************************************************** */
  public static void TestSigmoid() {
    double run;
    for (run = -1; run < 1; run += 0.1) {
      double rise = Sigmoid(run);
      System.out.println(String.format("{0}, {1}", run, rise));
    }
  }
  /* **************************************************************************** */
  public static double CurveMap(double Min, double Ctr, double Max) {// make random numbers in a normal distribution
    Min = Min - Ctr;// make Ctr the origin point, the zero
    Max = Max - Ctr;
    double loval = Sigmoid(Min);
    double hival = Sigmoid(Max);
    double range = hival - loval;

    double randval = Cats.rand.nextDouble();

    double run = loval + randval * range;

    double rise = InvSigmoid(run); // this is not finished
    return rise;
  }
  /* **************************************************************************** */
  public static void Curve() {
    double run;
    for (run = -1; run < 1; run += 0.01) {
      double rise = InvSigmoid(run);// Math.Log(-(run / (run - 1))) / Math.Log(Math.E);
      System.out.println(String.format("{0}", rise));
    }
  }
}
