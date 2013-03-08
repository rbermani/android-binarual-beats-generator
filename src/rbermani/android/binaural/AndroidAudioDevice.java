/*******************************************************************************
 * Copyright (c) 2010 Robert Bermani.
 * All rights reserved. This program and its accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which is included with this distribution and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Robert Bermani - initial API and implementation
 ******************************************************************************/
package rbermani.android.binaural;

import android.media.AudioTrack;
import android.media.AudioManager;
import android.media.AudioFormat;


public class AndroidAudioDevice
{
   private AudioTrack track;
   private int minSize;
   private short[] buffer;
 
   public AndroidAudioDevice()
   {
      minSize = AudioTrack.getMinBufferSize( 44100,
    		  AudioFormat.CHANNEL_CONFIGURATION_STEREO,
    		  AudioFormat.ENCODING_PCM_16BIT ) * 6;        
      
      buffer = new short[minSize];
      
      track = new AudioTrack( AudioManager.STREAM_MUSIC, 44100, 
                                        AudioFormat.CHANNEL_CONFIGURATION_STEREO,
                                        AudioFormat.ENCODING_PCM_16BIT, 
                                        minSize,
                                        AudioTrack.MODE_STREAM);
      track.play();        
      
   }	   
   public void pauseAudio(){
	   track.pause();
   }
   public void release() {
	   track.release();
   }
   public void writeSamples( double[] samples ) 
   {	
      fillBuffer( samples );
      track.write( buffer, 0, samples.length );
   }
 
   public void writeSamples ( short[] samples )
   {
	   fillBuffer ( samples );
	   track.write( buffer, 0, samples.length );
   }
   
   public void writeRaw ( short[] samples )
   {
	   track.write ( samples, 0, samples.length );
   }
   private void fillBuffer( double[] samples )
   {
      if( buffer.length < samples.length )
         buffer = new short[samples.length];
 
      for( int i = 0; i < samples.length; i++ )
         buffer[i] = (short) (samples[i] * Short.MAX_VALUE);
   }
   private void fillBuffer( short[] samples )
   {
      if( buffer.length < samples.length )
         buffer = new short[samples.length];
 
      for( int i = 0; i < samples.length; i++ )
         buffer[i] = samples[i];
   }
public int getMinSize() {
	return minSize;
}		
}
