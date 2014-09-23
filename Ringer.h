// porting from Java

/*
 Note: There are odd code patterns here, put only only to make porting to C++ or D language easier.
 */
class Ringer {
public:
#if DoRinger
  static void main(String[] args) {
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
  interface IDeletable {
    void Delete();// virtual
  }
  /* **************************************************************************** */
  static class Population implements IDeletable {
    public int MyPopSize;
    public Ind[] Peeps;
    Audio aud = NULL;
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
    void NextGen2(double Fraction) {
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
    void NextGen(double Fraction) {
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
    void Mutate(double PopRate, double IndRate) {
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
    void Delete() override {
      for (int cnt = 0; cnt < this.MyPopSize; cnt++) {
        Peeps[cnt].Delete();
      }
      // free this.Peeps;
      this.Peeps = NULL;
    }
    /* **************************************************************************** */
    void RunTestAudio() {
      for (int cnt = 0; cnt < MyPopSize; cnt++) {// this must be connected to crucible
        Ind Peep = this.Peeps[cnt];
        int[] Wav = Peep.Wav;
        Peep.Score = aud.ScoreTest(Wav, Wav.length);
        boolean nop = true;
      }
    }
    /* **************************************************************************** */
    void RunTest(TargetList tl) {
      for (int cnt = 0; cnt < MyPopSize; cnt++) {// this must be connected to crucible
        this.Peeps[cnt].RunTest(tl);
      }
    }
    /* **************************************************************************** */
    double GetPromedioGanancias() {
      double Sum = 0.0;
      for (int cnt = 0; cnt < this.MyPopSize; cnt++) {
        Sum += Peeps[cnt].Score;
      }
      Sum /= this.MyPopSize;
      return Sum;
    }
    /* **************************************************************************** */
    double GetElMejorCuenta() {
      return GetElMejor().Score;
    }
    /* **************************************************************************** */
    Ind GetElMejor() {
      return Peeps[MyPopSize - 1];
    }
  }
  /* **************************************************************************** */
  static class Socket implements IDeletable {// maybe use this
    public Ind Peep;
    @Override
    public void Delete() {
    }
  }
  /* **************************************************************************** */
  static void Test() {
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
#endif
};



#endif // DoRinger
