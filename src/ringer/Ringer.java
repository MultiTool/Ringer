package ringer;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/*
 Note: There are odd code patterns here, put only only to make porting to C++ or D language easier.
 */
class Ringer {
  public static final int ClipLow = -1024, ClipHigh = +1024;
  public static int ClipRange = ClipHigh - ClipLow;
  public static void main(String[] args) {
    // TODO code application logic here

    if (false) {
      Audio aud = new Audio();
      aud.Test();
      //ScoreTest(int SoundInts[], int NumSamples)
      return;
    }

    Ringer.Population pop = new Ringer.Population();
    Test();
  }
  /* **************************************************************************** */
  public interface IDeletable {
    void Delete();// virtual
  }

  /* **************************************************************************** */
  public static class Population implements IDeletable {
    public int MyPopSize;
    public Ind[] Peeps;
    Audio aud = null;
    public Population() {
      aud = new Audio();
    }
    public void Seed(int PopSize, int SampleSize) {
      Ind ani;
      MyPopSize = PopSize;
      Peeps = new Ind[MyPopSize];
      for (int cnt = 0; cnt < MyPopSize; cnt++) {
        ani = new Ind();
        ani.Seed(SampleSize);
        Peeps[cnt] = ani;
      }
    }
    public void Sort() {
      Collections.shuffle(Arrays.asList(this.Peeps));// shuffle to prevent bias in order when many scores are equal
      Arrays.sort(this.Peeps, new Comparator<Ind>() {
        @Override
        public int compare(Ind I0, Ind I1) {
          return Double.compare(I0.Score, I1.Score);
        }
      });
    }
    /* **************************************************************************** */
    public void NextGen2(double Fraction) {
      int LoFrac = (int) (this.MyPopSize * Fraction);
      int HiFrac = this.MyPopSize - LoFrac;
      int Ultimo = this.MyPopSize - 1;
      int TopCnt = Ultimo;
      Ind doomed, parent;
      for (int cnt = 0; cnt < LoFrac; cnt++) {
        doomed = Peeps[cnt];
        doomed.Delete();
        parent = this.Peeps[TopCnt];
        Peeps[cnt] = parent.Copy();
        TopCnt--;
        if (TopCnt < LoFrac) {
          TopCnt = Ultimo;
        }// wrap
      }
    }
    /* **************************************************************************** */
    public void NextGen(double Fraction) {
      int LoFrac = (int) (this.MyPopSize * Fraction);
      int HiFrac = this.MyPopSize - LoFrac;
      int Ultimo = this.MyPopSize - 1;
      int TopCnt = Ultimo;
      Ind doomed, parent;
      for (int cnt = LoFrac - 1; cnt >= 0; cnt--) {
        doomed = Peeps[cnt];
        doomed.Delete();
        parent = this.Peeps[TopCnt];
        Peeps[cnt] = parent.Copy();
        TopCnt--;
        if (TopCnt < LoFrac) {
          TopCnt = Ultimo;
        }// wrap
      }
    }
    /* **************************************************************************** */
    public void Mutate(double PopRate, double IndRate) {
      /*
       so decide, will each ind have its own array length, or will all be the same?

       pro static size:
       can keep hidden information after being shrunk and then expanded again
       easy to maintain

       pro dynamic size:
       more efficient with memory
       therefore has potential for longer arrays per experiment

       */
      if (true) {
        // this assumes that the population was sorted with high scorers on top, and their kids copied to the bottom
        int Fraction = (int) (this.MyPopSize * PopRate);
        for (int cnt = 0; cnt < Fraction; cnt++) {
          Peeps[cnt].Mutate(IndRate);
        }
      } else {
        for (int cnt = 0; cnt < this.MyPopSize; cnt++) {
          if (Cats.rand.nextDouble() < PopRate) {
            Peeps[cnt].Mutate(IndRate);
          }
        }
      }
      int FreakNum = this.MyPopSize / 8;
      //FreakNum = 1;
      int cnt = 0;
      if (true) {
        while (cnt < this.MyPopSize) {
          if ((FreakNum <= cnt) && Peeps[cnt].Score != 0.0) {
            break;
          }
          Peeps[cnt].Freak();// plus one fully-random kid to jump out of local maxima
          cnt++;
        }
      } else {
        while (cnt < FreakNum || Peeps[cnt].Score == 0.0) {
          //Peeps[cnt].Freak();// plus one fully-random kid to jump out of local maxima
          Peeps[cnt].Freak();// plus one fully-random kid to jump out of local maxima
          cnt++;
        }
      }
    }
    /* **************************************************************************** */
    @Override
    public void Delete() {
      for (int cnt = 0; cnt < this.MyPopSize; cnt++) {
        Peeps[cnt].Delete();
      }
      // free this.Peeps;
      this.Peeps = null;
    }
    /* **************************************************************************** */
    public void RunTestAudio() {
      for (int cnt = 0; cnt < MyPopSize; cnt++) {// this must be connected to crucible
        Ind Peep = this.Peeps[cnt];
        int[] Wav = Peep.Wav;
        Peep.Score = aud.ScoreTest(Wav, Wav.length);
        boolean nop = true;
      }
    }
    /* **************************************************************************** */
    public void RunTest(TargetList tl) {
      for (int cnt = 0; cnt < MyPopSize; cnt++) {// this must be connected to crucible
        this.Peeps[cnt].RunTest(tl);
      }
    }
    /* **************************************************************************** */
    public double GetPromedioGanancias() {
      double Sum = 0.0;
      for (int cnt = 0; cnt < this.MyPopSize; cnt++) {
        Sum += Peeps[cnt].Score;
      }
      Sum /= this.MyPopSize;
      return Sum;
    }
    /* **************************************************************************** */
    public double GetElMejorCuenta() {
      return GetElMejor().Score;
    }
    /* **************************************************************************** */
    public Ind GetElMejor() {
      return Peeps[MyPopSize - 1];
    }
  }
  /* **************************************************************************** */
  public static class Socket implements IDeletable {// maybe use this
    public Ind Peep;
    @Override
    public void Delete() {
    }
  }
  /* **************************************************************************** */
  public static class Ind implements IDeletable {
    public static final int MaxSize = 400;
    //public static final int MinSize = 100;
    //public static final int MaxSize = 13;
    public static final int MinSize = MaxSize / 2;
    public static int SizeRange = MaxSize - MinSize;
    public int SampleLength;
    public int[] Wav;
    public double Score, Errors;
    /* **************************************************************************** */
    public void Seed(int SampleSize) {
      SampleLength = SampleSize;
      Wav = new int[MaxSize];
      for (int cnt = 0; cnt < SampleLength; cnt++) {
        Wav[cnt] = ClipLow + Cats.rand.nextInt(ClipRange);
      }
    }
    /* **************************************************************************** */
    public Ind Copy() {
      Ind org = new Ind();
      org.Wav = new int[MaxSize];
      //System.arraycopy(this.Wav, 0, org.Wav, 0, this.SampleLength);
      System.arraycopy(this.Wav, 0, org.Wav, 0, this.MaxSize);
      org.SampleLength = this.SampleLength;
      org.Score = this.Score;
      return org;
    }
    /* **************************************************************************** */
    @Override
    public void Delete() {
      // free Wav;
      Wav = null;
    }
    /* **************************************************************************** */
    public void Mutate(double Rate) {
      for (int cnt = 0; cnt < this.SampleLength; cnt++) {
        if (Cats.rand.nextDouble() < Rate) {
          switch (0) {
            case 0: {
              double mid = this.Wav[cnt];
              if (true) {
                mid = (mid - ClipLow) / ClipRange;// 0 to 1
                mid = (mid * 2.0) - 1.0;// -1.0 to +1.0
                mid = Cats.CurveMap(-1.0, mid, 1.0);
                mid = (mid + 1.0) / 2.0;
                this.Wav[cnt] = (int) (ClipLow + (mid * ClipRange));
              } else {
                mid = Cats.CurveMap(ClipLow, mid, ClipHigh);
                this.Wav[cnt] = (int) mid;
                //this.Wav[cnt] = (int) (ClipLow + (mid * ClipRange));
              }
            }
            case 1: {
              this.Wav[cnt] += -100 + Cats.rand.nextInt(200);
              if (this.Wav[cnt] < ClipLow) {
                this.Wav[cnt] += ClipRange;
              }
              if (ClipHigh <= this.Wav[cnt]) {
                this.Wav[cnt] -= ClipRange;
              }
              break;
            }
            case 2: {
              this.Wav[cnt] = Cats.rand.nextInt(ClipRange) - ClipLow;
              break;
            }
          }
        }
      }
      //ModifyLength(0.01);
      //ModifyLength(0.1);
    }
    /* **************************************************************************** */
    public void Freak() {
      //this.SampleLength = MinSize + Cats.rand.nextInt(SizeRange);
      Wav = new int[MaxSize];
      for (int cnt = 0; cnt < MaxSize; cnt++) {
        Wav[cnt] = ClipLow + Cats.rand.nextInt(ClipRange);
      }
    }
    /* **************************************************************************** */
    public void ModifyLength(double Rate) {
      if (true) {
        if (Cats.rand.nextDouble() < Rate) {
          switch (Cats.rand.nextInt(2)) {
            case 0:// decrement and wrap
              if (--SampleLength < 0) {
                SampleLength += MaxSize;
              }
              break;
            case 1:// increment and wrap
              if (SampleLength > 11) {
                boolean nop = true;
              }
              if (++SampleLength > MaxSize) {
                SampleLength -= MaxSize;
              }
              break;
            default:
              break;
          }
        }
      }
    }
    /* **************************************************************************** */
    public void RunTest(TargetList tl) {// this must be connected to crucible
      if (true) {
        this.Score = tl.Compare(this);
      } else if (true) {// select for ramp
        this.Score = 0;
        this.Errors = 0;
        double sumsq = 0;
        double Value = 0;
        for (int cnt = 0; cnt < this.SampleLength; cnt++) {
          int val = this.Wav[cnt];
          double delta = Math.abs(val - cnt);
          Value += 1.0 / (1.0 + delta);
          double deltasq = delta * delta;
          sumsq += deltasq;
          this.Errors += delta;
          //this.Errors += deltasq;
          // if (delta == 0) { delta = 0.00001; } Score += 1.0 / delta;
        }
        //this.Errors = Math.sqrt(this.Errors);
        //this.Errors = (this.Errors + 1.0) / (double) SampleLength;
        //Score = 1.0 / Errors;
        if (this.Errors < 0.01) {
          boolean nop = true;
        }
        this.Score = Value;
      }
    }
  }
  /* **************************************************************************** */
  public static void Test() {
    TargetList tl = new TargetList();
    tl.SeedRamp(1, Ind.MaxSize);
    Population pop = new Population();
    pop.Seed(100, Ind.MaxSize);
    //pop.Seed(50, Ind.MaxSize);
    double BestScore = 0.0;
    double DragBest = 0.0;
    int GenCnt = 0;
    //while (DragBest < 0.9) {
    while (GenCnt < 1000) {
      //pop.RunTest(tl);
      pop.RunTestAudio();
      pop.Sort();
      BestScore = pop.GetElMejorCuenta();
      DragBest = DragBest * 0.9 + BestScore * 0.1;

      System.out.println(" " + GenCnt + "  " + pop.GetPromedioGanancias() + " " + BestScore);

//      if ((GenCnt % 10000) == 0) {
      if ((GenCnt % 10) == 0) {
        //System.out.println("{0,10:0} {1,10:0.00} {2}", GenCnt, pop.GetPromedioGanancias(), pop.GetElMejor());
        //System.out.format("%i %f %f", GenCnt, pop.GetPromedioGanancias(), pop.GetElMejor());
        // System.out.println(" " + GenCnt + "  " + pop.GetPromedioGanancias() + " " + BestScore);
        // pop.aud.PrintSample(HearInts, HearInts.length);
      }
      pop.NextGen(0.5);
      pop.Mutate(0.5, 0.05);

      GenCnt++;
    }
    System.out.println("Final: " + GenCnt + "  " + pop.GetPromedioGanancias() + " " + BestScore);
  }
  public static class TargetList extends ArrayList<Target> {
    /* **************************************************************************** */
    public void Seed(int NumTargets, int SampleSize) {
      for (int cnt = 0; cnt < NumTargets; cnt++) {
        Target tg = new Target();
        tg.Seed(SampleSize);
        this.add(tg);
      }
    }
    /* **************************************************************************** */
    public void SeedRamp(int NumTargets, int SampleSize) {
      for (int cnt = 0; cnt < NumTargets; cnt++) {
        Target tg = new Target();
        tg.SeedRamp(SampleSize);
        this.add(tg);
      }
    }
    /* **************************************************************************** */
    public double Compare(Ind other) {
      double Score = 0.0;
      double OneScore;
      for (int cnt = 0; cnt < this.size(); cnt++) {
        Target tg = this.get(cnt);
        OneScore = tg.Score(other);
        Score = Math.max(Score, OneScore);
      }
      return Score;
    }
  }
  public static class Target {
    public double Amplitude;
    public double Radius;
    public int VecLen;
    int[] Vector;
    public Target() {
    }
    /* **************************************************************************** */
    public void Seed(int SampleSize) {
      this.VecLen = SampleSize;
      this.Radius = Cats.rand.nextInt(ClipRange / 2);
      this.Amplitude = Cats.rand.nextDouble();
      this.Vector = new int[VecLen];
      for (int cnt = 0; cnt < VecLen; cnt++) {
        this.Vector[cnt] = ClipLow + Cats.rand.nextInt(ClipRange);
      }
    }
    /* **************************************************************************** */
    public void SeedRamp(int SampleSize) {
      this.VecLen = SampleSize;
      this.Radius = ClipRange / 1;
      //this.Radius = ClipRange * 100;
      this.Radius = Math.sqrt((ClipHigh * ClipHigh) * SampleSize);// length of diagonal from hypercube center to corner
      this.Amplitude = 1.0;
      this.Vector = new int[VecLen];
      for (int cnt = 0; cnt < VecLen; cnt++) {
        this.Vector[cnt] = cnt;
      }
    }
    /* **************************************************************************** */
    public double Score(Ind other) {
      int Shortest = Math.min(other.SampleLength, this.VecLen);
      double sumdeltasq = 0;
      int dcnt = 0;
      while (dcnt < Shortest) {
        double guess = other.Wav[dcnt];
        double val = this.Vector[dcnt];
        double delta = (val - guess);
        sumdeltasq += delta * delta;
        dcnt++;
      }
      while (dcnt < this.VecLen) {
        double val = this.Vector[dcnt];
        sumdeltasq += val * val;
        dcnt++;
      }
      double distance = Math.sqrt(sumdeltasq);
      double Score;
      if (distance < this.Radius) {
        Score = 1.0 - (distance * (1.0 / this.Radius));
      } else {
        Score = 0;
      }
      Score *= this.Amplitude;
      return Score;
    }
  }
}
