/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package genwave;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.sound.sampled.*;

/**

 @author MultiTool
 Cannibalized from
 http://www.java-tips.org/java-se-tips/javax.sound/capturing-audio-with-java-sound-api.html

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
    float sampleRate = 8000;
    //float sampleRate = 44100;
    int sampleSizeInBits = 0;
    sampleSizeInBits = sampleint;
    int channels = 1;
    boolean signed = true;
    boolean bigEndian = true;
    return new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
  }
  /* **************************************************************************** */
  private void Feedback(byte audio[]) {
    try {
      InputStream input = new ByteArrayInputStream(audio);
      final AudioFormat SpeakFormat = getFormat();
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
            SpeakLine.close();
            try {
              Thread.sleep(100);
            } catch (Exception ex) {
            }
            running = false;
          } catch (IOException e) {
            System.err.println("I/O problems: " + e);
            running = false;
            System.exit(-3);
          }
        }
      };
      Thread playThread = new Thread(SpeakerRun);

      int bufferSize = (int) ListenFormat.getSampleRate() * ListenFormat.getFrameSize();
      byte buffer[] = new byte[bufferSize];

      out = new ByteArrayOutputStream();
      running = true;
      playThread.start();

      try {
        while (running) {
          int count = ListenLine.read(buffer, 0, buffer.length);
          if (count > 0) {
            out.write(buffer, 0, count);
            int[] WavInt = Bytes2Ints(buffer, count, BytesPerSample);
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
        System.exit(-1);
      }

    } catch (LineUnavailableException e) {
      System.err.println("Line unavailable: " + e);
      System.exit(-2);
      System.exit(-4);
    }
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
  public void Test() {
    // http://www.jsresources.org/faq_audio.html
    int NumSamples = 1000;
    byte SoundBytes[];
    int SoundInts[] = new int[NumSamples];
    int bcnt = 0;
    for (int icnt = 0; icnt < NumSamples; icnt++) {
      int trunc = icnt % 120;
      SoundInts[icnt] = trunc;
    }
    SoundBytes = Ints2Bytes(SoundInts, BytesPerSample);
    if (false) {// test
      int SoundInts2[] = Bytes2Ints(SoundBytes, SoundBytes.length, BytesPerSample);
      for (int icnt = 0; icnt < SoundInts2.length; icnt++) {
        System.out.println(SoundInts2[icnt]);
      }
    }
    Feedback(SoundBytes);
  }
}
