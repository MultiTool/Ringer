package ringer;

/**
 *
 * @author MultiTool
 */
public class Wave {
  /* ********************************************************************** */
  public double[] Vect;
  /* ********************************************************************** */
  public Wave(int VSize) {
    this.Vect = new double[VSize];
  }
  /* ********************************************************************** */
  public void Delete_Me() {
    this.Vect = null;
  }
  /* ********************************************************************** */
  public int Get_Length() {
    return this.Vect.length;
  }
  /* ********************************************************************** */
  void Rand_Init() {
    int ln = this.Vect.length;
    double amp = 4.0;
    double Range = amp * 2.0;
    double val;
    for (int cnt = 0; cnt < ln; cnt++) {
      val = this.Vect[cnt];
      val = (Base.rand.nextDouble() * Range - amp);// range -1 to +1
      this.Vect[cnt] = val;
    }
  }
  /* ********************************************************************** */
  void Clip_Me(double Limit) {
    int ln = this.Vect.length;
    for (int cnt=0; cnt<ln; cnt++) {
      if (this.Vect[cnt]>Limit){ this.Vect[cnt]=Limit; }
      else if (this.Vect[cnt]<-Limit){ this.Vect[cnt]=-Limit; }
    }
  }
  /* ********************************************************************** */
  void Mutate_Me(double MRate) {
    double val, amp = 2.0;//0.3;
    for (int cnt = 0; cnt < this.Vect.length; cnt++) {
      // need to randomize 
      if (Base.rand.nextDouble() < MRate) {
        val = this.Vect[cnt];
        val += (Base.rand.nextDouble() * 2.0 - 1.0) * amp;// drift mutation, -amp to +amp
        //val = (Base.rand.nextDouble()*2.0-1.0)*amp;// jump mutation. -amp to +amp
        this.Vect[cnt] = val;
      }
    }
  }
  /* ********************************************************************** */
  public void Copy_From(Wave donor) {
    if (true) {
      this.Vect = new double[donor.Vect.length];
      System.arraycopy(donor.Vect, 0, this.Vect, 0, donor.Vect.length);
    } else {
      int len = Math.min(donor.Vect.length, this.Vect.length);
      System.arraycopy(donor.Vect, 0, this.Vect, 0, len);
    }
  }
  /* ********************************************************************** */
  double SumOfSquares() {
    int ln = this.Vect.length;
    double val, SumSq = 0.0;
    for (int cnt = 0; cnt < ln; cnt++) {
      val = this.Vect[cnt];
      SumSq += val * val;
    }
    return SumSq;
  }
  /* ********************************************************************** */
  double Magnitude() {
    return Math.sqrt(this.SumOfSquares());
  }
  /* ********************************************************************** */
  double MaxLen() {// assuming the range of every number is -1 to +1
    return Math.sqrt(this.Vect.length);
  }
  /* ********************************************************************** */
  double GetWaveEnergy() {// crude wave energy measure
    int ln = this.Vect.length;
    if (ln <= 1) {
      return 0.0;
    }// no data, no energy
    double prevamp, delta;// energy is defined here as the sum of all changes
    double EnergySum = 0, amp = this.Vect[0];
    for (int cnt = 1; cnt < ln; cnt++) {
      prevamp = amp;
      amp = this.Vect[cnt];
      delta = amp - prevamp;
      EnergySum += Math.abs(delta);// EnergySum += (delta * delta);// or this?
    }
    return EnergySum;
  }
}
