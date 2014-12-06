#ifndef POP_H_INCLUDED
#define POP_H_INCLUDED

#include "Org.h"
#include "Lugar.h"
#include "Target.h"

//#define popmax 1000
//#define popmax 300
#define popmax 100


/* ********************************************************************** */
class Pop;
typedef Pop *PopPtr;
class Pop {
public:
  size_t popsz;
  LugarVec forestv;
  OrgVec ScoreDexv; // for sorting
  typedef struct ScorePair { double Score[2]; };
  std::vector<ScorePair> ScoreBuf;// for recording scores even after some creatures are dead
  uint32_t MaxNeuroGens = 2000;
  uint64_t BioGenCnt;
  double SurvivalRate = 0.5;

  TargetList *targets = NULL;

  /* ********************************************************************** */
  Pop() : Pop(popmax) {
    targets = new TargetList();
    int MaxSize = 100;
    targets->SeedRamp(1, MaxSize);// Ind.MaxSize);
  }
  /* ********************************************************************** */
  Pop(size_t popsize) {
    LugarPtr lugar;
    Org *org;
    size_t pcnt;
    this->popsz = popsize;
    forestv.resize(popsize);
    ScoreDexv.resize(popsize);
    ScoreBuf.resize(popsize);
    BioGenCnt = 0;
    for (pcnt=0; pcnt<popsize; pcnt++) {
      lugar = new Lugar();
      org = Org::Abiogenate(); lugar->Attach_Tenant(org);
      forestv.at(pcnt) = lugar;
      ScoreDexv.at(pcnt) = org;
    }
  }
  /* ********************************************************************** */
  ~Pop() {
    size_t siz, pcnt;
    siz = forestv.size();
    for (pcnt=0; pcnt<siz; pcnt++) {
      delete forestv.at(pcnt);
    }
  }
  /* ********************************************************************** */
  void Fire_Cycle() {
    LugarPtr lugar;
    OrgPtr org;
    size_t siz = this->forestv.size();
    for (size_t cnt=0; cnt<siz; cnt++) {
      lugar=this->forestv.at(cnt);
      org=lugar->tenant;
      if(false) {
        org->Fire_Cycle(targets);
      } else {
        org->RunTest(targets);
      }
    }
  }
  /* ********************************************************************** */
  void Clear_Scores() {
    OrgPtr org;
    size_t siz = this->ScoreDexv.size();
    for (size_t cnt=0; cnt<siz; cnt++) {
      org=this->ScoreDexv.at(cnt);
      org->Clear_Score();
    }
  }
  /* ********************************************************************** */
  void Calculate_Scores() {
    OrgPtr org;
    size_t siz = this->ScoreDexv.size();
    for (size_t cnt=0; cnt<siz; cnt++) {
      org=this->ScoreDexv.at(cnt);
      org->Calculate_Score();
    }
  }
  /* ********************************************************************** */
  void Calculate_Score_And_Success(double Margin) {
    OrgPtr org;
    size_t siz = this->ScoreDexv.size();
    for (size_t cnt=0; cnt<siz; cnt++) {
      org=this->ScoreDexv.at(cnt);
      org->Calculate_Score_And_Success(Margin);
    }
  }
  /* ********************************************************************** */
  void Gen() { // new generation
    uint32_t popsize = this->forestv.size();
    int Fire_Test_Cycles = 50;
    int Test_Len = 10;
    int Start_Testing = Fire_Test_Cycles-Test_Len;
    //MaxNeuroGens
    LugarPtr lugar;
    Org *parent, *child;
    uint32_t pcnt;
    LugarPtr place;

    int RTerm;
    if (false) {
      RTerm = rand()%7;// advance the feed randomly so the Orgs will have to listen for the phase to guess right
    } else {
      RTerm = BioGenCnt%7;// advance the feed arbitrarily each time so the Orgs will have to listen for the phase to guess right
    }
    this->Clear_Scores();
    for (int fcnt=0; fcnt<Fire_Test_Cycles; fcnt++) {
      this->Fire_Cycle();
      if (Start_Testing <= fcnt) {
        //this->Calculate_Score_And_Success(0.25);// must be within .25 of right answer (max dist is 1.0, any dist >=0.5 is digitally wrong)
        this->Calculate_Score_And_Success(0.45);// must be within .45 of right answer (max dist is 1.0, any dist >=0.5 is digitally wrong)
      }
    }

    /* First score and sort the parents, then create children. */
    Sort();
    Record_Scores();
    OrgPtr bestbeast = ScoreDexv[0];
    OrgPtr leastbeast = ScoreDexv[this->popsz-2];
    //double avgbeast = AvgBeastScore(1.0);
    Birth_And_Death(SurvivalRate);
    BioGenCnt++;
  }
  /* ********************************************************************** */
  double AvgBeastScore(double TopPercent) {
    size_t siz = ScoreDexv.size();
    size_t Limit = (TopPercent*(double)siz);
    double sum = 0.0;
    //for (int cnt=0; cnt<siz; cnt++) {
    for (int cnt=0; cnt<Limit; cnt++) {
      //sum += ScoreDexv[cnt]->Score[0];
      sum += ScoreDexv[cnt]->Score[1];
    }
    //sum /= (double)siz;
    sum /= (double)Limit;
    return sum;
  }
  /* ********************************************************************** */
  uint32_t GetMaxSize() {
    uint32_t MaxSize=0, SumSize=0, AvgSize=0;
    uint32_t popsize = this->forestv.size();
    uint32_t numsamples;
    LugarPtr lugar;
    OrgPtr org;
    uint32_t pcnt;
    LugarPtr place;
    for (pcnt=0; pcnt<popsize; pcnt++) {
      lugar = forestv[pcnt];
      org = lugar->tenant;
      numsamples=org->Wav.size();
      if (MaxSize<numsamples) {
        MaxSize= numsamples;
      }
      SumSize += numsamples;
    }
    return MaxSize;
  }
  /* ********************************************************************** */
  static bool AscendingScore(OrgPtr b0, OrgPtr b1) {
    return b0->Compare_Score(b1) > 0;
  }
  static bool DescendingScore(OrgPtr b0, OrgPtr b1) {
    return b1->Compare_Score(b0) > 0;
  }
  void Sort() {
    std::random_shuffle(ScoreDexv.begin(), ScoreDexv.end());
    std::sort (ScoreDexv.begin(), ScoreDexv.end(), DescendingScore);
  }
  /* ********************************************************************** */
  void Birth_And_Death(double SurvivalRate) {
    size_t siz = ScoreDexv.size();
    size_t NumSurvivors = siz * SurvivalRate;
    size_t topcnt, cnt;
    LugarPtr home;
    OrgPtr doomed, parent, child;
    topcnt = 0;
    for (cnt=NumSurvivors; cnt<siz; cnt++) {
      doomed = ScoreDexv[cnt]; home = doomed->home;
      parent = ScoreDexv[topcnt];
      child = parent->Spawn();
      home->Attach_Tenant(child); ScoreDexv[cnt] = child;
      delete doomed;
      topcnt++;
      if (topcnt>=NumSurvivors) {topcnt=0;}
    }
  }
  /* ********************************************************************** */
  void Record_Scores() {
    size_t siz = ScoreDexv.size();
    double *Dest, *Src;
    for (int cnt=0; cnt<siz; cnt++) {
      Src = ScoreDexv[cnt]->Score;
      Dest = ScoreBuf.at(cnt).Score;
      Dest[0] = Src[0];
      Dest[1] = Src[1];
    }
  }
  /* ********************************************************************** */
  void Print_Sorted_Scores() {
    double *Score;
    size_t siz = ScoreDexv.size();
    size_t cnt;
    for (cnt=0; cnt<siz; cnt++) {
      Score = ScoreBuf.at(cnt).Score;
      bugprintf(" %03li, Score:%f, %f\n", cnt, Score[0], Score[1]);
      if (Score[0]!=0.0) {
        bool nop = true;
      }
    }
  }
  /* ********************************************************************** */
  void Mutate(double Pop_MRate, double Org_MRate) {
    OrgPtr org;
    size_t siz = this->ScoreDexv.size();
    size_t Num2Mutate = (Pop_MRate * (double)siz);
    size_t FirstOrg, LastOrg;
    LastOrg = siz-1;
    FirstOrg = siz-Num2Mutate;
    for (int cnt=LastOrg-1; cnt>=FirstOrg; cnt--) {
      org = this->ScoreDexv[cnt];// lugar->tenant;
      org->Mutate_Me(Org_MRate);
    }
    org = this->ScoreDexv[LastOrg];// very last mutant is 100% randomized, to introduce 'new blood'
    org->Rand_Init();
  }
  /* ********************************************************************** */
  void Mutate2(double Pop_MRate, double Org_MRate) {
    OrgPtr org;
    size_t FirstOrg, LastOrg;
    FirstOrg = 10;// experiment: preserve the best performers intact
    size_t siz = this->ScoreDexv.size(); LastOrg = siz-1;
    for (int cnt=FirstOrg; cnt<LastOrg; cnt++) {
      if (frand()<Pop_MRate) {
        org = this->ScoreDexv[cnt];// lugar->tenant;
        org->Mutate_Me(Org_MRate);
      }
    }
    org = this->ScoreDexv[LastOrg];// very last mutant is 100% randomized, to introduce 'new blood'
    org->Rand_Init();
  }
  /* ********************************************************************** */
  void Compile_Me() {
    size_t siz = this->forestv.size();
    for (size_t cnt=0; cnt<siz; cnt++) {
      LugarPtr lugar = this->forestv.at(cnt);
      OrgPtr org = lugar->tenant;
      org->Compile_Me();
    }
  }
  /* ********************************************************************** */
  static bool AscendingUid(UidType b0, UidType b1) {
    return (b0 < b1);
  }
};

#endif // POP_H_INCLUDED
