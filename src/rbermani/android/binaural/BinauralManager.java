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

import android.content.Context;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.PowerManager;



public class BinauralManager {

	final int sineTableSz = 16384;

	private static final short NOISE_ADJUST = 12;
	private static final int NOISE_AMPLITUDE = (Short.MAX_VALUE);
	private static final short NOISE_BANDS = 9;
	private static final short RAND_MULT = 75;

	private AndroidAudioDevice device;

	private volatile boolean continueRunning = true;
	private double frequencyLeft, frequencyRight;

	private boolean pinkNoiseEnable = false;

	private volatile int leftChanIncrement;
	private volatile int rightChanIncrement;
	private int leftOffset;
	private int rightOffset;
	private volatile Thread backgroundThread;
	private int bufferLength;


	private Noise noiseTable[];
	private int noiseTableOffset = 1;
	private int rand0 = 0, rand1 = 0;

	private int randomSeed;

	private short sineTable[] = new short[sineTableSz];
	private int noiseBuffer[] = new int[sineTableSz];

	private MediaPlayer nMediaPlayer;
	private float pinkNoiseAmpPref = .020f;
	private float binauralAmpPref  = 0.05f;
	private float backgroundLoopTrack = 0.5f;

	public BinauralManager() {

		device = new AndroidAudioDevice( );
		
		
		this.bufferLength = device.getMinSize();
		this.randomSeed = 3;

		this.leftOffset = 0;
		this.rightOffset = 0;

		this.noiseTable = new Noise[NOISE_BANDS];

		for (int i = 0; i < noiseTable.length; i++)
			noiseTable[i] = new Noise();


		// Populate sine lookup table
		for (int a = 0; a < sineTableSz; a++) {
			sineTable[a] = (short) (Short.MAX_VALUE *
					Math.sin((2 * Math.PI * a) / sineTableSz));
		}

		// Populate pink noise lookup table
		for (int a = 0; a < noiseBuffer.length; a++) {
			noiseBuffer[a] = (genPinkNoiseSample() << 16);

		}


		backgroundThread = new Thread(new Runnable () {
			public void run() {

				while( continueRunning )
				{
					device.writeRaw( getMixedSamples() );

				}    

			}
		});
		

	}

	public boolean isPlaying(){
		return continueRunning;
	}
	private class Noise {
		public int value;
		public int increment;

		public Noise()
		{
			this.value = 0;
			this.increment = 0;
		}
	}

	public void loadBackgroundTrack(Context parent){

		if (nMediaPlayer == null){
		nMediaPlayer = MediaPlayer.create(parent, R.raw.waters);
		nMediaPlayer.setLooping(true);
		} 
		
		if (nMediaPlayer != null){
			//nMediaPlayer.setWakeMode(parent, PowerManager.PARTIAL_WAKE_LOCK);
			nMediaPlayer.setVolume(backgroundLoopTrack, backgroundLoopTrack);
		}
		
		
	}

	public void releaseBackgroundTrack(){
		if (nMediaPlayer != null){
			nMediaPlayer.release();
			} 
	}
	
	
	public void enableBackgroundTrack(){
		
		
		if (nMediaPlayer != null){
				if (!nMediaPlayer.isPlaying()){
				nMediaPlayer.start();}
			} 
		
	}

	public void disableBackgroundTrack(){
		if (nMediaPlayer != null){
			if (nMediaPlayer.isPlaying())
				nMediaPlayer.pause();
		}
	}
	public void setMixerLevels(int pinkNoiseAmp,
			int binauralAmp, int backgroundLoopAmp) {

		this.pinkNoiseAmpPref = 0.020f*(pinkNoiseAmp/100f+0.01f);
		this.binauralAmpPref =  0.058f*(binauralAmp/100f+0.01f);
		this.backgroundLoopTrack = 0.5f*(backgroundLoopAmp/100f+0.01f);

	}
	public void start() {
		continueRunning = true;

		try {
			if (!backgroundThread.isAlive()){	
				backgroundThread.start();
			}

		} catch (IllegalThreadStateException e) {
			backgroundThread = new Thread(new Runnable () {
				public void run() {

					while( continueRunning )
					{
						device.writeRaw( getMixedSamples() );

					}    

				}
			});
			backgroundThread.start();
		}	



	}

