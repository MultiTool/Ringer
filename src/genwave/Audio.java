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
  /* **************************************************************************** */
  public void Capture() {
    captureAudio();
    running = false;
    playAudio();
  }
  int bufferSize;
  byte buffer[];
  /* **************************************************************************** */
  private void CaptureAudioToBuffer() {
    // still thinking about the right way to do this
    try {
      final AudioFormat format = getFormat();
      DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
      final TargetDataLine line = (TargetDataLine) AudioSystem.getLine(info);
      line.open(format);
      line.start();

      try {
        int count = line.read(buffer, 0, buffer.length);
        if (count > 0) {
          out.write(buffer, 0, count);
        }
        out.close();
      } catch (IOException e) {
        System.err.println("I/O problems: " + e);
        System.exit(-1);
      }

      Runnable runner = new Runnable() {
        @Override
        public void run() {
          try {
            int count = line.read(buffer, 0, buffer.length);
            if (count > 0) {
              out.write(buffer, 0, count);
            }
            out.close();
          } catch (IOException e) {
            System.err.println("I/O problems: " + e);
            System.exit(-1);
          }
        }
      };
      Thread captureThread = new Thread(runner);
      captureThread.start();
    } catch (LineUnavailableException e) {
      System.err.println("Line unavailable: " + e);
      System.exit(-2);
    }
  }
  /* **************************************************************************** */
  private void captureAudio() {
    try {
      final AudioFormat format = getFormat();
      DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
      final TargetDataLine line = (TargetDataLine) AudioSystem.getLine(info);
      line.open(format);
      line.start();
      Runnable runner = new Runnable() {
        int bufferSize = (int) format.getSampleRate() * format.getFrameSize();
        byte buffer[] = new byte[bufferSize];
        @Override
        public void run() {
          out = new ByteArrayOutputStream();
          running = true;
          try {
            while (running) {
              int count = line.read(buffer, 0, buffer.length);
              if (count > 0) {
                out.write(buffer, 0, count);
              }
            }
            out.close();
          } catch (IOException e) {
            System.err.println("I/O problems: " + e);
            System.exit(-1);
          }
        }
      };
      Thread captureThread = new Thread(runner);
      captureThread.start();
    } catch (LineUnavailableException e) {
      System.err.println("Line unavailable: " + e);
      System.exit(-2);
    }
  }
  /* **************************************************************************** */
  private void playAudio() {
    try {
      byte audio[] = out.toByteArray();
      InputStream input = new ByteArrayInputStream(audio);
      final AudioFormat format = getFormat();
      final AudioInputStream ais = new AudioInputStream(input, format, audio.length / format.getFrameSize());
      DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
      final SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
      line.open(format);
      line.start();

      Runnable runner = new Runnable() {
        int bufferSize = (int) format.getSampleRate() * format.getFrameSize();
        byte buffer[] = new byte[bufferSize];
        @Override
        public void run() {
          try {
            int count;
            while ((count = ais.read(buffer, 0, buffer.length)) != -1) {
              if (count > 0) {
                line.write(buffer, 0, count);
              }
            }
            line.drain();
            line.close();
          } catch (IOException e) {
            System.err.println("I/O problems: " + e);
            System.exit(-3);
          }
        }
      };
      Thread playThread = new Thread(runner);
      playThread.start();
    } catch (LineUnavailableException e) {
      System.err.println("Line unavailable: " + e);
      System.exit(-4);
    }
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
            running = false;
            SpeakLine.drain();
            SpeakLine.close();
          } catch (IOException e) {
            System.err.println("I/O problems: " + e);
            running = false;
            System.exit(-3);
          }
        }
      };
      Thread playThread = new Thread(SpeakerRun);

      Runnable ListenerRun = new Runnable() {
        int bufferSize = (int) ListenFormat.getSampleRate() * ListenFormat.getFrameSize();
        byte buffer[] = new byte[bufferSize];
        @Override
        public void run() {
          out = new ByteArrayOutputStream();
          running = true;
          try {
            while (running) {
              int count = ListenLine.read(buffer, 0, buffer.length);
              if (count > 0) {
                out.write(buffer, 0, count);
              }
            }
            out.close();
          } catch (IOException e) {
            System.err.println("I/O problems: " + e);
            System.exit(-1);
          }
        }
      };
      Thread captureThread = new Thread(ListenerRun);

      running = true;
      playThread.start();
      captureThread.start();

    } catch (LineUnavailableException e) {
      System.err.println("Line unavailable: " + e);
      System.exit(-2);
      System.exit(-4);
    }
  }
  /* **************************************************************************** */
  private AudioFormat getFormat() {
    float sampleRate = 8000;
    //float sampleRate = 44100;
    int sampleSizeInBits = 8;
    //int sampleSizeInBits = 16;
    int channels = 1;
    boolean signed = true;
    boolean bigEndian = true;
    return new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
  }
  /* **************************************************************************** */
  private void Feedback2(byte audio[]) {
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
            running = false;
            SpeakLine.drain();
            SpeakLine.close();
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
            if (false) {
              int[] WavInt = ByteWav2Int(buffer, count);
              for (int bcnt = 0; bcnt < WavInt.length; bcnt++) {
                int bt = WavInt[bcnt];
                System.out.println("bt:" + bt);
              }
            } else {
              for (int bcnt = 0; bcnt < count; bcnt++) {
                byte bt = buffer[bcnt];
                //System.out.println("bt:" + bt);
                System.out.println(bt);
              }
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
  public int[] ByteWav2Int(byte[] byteArray, int len) {
    int[] audio = new int[len / 2];
    int bcnt = 0;
    for (int icnt = 0; icnt < len / 2; icnt++) { // read in the samples
      int ub1 = byteArray[bcnt++] & 0xFF;
      int ub2 = byteArray[bcnt++] & 0xFF;
      audio[icnt] = (ub2 << 8) + ub1;
      //audio[icnt] = (ub1 << 8) + ub2;
    }
    return audio;
  }
  /* **************************************************************************** */
  public void Test() {
    int NumSamples = 1000;
    byte SoundBytes[];

    if (true) {
      SoundBytes = new byte[NumSamples * 1];
      int bcnt = 0;
      for (int icnt = 0; icnt < NumSamples; icnt++) {
        byte ub0 = (byte) ((icnt & 0xFF) - 127);
        SoundBytes[bcnt++] = ub0;
        //SoundBytes[bcnt++] = 0;
      }
    } else {
      SoundBytes = new byte[NumSamples * 2];
      int bcnt = 0;
      for (int icnt = 0; icnt < NumSamples; icnt++) {

        double ficnt = (double) icnt;
        //int Amp = Math.sin(ficnt / 100.0);

        byte ub0 = (byte) (icnt & 0xFF);
        byte ub1 = (byte) ((icnt >> 8) & 0xFF);

        SoundBytes[bcnt++] = ub0;
        SoundBytes[bcnt++] = ub1;
        //System.out.println(icnt);
      }
    }
    /*
     int[] audio = new int[byteArray.length/2];
     for (int i = 0; i < byteArray.length/2; i++) { // read in the samples
     int ub1 = byteArray[i * 2 + 0] & 0xFF;
     int ub2 = byteArray[i * 2 + 1] & 0xFF;
     audio[i] = (ub2 << 8) + ub1;
     }
     */
    Feedback2(SoundBytes);
  }
}
