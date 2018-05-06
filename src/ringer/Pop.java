package ringer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

/**
 *
 * @author MultiTool
 */
public class Pop {
  public Org[] Forest;
  //public ArrayList<Org> Forest;

  Tester tester = null;//, tester_internal=nullptr;// crucible
  int GenCnt;
  double SurvivalRate = 0.2;//0.02;//0.05;//0.5;
  double MutRate = 0.2;//0.5;//0.3;//0.8//0.6;//0.99;//
  int MaxOrgGens = 10000;//10000;//1000000;//50;//
  int MaxRetries = 1;//4;//2;//16;
  int PopSize = -1;
  int OrgSize = -1;// snox here there be problems - org matrix size needs to be determined flexibly by pop.
  //PopStats StatPack = null;

  int EvoStagnationLimit = 1500;//16384;//3000;// 5000;//100;//75;//50;
  int NumSurvivors;
  double SumScores = 0, AvgTopDigi = 0.0;
  double AllTimeTopScore = 0.0;
  double CurrentTopScore = 0.0;
  /* ********************************************************************** */
  public Pop() {
  }
  /* ********************************************************************** */
  public Pop(int PopSize) {
    this.Forest = new Org[PopSize];
  }
  /* ********************************************************************** */
  public void Delete_Me() {
  }
  /* ********************************************************************** */
  void Assign_Params(int popsize0, int OrgSize0, Tester tester0, int MaxOrgGens0, int MaxRetries0, int EvoStagnationLimit0) {
    this.PopSize = popsize0;// tbd
    this.Forest = new Org[popsize0];
    this.OrgSize = OrgSize0;
    this.tester = tester0;
    this.MaxOrgGens = MaxOrgGens0;
    this.MaxRetries = MaxRetries0;
    this.EvoStagnationLimit = EvoStagnationLimit0;
    this.InitPop();
  }
  /* ********************************************************************** */
  void InitPop() {// Create and seed the population of creatures.
    Org org;
    int pcnt;
    this.Forest = new Org[this.PopSize];
    for (pcnt = 0; pcnt < this.PopSize; pcnt++) {
      org = Org.Abiogenate(this.OrgSize);
      Forest[pcnt] = org;
    }
    NumSurvivors = (int) (this.PopSize * SurvivalRate);
  }
  /* ********************************************************************** */
  void Evolve() {// evolve for generations
    for (int RetryCnt = 0; RetryCnt < MaxRetries; RetryCnt++) {
      this.Init_Evolution();
      double CurrentTopScoreLocal;
      AllTimeTopScore = 0.0;
      int AbortCnt = 0;
      System.out.printf("%nRetryCnt:%d%n", RetryCnt);
      for (GenCnt = 0; GenCnt < MaxOrgGens; GenCnt++) {
        this.Gen();
        CurrentTopScoreLocal = this.GetTopScore();
        if (CurrentTopScoreLocal >= 1.0) {
          System.out.printf("Maximized.%n");
          break;
        }
        if (AllTimeTopScore < CurrentTopScoreLocal) {
          AbortCnt = 0;
          AllTimeTopScore = CurrentTopScoreLocal;
        } else {
          AbortCnt++; // stopping condition: if best score hasn't improved in EvoStagnationLimit generations, bail.
          if (AbortCnt > EvoStagnationLimit) {
            System.out.printf("Stagnated.%n");
            break;
          }
        }
      }
      this.Print_Results();
      if (false) {
        for (int gcnt = 0; gcnt < 50; gcnt++) {
          this.Gen_No_Mutate();// coast, no mutations
        }
        this.Print_Results();
      }
      System.out.printf("Final GenCnt:%d%n", GenCnt);
      Org TopOrg = this.GetTopOrg();
      tester.Print_Org(TopOrg);
//      if (this.StatPack != null){tbd
//        this.StatPack.Score.Collect(TopOrg.Score[2]);
//        this.StatPack.FinalGen.Collect(GenCnt);
//      }
      //std::cin.getline(name,256);
    }
  }
  /* ********************************************************************** */
  void Gen() { // each generation
    this.Gen_No_Mutate();
    this.Mutate(MutRate, MutRate);
  }
  /* ********************************************************************** */
  void Gen_No_Mutate() { // call this by itself to 'coast', reproduce and winnow generations without mutation.
    Score_And_Sort();
    Collect_Stats();
    Birth_And_Death();
  }
  /* ********************************************************************** */
  void Score_And_Sort() {
    int popsize = this.Forest.length;
    Org candidate;
    tester.Generation_Start();
    for (int pcnt = 0; pcnt < popsize; pcnt++) {
      candidate = Forest[pcnt];
      //System.out.printf("tester.Test(candidate)0%n");
      tester.Test(candidate);
      //System.out.printf("tester.Test(candidate)1%n");
    }
    tester.Generation_Finish();
    Sort();
    //cout << "Score_And_Sort end" << "%n";
  }
  /* ********************************************************************** */
  void Collect_Stats() {
    Org TopOrg = this.GetTopOrg();
    double PrevTopScore = CurrentTopScore;
    CurrentTopScore = TopOrg.Score[0];
    double TopDigiScore = TopOrg.Score[1];
    SumScores += TopDigiScore;
    double ModelStateMag = TopOrg.ModelStateMag;

    //AvgTopDigi=SumScores/this.GenCnt;
    AvgTopDigi = (AvgTopDigi * 0.9) + (TopDigiScore * 0.1);
    //if (this.GenCnt % 1 == 0){
    if (this.CurrentTopScore != PrevTopScore) {
      System.out.printf("GenCnt:%4d, ", this.GenCnt);
      TopOrg.Print_Scores();
      System.out.printf("%n");
    }
  }
  /* ********************************************************************** */
  void Print_Results() {
    //System.out.printf("Print_Results%n");
    Org TopOrg = this.GetTopOrg();

    double TopScore = TopOrg.Score[0];
    double TopDigiScore = TopOrg.Score[1];
    AvgTopDigi = (AvgTopDigi * 0.9) + (TopDigiScore * 0.1);
    if (true) {
      System.out.printf("GenCnt:%4d, ", this.GenCnt);
      TopOrg.Print_Scores();
      System.out.printf("%n");
    }
  }
  /* ********************************************************************** */
  public Org CloneTopOrg() {// deliver copy of top org that will outlive this whole population instance.
    return this.GetTopOrg().Spawn();
  }
  /* ********************************************************************** */
  public Org GetTopOrg() {
    Org TopOrg = Forest[0];
    return TopOrg;
  }
  /* ********************************************************************** */
  double GetTopScore() {
    Org TopOrg = this.GetTopOrg();
    double TopScore = TopOrg.Score[0];
    return TopScore;
  }
  /* ********************************************************************** */
  void Init_Evolution() {// re-initialize the population genome without changing the tester or the test
    Org org;
    this.GenCnt = 0;
    SumScores = 0.0;
    AvgTopDigi = 0.0;
    AllTimeTopScore = 0.0;
    CurrentTopScore = 0.0;
    int pcnt, popsize = Forest.length;
    for (pcnt = 0; pcnt < popsize; pcnt++) {
      org = Forest[pcnt];
      org.Rand_Init();
    }
  }
  /* ********************************************************************** */
  void Clear() {// is it really necessary to be able to clear without just deleting the population?
    int siz, pcnt;
    siz = Forest.length;
    for (pcnt = 0; pcnt < siz; pcnt++) {
      Forest[pcnt].Delete_Me();//tbd, just re-init instead of deleting.  or get rid of Clear()
    }
    this.tester = null;// is this a good idea?
  }
  /* ********************************************************************** */
  double AvgBeast() {
    int siz = Forest.length;
    double sum = 0.0;
    for (int cnt = 0; cnt < siz; cnt++) {
      sum += Forest[cnt].Score[0];
    }
    sum /= (double) siz;
    return sum;
  }
  /* ********************************************************************** */
  void Sort() {
    //cout << "Sort begin" << "%n";
    Collections.shuffle(Arrays.asList(this.Forest));// shuffle to prevent bias in order when many scores are equal
    Arrays.sort(this.Forest, new Comparator<Org>() {
      @Override public int compare(Org I0, Org I1) {
        //return I1.Compare_Score(I0);
        return I0.Compare_Score(I1);// descending? 
      }
    });
    //cout << "Sort end" << "%n";
  }
  static boolean AscendingScore(Org b0, Org b1) {
    return b0.Compare_Score(b1) > 0;
  }
  static boolean DescendingScore(Org b0, Org b1) {
    return b1.Compare_Score(b0) > 0;
  }
  /* ********************************************************************** */
  void Birth_And_Death() {
    int siz = Forest.length;
    int topcnt, cnt;
    Org doomed, child, survivor;
    topcnt = 0;
    for (cnt = 0; cnt < NumSurvivors; cnt++) {
      survivor = Forest[cnt];
      survivor.Reset();
    }
    for (cnt = NumSurvivors; cnt < siz; cnt++) {
      doomed = Forest[cnt];
      doomed.Doomed = true;
      doomed.Delete_Me();
      child = Forest[topcnt].Spawn();// Whenever one dies, replace it with the child of another.
      Forest[cnt] = child;
      if (++topcnt >= NumSurvivors) {
        topcnt = 0;
      }
    }
  }
  /* ********************************************************************** */
  void Mutate(double Pop_MRate, double Org_MRate) {
    Org org;
    int LastOrg;
    int siz = this.Forest.length;
    LastOrg = siz - 1;
    for (int cnt = this.NumSurvivors; cnt < LastOrg; cnt++) {
      //if (frand()<Pop_MRate) {
      org = this.Forest[cnt];
      org.Mutate_Me(Org_MRate);
      //}
    }
    org = this.Forest[LastOrg];// very last mutant is 100% randomized, to introduce 'new blood'
    org.Rand_Init();
  }
}
