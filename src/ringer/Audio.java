/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ringer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import javax.sound.sampled.*;

/**
 *
 * @author MultiTool Cannibalized from
 * http://www.java-tips.org/java-se-tips/javax.sound/capturing-audio-with-java-sound-api.html
 *
 * Note: There are odd code patterns here, put only to make porting to C++ or D
 * language easier.
 *
 */
public class Audio {
  protected boolean running;
  public int BytesPerSample = 2;// 4
  public int BitsPerSample = BytesPerSample * Byte.SIZE;
  public int MaxAmp = (1 << (BitsPerSample - 1)) - 1;
//  float SampleRate = 44100;//8000;
  int SampleRate = 44100;//8000;
  int bufferSize;
  byte buffer[];
  /* **************************************************************************** */
  public void Test() { // http://www.jsresources.org/faq_audio.html
    int NumSamples = 20000;
    int SoundInts0[] = new int[NumSamples];
    int SoundInts1[] = new int[NumSamples];
    //SawTooth(SoundInts0, NumSamples, 500);//120);
    Sine(SoundInts0, NumSamples, 300);//120);
    //this.PrintSample(WaveSpeak, WaveSpeak.length);

    int a0 = (1 << (BitsPerSample - 1)) - 1;
    int a1 = ((1 << BitsPerSample) >> 1) - 1;

    int blah = Integer.MAX_VALUE;
    int a2 = ((1 << (32 - 1)) - 1);

    Feedback(SoundInts0, SoundInts1);
  }
  /* **************************************************************************** */
  private void Feedback(int WaveSpeak[], int WaveListen[]) {
    byte SpeakBytes[];
    SpeakBytes = Ints2Bytes(WaveSpeak);
    try {
      // speak first in a separate thread, listen second in main thread
      {
        // PLAY *****************************************************************************************
        final AudioFormat SpeakFormat = GetFormat();
        final SourceDataLine SpeakLine;
        DataLine.Info SpeakInfo;
        {
          SpeakInfo = new DataLine.Info(SourceDataLine.class, SpeakFormat);
          SpeakLine = (SourceDataLine) AudioSystem.getLine(SpeakInfo);
          SpeakLine.open(SpeakFormat);
          SpeakLine.start();
        }
        Runnable SpeakerRun = new Runnable() { // player
          @Override public void run() {
            SpeakLine.write(SpeakBytes, 0, SpeakBytes.length); // blocks thread
            SpeakLine.drain();// blocks thread
            SpeakLine.close();
            running = false;
          }
        };
        Thread PlayThread = new Thread(SpeakerRun);

        this.running = true;
//      PlayThread.setPriority(Thread.MAX_PRIORITY); PlayThread.setDaemon(true);
        PlayThread.start();
      }
      {
        // RECORD *****************************************************************************************
        final AudioFormat ListenFormat;
        final TargetDataLine ListenLine;
        DataLine.Info ListenInfo;
        int[] WavInt;
        ByteArrayOutputStream out;
        out = new ByteArrayOutputStream();
        {
          ListenFormat = GetFormat();
          ListenInfo = new DataLine.Info(TargetDataLine.class, ListenFormat);
          ListenLine = (TargetDataLine) AudioSystem.getLine(ListenInfo);
          ListenLine.open(ListenFormat);
          ListenLine.start();
        }
        int BufferSize = WaveListen.length * ListenFormat.getFrameSize();
        byte ListenBytes[] = new byte[BufferSize];

        /*
        should reading end upon a condition, dynamically, or at a predetermined time?
        1. measure latency between speaker and recording.
        2. resize the listing array +latency, and maybe +safety margin.
        otherwise, there isn't any way to tell if the sound has finished yet, right? 
        
        so create latency measure first.
         */
        int HeardCount, ListenDex = 0;
        try {
          while (this.running) {
            System.out.println("ReadingLine");
            HeardCount = ListenLine.read(ListenBytes, 0, ListenBytes.length);
            if (HeardCount > 0) {
              out.write(ListenBytes, 0, HeardCount);
              WavInt = Bytes2Ints(ListenBytes, HeardCount);
              for (int FrameCnt = 0; FrameCnt < HeardCount; FrameCnt++) {
                WaveListen[ListenDex++] = WavInt[FrameCnt];
              }
            }
          }
          out.close();
        } catch (IOException e) {
          System.err.println("I/O problems: " + e); //System.exit(-1);
        }
      }
    } catch (LineUnavailableException e) {
      System.err.println("Line unavailable: " + e); // System.exit(-4);
      e.printStackTrace();
    }

    this.PrintSample(WaveListen, WaveListen.length);
  }
  /* **************************************************************************** */
  private AudioFormat GetFormat() {
    int Channels = 1;
    boolean Signed = true;
    boolean BigEndian = true;
    return new AudioFormat(SampleRate, BitsPerSample, Channels, Signed, BigEndian);
  }
  /* **************************************************************************** */
  public byte[] Ints2Bytes(int Ints[]) { // unfinished attempt at generic mapping to byte array
    int MaxShift = BitsPerSample - Byte.SIZE;
    int NumSamples = Ints.length;
    byte OutBytes[] = new byte[NumSamples * BytesPerSample];

    int AmpInt;
    int bufcnt = 0;
    for (int SampleCnt = 0; SampleCnt < NumSamples; SampleCnt++) {
      AmpInt = Ints[SampleCnt];
      for (int BitCnt = MaxShift; BitCnt >= 0; BitCnt -= Byte.SIZE) {// big endian
        OutBytes[bufcnt] = (byte) ((AmpInt >> BitCnt) & 0xFF);// most significant byte to least significant byte
        bufcnt++;
      }
    }
    if (false) {
      for (int SampleCnt = 0; SampleCnt < NumSamples; SampleCnt++) {// little endian, unfinished
        AmpInt = Ints[SampleCnt];
        for (int BitCnt = 0; BitCnt < BitsPerSample; BitCnt += Byte.SIZE) {// big endian
          OutBytes[bufcnt] = (byte) ((AmpInt >> BitCnt) & 0xFF);// least significant byte to most significant byte
          bufcnt++;
        }
      }
    }
    return OutBytes;
  }
  /* **************************************************************************** */
  public int[] Bytes2Ints(byte[] ByteArray, int ByteRayLen) {
    //int ByteRayLen = ByteArray.length;
    int[] audio = null;
    int LastByteDex = BytesPerSample - 1;
    int NumBytes = ByteArray.length;
    int IntRayLen = ByteRayLen / BytesPerSample;
    int maxshift = LastByteDex * Byte.SIZE;
    int majorbcnt = 0;
    int samplepnt;
    int shifter;
    audio = new int[IntRayLen];
    for (int icnt = 0; icnt < IntRayLen; icnt++) {
      shifter = maxshift;// big endian
      samplepnt = (ByteArray[majorbcnt++] << shifter);// MSB, preserve sign
      while (shifter > 0) {
        shifter -= Byte.SIZE;
        samplepnt |= ((ByteArray[majorbcnt++] & 0xFF) << shifter);
      }
      audio[icnt] = samplepnt;
    }
    return audio;
  }
  /* **************************************************************************** */
  public void Square(int SoundInts[], int NumSamples, double WaveLen) {
    int Toggle = this.MaxAmp;
    int HalfWaveLan = ((int) WaveLen) / 2;
    for (int icnt = 0; icnt < NumSamples; icnt++) {
      SoundInts[icnt] = Toggle;
      if (icnt % HalfWaveLan == 0) {// not so efficient
        Toggle = -Toggle;
      }
    }
  }
  /* **************************************************************************** */
  public void Sine(int SoundInts[], int NumSamples, double WaveLen) {
    double Time = 0;
    for (int icnt = 0; icnt < NumSamples; icnt++) {
      Time = ((double) icnt) / WaveLen;
      double amp = Math.sin(Time);
      //amp *= 1024.0 * 16.0;// 256.0;// arbitrary amplification
      amp *= this.MaxAmp;// max amplitude for this bit size
      SoundInts[icnt] = (int) Math.round(amp);
    }
  }
  /* **************************************************************************** */
  public void SawTooth(int SoundInts[], int NumSamples, int WaveLen) {
    //double Time=0;
    for (int icnt = 0; icnt < NumSamples; icnt++) {
      //Time=((double)icnt)/20.0;
      int trunc = icnt % WaveLen;
      SoundInts[icnt] = trunc;
    }
  }
  /* **************************************************************************** */
  public void PrintSample(int SoundInts[], int NumSamples) {
    System.out.println("Header XXXXXXXXXXXX");
    for (int icnt = 0; icnt < NumSamples; icnt++) {
      int bt = SoundInts[icnt];
      System.out.println(bt);
    }
  }
}
