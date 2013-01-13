/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package genwave;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.sound.sampled.*;
import javax.swing.JButton;
import javax.swing.JFrame;

/*
* To change this template, choose Tools | Templates
* and open the template in the editor.
http://www.java-tips.org/java-se-tips/javax.sound/capturing-audio-with-java-sound-api.html
How to call from main:
  JFrame frame = new Capture();
  frame.pack();
  frame.show();
*/
public class Capture extends JFrame {
  protected boolean running;
  ByteArrayOutputStream out;
  public Capture() {
    //AudioSystem.getAudioInputStream(null) 
    super("Capture Sound Demo");
    setDefaultCloseOperation(EXIT_ON_CLOSE);
    Container content = getContentPane();
 
    final JButton capture = new JButton("Capture");
    final JButton stop = new JButton("Stop");
    final JButton play = new JButton("Play");
 
    capture.setEnabled(true);
    stop.setEnabled(false);
    play.setEnabled(false);
 
    ActionListener captureListener =
            new ActionListener() {
              @Override
              public void actionPerformed(ActionEvent e) {
                capture.setEnabled(false);
                stop.setEnabled(true);
                play.setEnabled(false);
                captureAudio();
              }
            };
    capture.addActionListener(captureListener);
    content.add(capture, BorderLayout.NORTH);
 
    ActionListener stopListener =
            new ActionListener() {
              @Override
              public void actionPerformed(ActionEvent e) {
                capture.setEnabled(true);
                stop.setEnabled(false);
                play.setEnabled(true);
                running = false;
              }
            };
    stop.addActionListener(stopListener);
    content.add(stop, BorderLayout.CENTER);
 
    ActionListener playListener =
            new ActionListener() {
              @Override
              public void actionPerformed(ActionEvent e) {
                playAudio();
              }
            };
    play.addActionListener(playListener);
    content.add(play, BorderLayout.SOUTH);
  }
 
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
 
  private void playAudio() {
    try {
      byte audio[] = out.toByteArray();
      InputStream input = new ByteArrayInputStream(audio);
      final AudioFormat format = getFormat();
      final AudioInputStream ais =
              new AudioInputStream(input, format, audio.length / format.getFrameSize());
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
 
  private AudioFormat getFormat() {
    float sampleRate = 8000;
    int sampleSizeInBits = 8;
    int channels = 1;
    boolean signed = true;
    boolean bigEndian = true;
    return new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
  }
 
  public static void main(String args[]) {
    JFrame frame = new Capture();
    frame.pack();
    frame.show();
  }
}
