package ringer;

/**
 *
 * @author MultiTool
 */
public class Org {
  static int NumScores = 3;
  public Wave wave = null;
  public double[] Score;
  boolean Doomed = false;
  double ModelStateMag;
  /* ********************************************************************** */
  public Org(int VSize) {
    this.wave = new Wave(VSize);
    this.Score = new double[NumScores];
  }
  /* ********************************************************************** */
  public void Delete_Me() {
    this.wave.Delete_Me();
    this.wave = null;
  }
  /* ********************************************************************** */
  static Org Abiogenate(int Wdt) {
    Org child = new Org(Wdt);
    child.Rand_Init();
    return child;
  }
  /* ********************************************************************** */
  Org Spawn() {
    Org child = new Org(this.wave.Vect.length);
    child.Copy_From(this);
    return child;
  }
  /* ********************************************************************** */
  public void Copy_From(Org donor){
    this.wave.Copy_From(donor.wave);
  }
  /* ********************************************************************** */
  void Rand_Init() {
    this.wave.Rand_Init();
  }
  /* ********************************************************************** */
  void Mutate_Me(double MRate) {
    this.wave.Mutate_Me(MRate);
  }
  /* ********************************************************************** */
  void Mutate_Me() {
    double MRate = 0.3;
    this.Mutate_Me(MRate);
  }
  /* ********************************************************************** */
  void Reset() {
    Doomed = false;
    ModelStateMag = 0.0;
    for (int cnt = 0; cnt < NumScores; cnt++) {
      this.Score[cnt] = 0;
    }
  }
  /* ********************************************************************** */
  int Compare_Score(Org other) {
    int cnt = 0;
    while (cnt < NumScores) {
      if (this.Score[cnt] < other.Score[cnt]) {
        return 1;
      }
      if (this.Score[cnt] > other.Score[cnt]) {
        return -1;
      }
      cnt++;
    }
    return 0;
  }
  /* ********************************************************************** */
  void Print_Scores() {
    for (int cnt = 0; cnt < NumScores; cnt++) {
      System.out.printf("Sc%d:%1.10f, ", cnt, this.Score[cnt]);
    }
  }
}
