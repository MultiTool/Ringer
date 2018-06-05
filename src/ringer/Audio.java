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
  ByteArrayOutputStream out;
  public int BytesPerSample = 2;// 4
  public int BitsPerSample = BytesPerSample * Byte.SIZE;
  float SampleRate = 44100;//8000;
  int bufferSize;
  byte buffer[];
  /* **************************************************************************** */
  public void Test() { // http://www.jsresources.org/faq_audio.html
    int NumSamples = 20000;
    int SoundInts0[] = new int[NumSamples];
    int SoundInts1[] = new int[NumSamples];
    //SawTooth(SoundInts0, NumSamples, 500);//120);
    Sine(SoundInts0, NumSamples, 300);//120);

    Feedback(SoundInts0, SoundInts1);
  }
  /* **************************************************************************** */
  private void Feedback(int WaveSpeak[], int WaveListen[]) {
    byte SpeakBytes[]; //this.PrintSample(WaveSpeak, WaveSpeak.length);
    SpeakBytes = Ints2Bytes(WaveSpeak);
    try {
      final AudioFormat SpeakFormat = GetFormat();
      final AudioInputStream ais;
      final SourceDataLine SpeakLine;
      int[] WavInt;
      DataLine.Info SpeakInfo, ListenInfo;
      {
        //int numframes = SpeakBytes.length / SpeakFormat.getFrameSize();
        InputStream input = new ByteArrayInputStream(SpeakBytes);
        ais = new AudioInputStream(input, SpeakFormat, SpeakBytes.length / SpeakFormat.getFrameSize());
        SpeakInfo = new DataLine.Info(SourceDataLine.class, SpeakFormat);
        SpeakLine = (SourceDataLine) AudioSystem.getLine(SpeakInfo);
        SpeakLine.open(SpeakFormat);
        SpeakLine.start();
      }
      final AudioFormat ListenFormat;
      final TargetDataLine ListenLine;
      {
        ListenFormat = GetFormat();
        ListenInfo = new DataLine.Info(TargetDataLine.class, ListenFormat);
        ListenLine = (TargetDataLine) AudioSystem.getLine(ListenInfo);
        ListenLine.open(ListenFormat);
        ListenLine.start();
      }

      // speak first in a separate thread, listen second in main thread
      // PLAY *****************************************************************************************
      Runnable SpeakerRun = new Runnable() { // player
        //float numframes = 1000;// SpeakFormat.getSampleRate();
        int BufferSize = (int) SpeakFormat.getSampleRate() * SpeakFormat.getFrameSize();
        byte buffer[] = new byte[BufferSize];
        @Override public void run() {
          try {
            int count;
            while ((count = ais.read(buffer, 0, buffer.length)) != -1) {
              if (count > 0) {
                SpeakLine.write(buffer, 0, count);
              }
            }
            SpeakLine.drain();
            try {
//              float dlay = ((float) (1000 * numframes)) / SpeakFormat.getSampleRate();
//              Thread.sleep((int) dlay);
              Thread.sleep(25);// length of the whole sample in milliseconds
            } catch (Exception ex) {
            }
            //while (SpeakLine.getFramePosition() < numframes) {}
            //while (SpeakLine.isActive()){}
            SpeakLine.close();
            running = false;
          } catch (IOException e) {
            System.err.println("I/O problems: " + e);
            running = false;
            //System.exit(-3);
          }
        }
      };
      Thread PlayThread = new Thread(SpeakerRun);

      //int BufferSize = (int) ListenFormat.getSampleRate() * ListenFormat.getFrameSize();
      int BufferSize = WaveListen.length * ListenFormat.getFrameSize();
      byte ListenBytes[] = new byte[BufferSize];

      out = new ByteArrayOutputStream();
      running = true;
//      PlayThread.setPriority(Thread.MAX_PRIORITY); PlayThread.setDaemon(true);
      PlayThread.start();

      // RECORD *****************************************************************************************
      int count;
      try {
        while (running) {
          count = ListenLine.read(ListenBytes, 0, ListenBytes.length);
          if (count > 0) {
            out.write(ListenBytes, 0, count);
            WavInt = Bytes2Ints(ListenBytes, count);
            for (int bcnt = 0; bcnt < WavInt.length; bcnt++) {
              int bt = WavInt[bcnt];
              System.out.println("WavInt:" + bt);
              WaveListen[bcnt] = WavInt[bcnt];
            }
          }
        }
        out.close();
      } catch (IOException e) {
        System.err.println("I/O problems: " + e); //System.exit(-1);
      }

    } catch (LineUnavailableException e) {
      System.err.println("Line unavailable: " + e); // System.exit(-4);
    }
    //Arrays.copyOf(WaveIn, WavInt);
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
        shifter -= 8;
        samplepnt |= ((ByteArray[majorbcnt++] & 0xFF) << shifter);
      }
      audio[icnt] = samplepnt;
    }
    return audio;
  }
  /* **************************************************************************** */
  public void Sine(int SoundInts[], int NumSamples, double WaveLen) {
    double Time = 0;
    for (int icnt = 0; icnt < NumSamples; icnt++) {
      Time = ((double) icnt) / WaveLen;
      double amp = Math.sin(Time);
      amp *= 1024.0 * 16.0;// 256.0;// arbitrary amplification
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
