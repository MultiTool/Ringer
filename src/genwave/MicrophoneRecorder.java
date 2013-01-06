/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package genwave;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.sound.sampled.*;

/**
 *
 * @author john
 */
/**  
 * Reads data from the input channel and writes to the output stream  
 */
public class MicrophoneRecorder {// implements Runnable {
  // record microphone && generate stream/byte array  

  public static final int SAMPLE_RATE = 44100;
  private static final int BYTES_PER_SAMPLE = 2;                // 16-bit audio
  private static final int BITS_PER_SAMPLE = 16;                // 16-bit audio
  private static final double MAX_16_BIT = Short.MAX_VALUE;     // 32,767
  private static final int SAMPLE_BUFFER_SIZE = 4096;
  private AudioInputStream audioInputStream;
  private AudioFormat format;
  public TargetDataLine line;
  public Thread thread;
  private double duration;
  public MicrophoneRecorder(AudioFormat format) {
    super();
    this.format = format;
  }
  private static AudioFormat getAudioFormat() {
    float sampleRate = 8000.0F;
    //8000,11025,16000,22050,44100
    int sampleSizeInBits = 16;
    //8,16
    int channels = 1;
    //1,2
    boolean signed = true;
    //true,false
    boolean bigEndian = false;
    //true,false
    return new AudioFormat(
            sampleRate,
            sampleSizeInBits,
            channels,
            signed,
            bigEndian);
  }//end getAudioFormat
  public void Test() {// throws Exception 
    {
      for (Mixer.Info mixerinfo : AudioSystem.getMixerInfo()) {
        Mixer mixer = AudioSystem.getMixer(mixerinfo);

        {
          AudioFormat audioFormat = getAudioFormat();

          DataLine.Info dataLineInfo =
                  new DataLine.Info(
                  TargetDataLine.class,
                  audioFormat);
          try {

            {
              // if (AudioSystem.isLineSupported(Port.Info.MICROPHONE)) {
              if (AudioSystem.isLineSupported(Port.Info.LINE_IN)) {
                try {
                  Port line = (Port) AudioSystem.getLine(Port.Info.MICROPHONE);
                } catch (Exception ex) {
                  boolean nop = true;
                }
              }

            }

            TargetDataLine targetDataLine = (TargetDataLine) mixer.getLine(dataLineInfo);
            targetDataLine.open(audioFormat);
            targetDataLine.start();
          } catch (Exception ex) {
            boolean nop = true;
          }
        }

        System.out.println(mixerinfo.toString());
        if (mixer.isLineSupported(Port.Info.MICROPHONE)) {
          //mixers.add(mixer);
          //System.out.println(Integer.toString(mixers.size()) + ": " + mixerinfo.toString());
        }
      }

      AudioFormat audioFormat = getAudioFormat();
    }
    DataLine.Info info;
    {
      try {
        float sampleRate = 8000;
        int sampleSizeInBits = 8;
        int channels = 1;
        boolean signed = true;
        boolean bigEndian = true;
        AudioFormat formatx = new AudioFormat((float) SAMPLE_RATE, BITS_PER_SAMPLE, 1, true, false);
        //AudioFormat formatx = new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);

        TargetDataLine line;
        info = new DataLine.Info(SourceDataLine.class, formatx); // format is an AudioFormat object
        //info = new DataLine.Info(TargetDataLine.class, formatx); // format is an AudioFormat object
        if (!AudioSystem.isLineSupported(info)) {
          // Handle the error ... 
        }
      } catch (Exception ex) {
      }
// Obtain and open the line.
      try {
        AudioFormat formatx = new AudioFormat((float) SAMPLE_RATE, BITS_PER_SAMPLE, 1, true, false);
        line = (TargetDataLine) AudioSystem.getLine(info);
        line.open(formatx);
      } catch (Exception ex) {
        // Handle the error ... 
      }
      /*
       for (Mixer.Info mixerinfo : AudioSystem.getMixerInfo()) {
       mixer = AudioSystem.getMixer(mixerinfo);

       //System.out.println(mixerinfo.toString());
       if (mixer.isLineSupported(Port.Info.MICROPHONE)) {
       mixers.add(mixer);
       System.out.println(Integer.toString(mixers.size()) + ": " + mixerinfo.toString());
       }
       }
       */
    }
    /*
     MicrophoneRecorder mr = new MicrophoneRecorder(AudioFormatUtil.getDefaultFormat());
     mr.start();
     Thread.sleep(2000);
     mr.stop();

     AudioInputStream ais = mr.getAudioInputStream();
     */
    //save  
    //WaveData wd = new WaveData();  
    //Thread.sleep(3000);
    //wd.saveToFile("~tmp", Type.WAVE, mr.getAudioInputStream());  
  }
  public void start() {
    thread = new Thread(this);
    thread.setName("Capture");
    thread.start();
  }
  public void stop() {
    thread = null;
  }
  @Override
  public void run() {
    duration = 0;
    line = getTargetDataLineForRecord();
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    final int frameSizeInBytes = format.getFrameSize();
    final int bufferLengthInFrames = line.getBufferSize() / 8;
    final int bufferLengthInBytes = bufferLengthInFrames * frameSizeInBytes;
    final byte[] data = new byte[bufferLengthInBytes];
    int numBytesRead;
    line.start();
    while (thread != null) {
      if ((numBytesRead = line.read(data, 0, bufferLengthInBytes)) == -1) {
        break;
      }
      out.write(data, 0, numBytesRead);
    }
    // we reached the end of the stream. stop and close the line.  
    line.stop();
    line.close();
    line = null;
    // stop and close the output stream  
    try {
      out.flush();
      out.close();
    } catch (final IOException ex) {
      ex.printStackTrace();
    }
    // load bytes into the audio input stream for playback  
    final byte audioBytes[] = out.toByteArray();
    final ByteArrayInputStream bais = new ByteArrayInputStream(audioBytes);
    audioInputStream = new AudioInputStream(bais, format,
            audioBytes.length / frameSizeInBytes);
    final long milliseconds = (long) ((audioInputStream.getFrameLength()
            * 1000) / format.getFrameRate());
    duration = milliseconds / 1000.0;
    System.out.println(duration);
    try {
      audioInputStream.reset();
      System.out.println("resetting...");
    } catch (final Exception ex) {
      ex.printStackTrace();
      return;
    }
  }
  private TargetDataLine getTargetDataLineForRecord() {
    TargetDataLine line;
    final DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
    if (!AudioSystem.isLineSupported(info)) {
      return null;
    }
    // get and open the target data line for capture.  
    try {
      line = (TargetDataLine) AudioSystem.getLine(info);
      line.open(format, line.getBufferSize());
    } catch (final Exception ex) {
      return null;
    }
    return line;
  }
  public AudioInputStream getAudioInputStream() {
    return audioInputStream;
  }
  public AudioFormat getFormat() {
    return format;
  }
  public void setFormat(AudioFormat format) {
    this.format = format;
  }
  public Thread getThread() {
    return thread;
  }
  public double getDuration() {
    return duration;
  }
}
/*
 public class Testss {  
   public static void main(String[] args) throws Exception {  
     MicrophoneRecorder mr = new MicrophoneRecorder(AudioFormatUtil.getDefaultFormat());  
     mr.start();  
     Thread.sleep(2000);  
     mr.stop();  
     //save  
     WaveData wd = new WaveData();  
     Thread.sleep(3000);  
     wd.saveToFile("~tmp", Type.WAVE, mr.getAudioInputStream());  
   }  
 }  
 */