	public void stop() {

		continueRunning = false;

		if (nMediaPlayer != null){
			disableBackgroundTrack();
			
		} 


	}
	
	public void terminate() {

		continueRunning = false;

		if (nMediaPlayer != null){
			nMediaPlayer.stop();
			this.releaseBackgroundTrack();
			
		} 

		

	}
	public synchronized void configureBeats(double carrFrequency, double resFrequency){
		frequencyLeft = carrFrequency - (resFrequency/2);
		frequencyRight = carrFrequency + (resFrequency/2);

		leftChanIncrement =  (int) (frequencyLeft / 44100 * sineTableSz); // increment into sine table
		rightChanIncrement = (int) (frequencyRight / 44100 * sineTableSz);

		leftOffset = 0;
		rightOffset = 0;

	}	

	public void configureBeats(double carrFrequency, double resFrequency, int pinkNoiseAmp,
			int binauralAmp, int backgroundLoopAmp){

		configureBeats(carrFrequency, resFrequency);
		setMixerLevels(pinkNoiseAmp, binauralAmp, backgroundLoopAmp);

	}

	public short[] getMixedSamples() 
	{
		short outputBuffer[] = new short[bufferLength];
		int leftChanMix = 0, rightChanMix = 0;
		noiseTableOffset = 0;
		int leftChanIncrement = this.leftChanIncrement;
		int rightChanIncrement = this.rightChanIncrement;
		int leftOffset = this.leftOffset;
		int rightOffset = this.rightOffset;

		for( int i = 0; i < outputBuffer.length-1; i += 2 )
		{

			noiseTableOffset++;
			noiseTableOffset &= sineTableSz - 1;

			leftOffset &= sineTableSz - 1;
			leftChanMix = (int) (binauralAmpPref*(sineTable[leftOffset] << 16));
			leftOffset += leftChanIncrement;

			rightOffset &= sineTableSz - 1;
			rightChanMix = (int) (binauralAmpPref*(sineTable[rightOffset] << 16));
			rightOffset += rightChanIncrement;
			if (pinkNoiseEnable){

				leftChanMix += pinkNoiseAmpPref*noiseBuffer[noiseTableOffset];
				rightChanMix += pinkNoiseAmpPref*noiseBuffer[noiseTableOffset];

			}

			// white noise dithering
			rand0 = rand1;
			rand1 = (rand0 * 0x660D + 0xF35F) & 0xFFFF;

			if (leftChanMix <= 0x7FFF0000) leftChanMix += rand0;
			if (rightChanMix <= 0x7FFF0000) rightChanMix += rand0;

			outputBuffer[i] = (short) (leftChanMix >> 16);
			outputBuffer[i+1] = (short) (rightChanMix >> 16);

		}

		return outputBuffer;
	}

	public void pause(){
		device.pauseAudio();
	}

	public void setPinkNoise(boolean enable){
		this.pinkNoiseEnable = enable;
	}

	private short genPinkNoiseSample() 
	{

		int mixAccumulator;
		int count = 1;
		int offset;

		offset = noiseTableOffset++;

		int tmpValue;

		randomSeed *= RAND_MULT % 131074;
		mixAccumulator = ((randomSeed - 256)) * (NOISE_AMPLITUDE / 256 / (NOISE_BANDS + 1));

		for (int i = 0; (count & offset) != 0 && (i < noiseTable.length); i++) 
		{
			randomSeed *= RAND_MULT % 131074;
			tmpValue = (randomSeed - 256) * (NOISE_AMPLITUDE / 256 / (NOISE_BANDS + 1));
			count *= 2;

			noiseTable[i].increment = (tmpValue - noiseTable[i].value) / count;
			noiseTable[i].value += noiseTable[i].increment;
			mixAccumulator += noiseTable[i].value;
		}

		for (int i = 0; i < noiseTable.length; i++)
		{
			noiseTable[i].value += noiseTable[i].increment;
			mixAccumulator += noiseTable[i].value;
		}

		return ((short)mixAccumulator);

	}

	//	private class outputSound extends AsyncTask {
	//		protected Object doInBackground(Object... arg0)
	//		{
	//
	//			while( continueRunning )
	//			{
	//				device.writeRaw( getMixedSamples() );
	//			}        
	//			
	//			return null;
	//		}
	//	}

}


