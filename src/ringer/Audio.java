/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ringer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.sound.sampled.*;

/**

 @author MultiTool
 Cannibalized from
 http://www.java-tips.org/java-se-tips/javax.sound/capturing-audio-with-java-sound-api.html

 Note: There are odd code patterns here, put only only to make porting to C++ or D language easier.

 */
public class Audio {
  protected boolean running;
  ByteArrayOutputStream out;
  public int BytesPerSample = 2;
  public int sampleint = BytesPerSample * 8;
  int bufferSize;
  byte buffer[];
  /* **************************************************************************** */
  private AudioFormat getFormat() {
    //float sampleRate = 8000;
    float sampleRate = 44100;
    int sampleSizeInBits = sampleint;
    int channels = 1;
    boolean signed = true;
    boolean bigEndian = true;
    return new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
  }
  /* **************************************************************************** */
  private void Feedback(byte audio[]) {
    try {
      final AudioFormat SpeakFormat = getFormat();
      int numframes = audio.length / SpeakFormat.getFrameSize();
      InputStream input = new ByteArrayInputStream(audio);
      final AudioInputStream ais = new AudioInputStream(input, SpeakFormat, audio.length / SpeakFormat.getFrameSize());
      DataLine.Info info = new DataLine.Info(SourceDataLine.class, SpeakFormat);
      final SourceDataLine SpeakLine = (SourceDataLine) AudioSystem.getLine(info);
      SpeakLine.open(SpeakFormat);
      SpeakLine.start();

      final AudioFormat ListenFormat = getFormat();
      DataLine.Info ListenInfo = new DataLine.Info(TargetDataLine.class, ListenFormat);
      final TargetDataLine ListenLine = (TargetDataLine) AudioSystem.getLine(ListenInfo);
      ListenLine.open(ListenFormat);
      ListenLine.start();

      Runnable SpeakerRun = new Runnable() { // player
        float numframes = 1000;// SpeakFormat.getSampleRate();
        int bufferSize = (int) SpeakFormat.getSampleRate() * SpeakFormat.getFrameSize();
        byte buffer[] = new byte[bufferSize];
        @Override
        public void run() {
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
            System.out.println("I/O problems: " + e);
            running = false;
            //System.exit(-3);
          }
        }
      };
      Thread playThread = new Thread(SpeakerRun);

      int bufferSize = (int) ListenFormat.getSampleRate() * ListenFormat.getFrameSize();
      byte buffer[] = new byte[bufferSize];

      out = new ByteArrayOutputStream();
      running = true;
      //running = false;

//      playThread.setPriority(Thread.MAX_PRIORITY);
//      playThread.setDaemon(true);
      playThread.start();
//      try {
//        Thread.sleep(100);
//      } catch (Exception ex) {
//      }

      int count;
      int[] WavInt;
      try {
        while (running) {
          count = ListenLine.read(buffer, 0, buffer.length);
          if (count > 0) {
            out.write(buffer, 0, count);
            WavInt = Bytes2Ints(buffer, count, BytesPerSample);
            for (int bcnt = 0; bcnt < WavInt.length; bcnt++) {
              int bt = WavInt[bcnt];
              System.out.println(bt);
            }
          }
          boolean nop = true;
        }
        out.close();
      } catch (IOException e) {
        System.err.println("I/O problems: " + e);
        System.out.println("I/O problems: " + e);
        //System.exit(-1);
      }

    } catch (LineUnavailableException e) {
      System.err.println("Line unavailable: " + e);
      System.out.println("Line unavailable: " + e);
      // System.exit(-4);
    }
  }
  /* **************************************************************************** */
  private int[] FeedbackTest(int SoundInts[]) {
    int[] WavInt = null;
    byte SoundBytes[] = Ints2Bytes(SoundInts, BytesPerSample);
    try {
      final AudioFormat SpeakFormat = getFormat();
      int numframes = SoundBytes.length / SpeakFormat.getFrameSize();
      InputStream input = new ByteArrayInputStream(SoundBytes);
      final AudioInputStream ais = new AudioInputStream(input, SpeakFormat, SoundBytes.length / SpeakFormat.getFrameSize());
      DataLine.Info info = new DataLine.Info(SourceDataLine.class, SpeakFormat);
      final SourceDataLine SpeakLine = (SourceDataLine) AudioSystem.getLine(info);
      SpeakLine.open(SpeakFormat);
      SpeakLine.start();

      final AudioFormat ListenFormat = getFormat();
      DataLine.Info ListenInfo = new DataLine.Info(TargetDataLine.class, ListenFormat);
      final TargetDataLine ListenLine = (TargetDataLine) AudioSystem.getLine(ListenInfo);
      ListenLine.open(ListenFormat);
      ListenLine.start();

      Runnable SpeakerRun = new Runnable() { // player
        float numframes = 1000;// SpeakFormat.getSampleRate();
        int bufferSize = (int) SpeakFormat.getSampleRate() * SpeakFormat.getFrameSize();
        byte buffer[] = new byte[bufferSize];
        float Delay = ((float) (1000 * numframes)) / SpeakFormat.getSampleRate();
        @Override
        public void run() {
          try {
            int count;
            while ((count = ais.read(buffer, 0, buffer.length)) != -1) {
              if (count > 0) {
                SpeakLine.write(buffer, 0, count);
              }
            }
            SpeakLine.drain();
            try {
              //Thread.sleep(25);// length of the whole sample in milliseconds
              Thread.sleep((int) Delay);
            } catch (Exception ex) {
            }
            SpeakLine.close();
            running = false;
          } catch (IOException e) {
            System.err.println("I/O problems: " + e);
            System.out.println("I/O problems: " + e);
            running = false;
            //System.exit(-3);
          }
        }
      };
      Thread playThread = new Thread(SpeakerRun);
      playThread.start();

      int InBufferSize = (int) ListenFormat.getSampleRate() * ListenFormat.getFrameSize();
      InBufferSize /= 4;
      //InBufferSize += 1;
      byte InBuffer[] = new byte[InBufferSize];

      out = new ByteArrayOutputStream();
      running = true;
      int count;
      try {
        while (running) {
          count = ListenLine.read(InBuffer, 0, InBuffer.length);
          if (count > 0) {
            out.write(InBuffer, 0, count);
            WavInt = Bytes2Ints(InBuffer, count, BytesPerSample);
          }
        }
        out.close();
        ListenLine.close();
      } catch (IOException e) {
        System.err.println("I/O problems: " + e);
        System.out.println("I/O problems: " + e);
        WavInt = null;
        ListenLine.close();
//        System.exit(-1);
      }
    } catch (LineUnavailableException e) {
      System.err.println("Line unavailable: " + e);
      System.out.println("Line unavailable: " + e);
      WavInt = null;
//      System.exit(-2);
    }
    return WavInt;
  }
  /* **************************************************************************** */
  public int[] Bytes2Ints(byte[] byteArray, int ByteRayLen, int BytesPerSample) {
    //int ByteRayLen = byteArray.length;
    int[] audio = null;
    int LastByteDex = BytesPerSample - 1;
    int IntRayLen = ByteRayLen / BytesPerSample;
    int maxshift = LastByteDex * 8;
    int majorbcnt = 0;
    int samplepnt;
    int shifter;
    audio = new int[IntRayLen];
    for (int icnt = 0; icnt < IntRayLen; icnt++) {
      shifter = maxshift;// big endian
      samplepnt = (byteArray[majorbcnt++] << shifter);// MSB, preserve sign
      while (shifter > 0) {
        shifter -= 8;
        samplepnt |= ((byteArray[majorbcnt++] & 0xFF) << shifter);
      }
      audio[icnt] = samplepnt;
    }
    return audio;
  }
  /* **************************************************************************** */
  public byte[] Ints2Bytes(int Ints[], int BytesPerSample) {
    // unfinished attempt at generic mapping to byte array
    byte bt;
    int shifter = 0;
    int majorbcnt;
    int maxshift = (BytesPerSample - 1) * 8;
    int NumSamples = Ints.length;
    byte OutBytes[] = new byte[NumSamples * BytesPerSample];
    majorbcnt = 0;
    for (int icnt = 0; icnt < NumSamples; icnt++) {
      int samplepnt = Ints[icnt];
      if (false) {
        shifter = 0;// little endian
        for (int bcnt = 0; bcnt < BytesPerSample; bcnt++) {
          bt = (byte) ((samplepnt >> shifter) & 0xFF);
          OutBytes[majorbcnt++] = bt;
          shifter += 8;
        }
      } else {
        shifter = maxshift;// big endian
        for (int bcnt = 0; bcnt < BytesPerSample; bcnt++) {
          bt = (byte) ((samplepnt >> shifter) & 0xFF);
          OutBytes[majorbcnt++] = bt;
          shifter -= 8;
        }
      }
    }
    return OutBytes;
  }
  /* **************************************************************************** */
  public void SawTooth(int SoundInts[], int NumSamples, int WaveLen) {
    for (int icnt = 0; icnt < NumSamples; icnt++) {
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
  /* **************************************************************************** */
  public void Test() {
    // http://www.jsresources.org/faq_audio.html
    int NumSamples = 1000;
    byte SoundBytes[];
    int SoundInts[] = new int[NumSamples];
    SawTooth(SoundInts, NumSamples, 120);
    int[] HearInts = Test(SoundInts, NumSamples);
    double Score = JudgeWave(HearInts, HearInts.length);
    // Double.MAX_VALUE;
    SoundBytes = Ints2Bytes(SoundInts, BytesPerSample);
    if (false) {// test
      int SoundInts2[] = Bytes2Ints(SoundBytes, SoundBytes.length, BytesPerSample);
      for (int icnt = 0; icnt < SoundInts2.length; icnt++) {
        System.out.println(SoundInts2[icnt]);
      }
    }
    Feedback(SoundBytes);
  }
  /* **************************************************************************** */
  public int[] Test(int SoundInts[], int NumSamples) {
    int[] HearInts = FeedbackTest(SoundInts);
    double Score = JudgeWave(HearInts, HearInts.length);
    return HearInts;
  }
  /* **************************************************************************** */
  public double ScoreTest(int SoundInts[], int NumSamples) {
    //SawTooth(SoundInts, NumSamples, 22);// test
    int[] HearInts = FeedbackTest(SoundInts);
    if (HearInts == null) {
      return 0.0;
    }
    //PrintSample(HearInts, HearInts.length);
    double Score = JudgeWave(HearInts, HearInts.length);
    return Score;
  }
  /* **************************************************************************** */
  public double JudgeWave(int[] HearInts, int NumSamples) {
    double Score = 0.0;
    for (int scnt = 0; scnt < NumSamples; scnt++) {
      double amp = HearInts[scnt];
      Score += amp * amp;
    }
    return Score / (double) NumSamples;
  }
}
