#ifndef TARGET_H_INCLUDED
#define TARGET_H_INCLUDED

#include "Base.h"

class Target;
typedef Target *TargetPtr;
class Target {
public:
  double Amplitude;
  double Radius;
  int VecLen;
  int *Vector;
  Target() {
  }
  /* **************************************************************************** */
  void Seed(int SampleSize) {
    this->VecLen = SampleSize;
    this->Radius = rand() % (ClipRange / 2);
    this->Amplitude = frand();
    this->Vector = new int[VecLen];
    for (int cnt = 0; cnt < VecLen; cnt++) {
      this->Vector[cnt] = ClipLow + rand() % ClipRange;
    }
  }
  /* **************************************************************************** */
  void SeedRamp(int SampleSize) {
    this->VecLen = SampleSize;
    this->Radius = ClipRange / 1;
    //this->Radius = ClipRange * 100;
    this->Radius = std::sqrt((ClipHigh * ClipHigh) * SampleSize);// length of diagonal from hypercube center to corner
    this->Amplitude = 1.0;
    this->Vector = new int[VecLen];
    for (int cnt = 0; cnt < VecLen; cnt++) {
      this->Vector[cnt] = cnt;
    }
  }
  /* **************************************************************************** */
  double Score(vector<int> *OtherWav) {
    int Shortest = std::min(OtherWav->size(), (size_t)this->VecLen);
    double sumdeltasq = 0;
    double ScaledWin = 0;
    int dcnt = 0;
    while (dcnt < Shortest) {
      double guess = OtherWav->at(dcnt);
      double val = this->Vector[dcnt];
      double delta = (val - guess);
      sumdeltasq += delta * delta;
      ScaledWin += 1.0 / (1.0 + delta); // range of 0.0 to 1.0 if delta is 0 to infinity
      dcnt++;
    }
    while (dcnt < this->VecLen) {
      double val = this->Vector[dcnt];
      sumdeltasq += val * val;// as if guess is zero for the rest
      ScaledWin += 1.0 / (1.0 + std::abs(val)); // range of 0.0 to 1.0 if delta is 0 to infinity
      dcnt++;
    }
    double distance = std::sqrt(sumdeltasq);
    double Score;
    if (distance < this->Radius) {
      Score = 1.0 - (distance * (1.0 / this->Radius));
    } else {
      Score = 0;
    }
    Score *= this->Amplitude;
    return Score;
  }
};
/* **************************************************************************** */
class TargetList : public std::vector<TargetPtr> {
/* **************************************************************************** */
public:
  /* **************************************************************************** */
  TargetList() {
  }
  /* **************************************************************************** */
  ~TargetList() {
    int NumTargets = this->size();
    for (int cnt = 0; cnt < NumTargets; cnt++) {
      TargetPtr tg = this->at(cnt);
      delete tg;
    }
    this->clear();// probably not needed
  }
  /* **************************************************************************** */
  void Seed(int NumTargets, int SampleSize) {
    for (int cnt = 0; cnt < NumTargets; cnt++) {
      TargetPtr tg = new Target();
      tg->Seed(SampleSize);
      this->push_back(tg);
    }
  }
  /* **************************************************************************** */
  void SeedRamp(int NumTargets, int SampleSize) {
    for (int cnt = 0; cnt < NumTargets; cnt++) {
      TargetPtr tg = new Target();
      tg->SeedRamp(SampleSize);
      this->push_back(tg);
    }
  }
  /* **************************************************************************** */
  double Compare(vector<int> *OtherWav) {//vector<int> Wav
    double Score = 0.0;
    double OneScore;
    for (int cnt = 0; cnt < this->size(); cnt++) {
      TargetPtr tg = this->at(cnt);
      OneScore = tg->Score(OtherWav);
      Score = std::max(Score, OneScore);
    }
    return Score;
  }
};


#endif // TARGET_H_INCLUDED
