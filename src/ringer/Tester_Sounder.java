package ringer;

import java.util.ArrayList;
import java.util.Arrays;

// Pack outputs from all creatures of a generation into a single long vector to squeeze out latencies of hardware IO.

/* ********************************************************************** */
class Tester_Sounder extends Tester {// evolve to create a vector with max wave energy (max changes, as we define it)
  int PopSize, OrgCnt, OrgVecLen, IOVecLen;
  //ArrayList<Org> OrgList;
  Org[] OrgList;
  //ArrayList<Double> OutVec, InVec;
  double[] OutVec, InVec;
  /* ********************************************************************** */
  Tester_Sounder() {
    this.OrgCnt = 0;
    //this.OrgList = new ArrayList<Org>();
    this.OrgList = new Org[0];
  }
  /* ********************************************************************** */
  @Override public void Delete_Me() {
    this.OrgList = null;
  }
  /* ********************************************************************** */
  @Override public void Assign_Pop_Size(int PopSize0, int OrgVecLen0) {
    this.PopSize = PopSize0;
    this.OrgVecLen = OrgVecLen0;
    this.IOVecLen = this.PopSize * this.OrgVecLen;
    //this.OrgList.ensureCapacity(this.PopSize);
    //for (int cnt = 0; cnt < this.PopSize; cnt++) { this.OrgList.add(null); }
    this.OrgList = new Org[this.PopSize];
    this.InVec = new double[this.IOVecLen];
    this.OutVec = new double[this.IOVecLen];
  }
  /* ********************************************************************** */
  @Override public void Generation_Start() {// once per generation
    this.OrgCnt = 0;
  }
  /* ********************************************************************** */
  @Override public void Generation_Finish() {// once per generation
    {// simulate that the outvec has looped through our object and returned as invec.
      //std::copy_n(this.OutVec.begin(), this.IOVecLen, this.InVec.begin());
      System.arraycopy(this.OutVec, 0, this.InVec, 0, this.IOVecLen);// tbd
      // to do: we have to simulate latency too, and adjust for that.
    }
    Wave buf = new Wave(this.OrgVecLen);
    double Energy;
    int SoundDex;
    Org candidate;
    for (int OrgDex = 0; OrgDex < this.OrgCnt; OrgDex++) {
      //candidate = OrgList.get(OrgDex);
      candidate = OrgList[OrgDex];
      SoundDex = OrgDex * this.OrgVecLen;
      //std::copy_n(this.InVec.begin() + SoundDex, this.OrgVecLen, buf.ray.begin());
      System.arraycopy(this.InVec, SoundDex, buf.Vect, 0, this.OrgVecLen);
      Energy = buf.GetWaveEnergy() / ((double) buf.Get_Length() - 1);// len-1 because we only measure differences between numbers, always one less.
      Energy *= 0.5;
      candidate.Score[0] = Energy;// to do: find a scoring system whose max is 1.0
    }
    Arrays.fill(this.OutVec, 0);
    Arrays.fill(this.InVec, 0);
    this.OrgCnt = 0;
  }
  /* ********************************************************************** */
  @Override public void Test(Org candidate) {
    //OrgList.set(this.OrgCnt, candidate);
    OrgList[this.OrgCnt] = candidate;
    candidate.wave.Clip_Me(1.0);// hacky. here we modify the Org's genome in a test.
    int SoundDex = this.OrgCnt * this.OrgVecLen;
    System.arraycopy(candidate.wave.Vect, 0, this.OutVec, SoundDex, this.OrgVecLen);
    //std::copy_n(candidate.ray.begin(), this.OrgVecLen, this.OutVec.begin() + SoundDex);
    this.OrgCnt++;
  }
  /* ********************************************************************** */
  @Override public void Print_Me() {
  }
  /* ********************************************************************** */
  @Override public void Print_Org(Org candidate) {
  }
};
