#ifndef ORG_H_INCLUDED
#define ORG_H_INCLUDED

#include <cstring>
#include "Base.h"
#include "Target.h"

#define genomelen 1000
#define genomelen2 4
#define baseamp INT_MAX

/* ********************************************************************** */
class Genome {
public:
  class Org *mine;
};
const int NodeNumLimit = 100;
/* ********************************************************************** */
class Org;
typedef Org *OrgPtr;
typedef std::vector<Org*> OrgVec;
class Org {
public:
  const static int NumScores = 2;
  double Score[NumScores];
  double Errors;
  struct Lugar *home;// my location
  bool Invicto;//Undefeated

  //public static class Ind implements IDeletable {// from Java, same as Org but a waveform
  const static int MaxSize = 400;
  //static final int MinSize = 100;
  //static final int MaxSize = 13;
  const static int MinSize = MaxSize / 2;
  const static int SizeRange = MaxSize - MinSize;
  int SampleLength;
  //int *Wav;
  //vector<int> Wav(N);
  //vector<int> *Wav;
  vector<int> Wav;// http://lemire.me/blog/archives/2012/06/20/do-not-waste-time-with-stl-vectors/

  /* ********************************************************************** */
  Org() {
    //this->Wav = new vector<int>(MaxSize);
    this->Wav.reserve(MaxSize);
    for (int cnt=0; cnt<NumScores; cnt++) {
      this->Score[cnt] = 0.0;
    }
    this->home = NULL;
    this->Invicto=false;
  }
  /* ********************************************************************** */
  ~Org() {
    //freesafe(Wav); Wav = NULL;
    //delete (this->Wav); Wav = NULL;
  }
  /* ********************************************************************** */
  static OrgPtr Abiogenate() {
    OrgPtr org = new Org();
    org->Mutate_Me(1.0);// 100% mutated, no inherited genetic info
    return org;
  }
  /* ********************************************************************** */
  void Fire_Cycle() {
  }
  /* ********************************************************************** */
  void Rand_Init() {
    this->Mutate_Me(1.0);
  }
  /* ********************************************************************** */
  void Random_Increase(double dupequota) {
  }
  /* ********************************************************************** */
  void Mutate_Me(double MRate) {
  }
  /* ********************************************************************** */
  OrgPtr Spawn() {
    OrgPtr child;
    child = new Org();

    //child->Wav = allocsafe(int, MaxSize);
    //child->Wav = new vector<int>(MaxSize);
    //child->Wav.reserve(MaxSize);

    //System.arraycopy(this->Wav, 0, child->Wav, 0, this->MaxSize);
    // std::copy(this->Wav, this->Wav + this->MaxSize, child->Wav);//SampleLength
    child->Wav = this->Wav;// does this copy values, or just the reference?

    //std::memcpy(child->Wav, this->Wav, this->MaxSize);//SampleLength
    child->SampleLength = this->SampleLength;
    //child->Score = this->Score;

    return child;
  }
  /* ********************************************************************** */
  void Uncompile_Me() {
  }
  /* ********************************************************************** */
  void Compile_Me() {
  }
  /* ********************************************************************** */
  void Clear_Score() {
    this->Invicto = true;
    for (int cnt=0; cnt<NumScores; cnt++) {
      this->Score[cnt]=0.0;
    }
  }
  /* ********************************************************************** */
  void Oneify_Score() { // this is for accumulating scores by multiplication: Score *= subscore
    for (int cnt=0; cnt<NumScores; cnt++) {
      this->Score[cnt]=1.0;
    }
  }
  /* ********************************************************************** */
  void Rescale_Score(double Factor) {
    for (int cnt=0; cnt<NumScores; cnt++) {
      this->Score[cnt]*=Factor;
    }
  }
  /* ********************************************************************** */
  bool Calculate_Success(double Margin) {
    bool SuccessTemp = true;
    return SuccessTemp;
  }
  /* ********************************************************************** */
  void Calculate_Score_And_Success(double Margin) {
    bool SuccessTemp = true;
    double Real, Guessed, Error, Temp0, Temp1, SumScore0, SumScore1;
    SumScore0 = 1.0; SumScore1 = 0.0;
    this->Score[0] += SumScore0;
    this->Score[1] += SumScore1;
    this->Invicto = this->Invicto && SuccessTemp;
  }
  /* ********************************************************************** */
  void Calculate_Score() {
    double Real, Guessed, Temp0, Temp1, SumScore0, SumScore1;
    SumScore0 = 1.0; SumScore1 = 0.0;
    this->Score[0] += SumScore0;
    this->Score[1] += SumScore1;
  }
  /* ********************************************************************** */
  void Print_Me() {
    bugprintf("Org\n");
    Print_Score();
    size_t siz = this->Wav.size();
  }
  /* ********************************************************************** */
  void Print_Jacks() {
  }
  /* ********************************************************************** */
  void Print_Score() {
    bugprintf(" Score:%f, %f\n", this->Score[0], this->Score[1]);
  }
  /* ********************************************************************** */
  int Compare_Score(OrgPtr other) {
    int cnt = 0;
    double *ScoreMe, *ScoreYou;
    ScoreMe=this->Score; ScoreYou=other->Score;
    while (cnt<NumScores) {
      if (ScoreMe[cnt]<ScoreYou[cnt]) {return 1;}
      if (ScoreMe[cnt]>ScoreYou[cnt]) {return -1;}
      cnt++;
    }
    return 0;
  }
  // Java begins here
  /* **************************************************************************** */
  void Seed(int SampleSize) {
    SampleLength = SampleSize;
    Wav = vector<int>(MaxSize);
    for (int cnt = 0; cnt < SampleLength; cnt++) {
      this->Wav[cnt] = ClipLow + rand() % ClipRange;// Cats.rand.nextInt(ClipRange);
    }
  }
  /* **************************************************************************** */
  void Mutate(double Rate) {
    for (int cnt = 0; cnt < this->SampleLength; cnt++) {
      if (frand() < Rate) {
        switch (0) {
        case 0: {
          double mid = this->Wav[cnt];
          if (true) {
            mid = (mid - ClipLow) / ClipRange;// 0 to 1
            mid = (mid * 2.0) - 1.0;// -1.0 to +1.0
            mid = Cats::CurveMap(-1.0, mid, 1.0);
            mid = (mid + 1.0) / 2.0;
            this->Wav[cnt] = (int) (ClipLow + (mid * ClipRange));
          } else {
            mid = Cats::CurveMap(ClipLow, mid, ClipHigh);
            this->Wav[cnt] = (int) mid;
            //this->Wav[cnt] = (int) (ClipLow + (mid * ClipRange));
          }
        }
        case 1: {
          this->Wav[cnt] += -100 + Cats::rand(200);
          if (this->Wav[cnt] < ClipLow) {
            this->Wav[cnt] += ClipRange;
          }
          if (ClipHigh <= this->Wav[cnt]) {
            this->Wav[cnt] -= ClipRange;
          }
          break;
        }
        case 2: {
          this->Wav[cnt] = Cats::rand(ClipRange) - ClipLow;
          break;
        }
        }
      }
    }
    //ModifyLength(0.01);
    //ModifyLength(0.1);
  }
  /* **************************************************************************** */
  void Freak() {
    //this.SampleLength = MinSize + Cats.rand.nextInt(SizeRange);
    Wav = vector<int>(MaxSize);
    for (int cnt = 0; cnt < MaxSize; cnt++) {
      Wav[cnt] = ClipLow + Cats::rand(ClipRange);
    }
  }
  /* **************************************************************************** */
  void ModifyLength(double Rate) {
    if (true) {
      if (Cats::frand() < Rate) {
        switch (Cats::rand(2)) {
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
  void RunTest(TargetList tl) {// this must be connected to crucible
    if (true) {
      this->Score[0] = tl.Compare(&(this->Wav));
      // Compare(int *OtherWav, int SampleLength)
    } else if (true) {// select for ramp
      this->Score[0] = 0;
      this->Errors = 0;
      double sumsq = 0;
      double Value = 0;
      for (int cnt = 0; cnt < this->SampleLength; cnt++) {
        int val = this->Wav[cnt];
        double delta = std::abs(val - cnt);
        Value += 1.0 / (1.0 + delta);
        double deltasq = delta * delta;
        sumsq += deltasq;
        this->Errors += delta;
        //this->Errors += deltasq;
        // if (delta == 0) { delta = 0.00001; } Score += 1.0 / delta;
      }
      //this->Errors = Math.sqrt(this->Errors);
      //this->Errors = (this->Errors + 1.0) / (double) SampleLength;
      //Score = 1.0 / Errors;
      if (this->Errors < 0.01) {
        boolean nop = true;
      }
      this->Score[0] = Value;
    }
  }
};

#endif // ORG_H_INCLUDED
