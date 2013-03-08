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

import java.util.List;

import com.admob.android.ads.AdManager;
import com.admob.android.ads.AdView;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;


public class MainActivity extends Activity implements OnClickListener, OnTouchListener, 
OnItemSelectedListener
{	    

	protected ArrayAdapter<CharSequence> mAdapter;

	private ImageButton leftCarrBtn, rightCarrBtn, leftBeatBtn,rightBeatBtn;
	private LinearLayout layoutBackground;
	private Button executeBtn, muteBtn;
	private EditText txtCarrier,txtBeat;
	private BinauralManager binaural;
	private double carrierFrequency,beatFrequency;
	private double beatFreqMax, beatFreqMin;
	private final double carrierFreqMax = 800;
	private final double carrierFreqMin = 0;
	private final int holdDelayMs = 800;
	
	private AdView adView;
	
	private boolean chkBoxPinkNoise;
	private boolean chkBoxBackground;
	private boolean chkBoxMixer;
	private int pinkNoiseAmpPref;
	private int binauralAmpPref;
	private int backgroundLoopTrack;

	private Handler mHandler = new Handler();

	private Runnable mIncrementBeat = new Runnable() {
		public void run() {
			beatFrequency++;
			setBeatText();
			mHandler.postDelayed(this, 200);
		}
	};

	private Runnable mDecrementBeat = new Runnable() {
		public void run() {
			beatFrequency--;
			setBeatText();
			mHandler.postDelayed(this, 200);
		}
	};
	private Runnable mIncrementCarrier = new Runnable() {
		public void run() {
			carrierFrequency++;
			setCarrierText();
			mHandler.postDelayed(this, 200);
		}
	};

	private Runnable mDecrementCarrier = new Runnable() {
		public void run() {
			carrierFrequency--;
			setCarrierText();
			mHandler.postDelayed(this, 200);
		}
	};

	private Runnable mRefreshAd = new Runnable() {
		public void run() {
			adView.requestFreshAd();
			mHandler.postDelayed(this, 120000);
		}
	};
	public void onItemSelected(AdapterView<?> parent,
			View view, int pos, long id) {

		switch (pos){
		case 0: //Alpha
			beatFreqMax = 13;
			beatFreqMin = 8;
			break;
		case 1: // Beta
			beatFreqMax = 30;
			beatFreqMin = 13;
			break;

		case 2: //Theta
			beatFreqMax = 8;
			beatFreqMin = 4;
			break;
		case 3: //Delta

			beatFreqMax = 4;
			beatFreqMin = 0.5;
			break;
		}
		setBeatText();

	}

	public void onNothingSelected(AdapterView parent) {
		// Do nothing.
	}

	protected void onStart(){
		super.onStart();
		getPrefs();

	}
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);    

		setContentView(R.layout.main);
		
//		AdManager.setTestDevices( new String[] {  
//			      AdManager.TEST_EMULATOR,      // Android emulator  
//			      "015A8A3C1501A002", // My Droid X ID 
//			         } ); 
		
		adView = (AdView) findViewById(R.id.ad);
		mHandler.post(mRefreshAd);
		
		rightCarrBtn = (ImageButton) findViewById(R.id.rightCarrBtn);
		leftCarrBtn = (ImageButton) findViewById(R.id.leftCarrBtn);
		leftBeatBtn = (ImageButton) findViewById(R.id.leftBeatBtn);
		rightBeatBtn = (ImageButton) findViewById(R.id.rightBeatBtn);

		executeBtn = (Button) findViewById(R.id.executeBtn);
		muteBtn = (Button) findViewById(R.id.btnMute);

		txtCarrier = (EditText) findViewById(R.id.txtCarrFreq);
		txtBeat = (EditText) findViewById(R.id.txtBeatFreq);

		layoutBackground = (LinearLayout) findViewById(R.id.BackgroundLayout);
		binaural = new BinauralManager(); 
		
		this.carrierFrequency = 200;
		setCarrierText();
		

		Spinner spinner = (Spinner) findViewById(R.id.spnWaveState);
		this.mAdapter = ArrayAdapter.createFromResource(this, R.array.wave_states,
				android.R.layout.simple_spinner_item);
		this.mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(mAdapter);
		spinner.setOnItemSelectedListener(this);

		rightCarrBtn.setOnClickListener(this);
		leftCarrBtn.setOnClickListener(this);
		leftBeatBtn.setOnClickListener(this);
		rightBeatBtn.setOnClickListener(this);
		executeBtn.setOnClickListener(this);
		muteBtn.setOnClickListener(this);

		rightCarrBtn.setOnTouchListener(this);
		leftCarrBtn.setOnTouchListener(this);
		leftBeatBtn.setOnTouchListener(this);
		rightBeatBtn.setOnTouchListener(this);
		executeBtn.setOnTouchListener(this);
		muteBtn.setOnTouchListener(this);
		
//		txtBeat.setOnFocusChangeListener(this);
//		txtCarrier.setOnFocusChangeListener(this);
		layoutBackground.setOnClickListener(this);
		
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.settings:
			Intent settingsActivity = new Intent(getBaseContext(),
					PreferencesDlg.class);

			startActivity(settingsActivity);
			return true;
		case R.id.about:
			Intent aboutActivity = new Intent(getBaseContext(),
					AboutActivity.class);
			
			startActivity(aboutActivity);
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void getPrefs() {

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

		chkBoxPinkNoise = prefs.getBoolean("chkBoxPinkNoise", false);
		chkBoxBackground = prefs.getBoolean("chkBoxBackground", false);
		chkBoxMixer = prefs.getBoolean("chkBoxMixer", false);
		if (chkBoxMixer){
		pinkNoiseAmpPref = prefs.getInt("pinkNoiseAmpPref", 100);
		binauralAmpPref = prefs.getInt("binauralAmpPref", 100);
		backgroundLoopTrack = prefs.getInt("backgroundLoopTrack", 100);
		} else{
			pinkNoiseAmpPref = 100;
			binauralAmpPref = 100;
			backgroundLoopTrack = 100;
		}
		
		binaural.setMixerLevels(pinkNoiseAmpPref,binauralAmpPref,backgroundLoopTrack);
		if (chkBoxBackground && binaural.isPlaying()){
			binaural.loadBackgroundTrack(this);
			binaural.enableBackgroundTrack();
		} else {
			binaural.disableBackgroundTrack();
		}
		
		
		binaural.setPinkNoise(this.chkBoxPinkNoise);

	}	

	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}


	public void onBackPressed(){

		binaural.terminate();
		

		this.finish();
	}
	private void setBeatText(){
		if (this.beatFrequency < beatFreqMin){
			this.beatFrequency = beatFreqMin;
		} else if (this.beatFrequency > beatFreqMax){
			this.beatFrequency = beatFreqMax;
		}



		txtBeat.setText(Double.valueOf(this.beatFrequency).toString(),TextView.BufferType.EDITABLE);

	}
	private void setCarrierText(){
		if (this.carrierFrequency < 0){
			this.carrierFrequency = 0;
		} else if (this.carrierFrequency > carrierFreqMax){
			this.carrierFrequency = carrierFreqMax;
		}

		txtCarrier.setText(Double.valueOf(this.carrierFrequency).toString(),TextView.BufferType.EDITABLE);

	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		if (v == rightCarrBtn){
			if (event.getAction() == MotionEvent.ACTION_DOWN){
				mHandler.removeCallbacks(mIncrementCarrier);
				mHandler.postDelayed(mIncrementCarrier, holdDelayMs);
			} else if (event.getAction() == MotionEvent.ACTION_UP){
				mHandler.removeCallbacks(mIncrementCarrier);
			}

		}else if (v == leftCarrBtn){
			if (event.getAction() == MotionEvent.ACTION_DOWN){
				mHandler.removeCallbacks(mDecrementCarrier);
				mHandler.postDelayed(mDecrementCarrier, holdDelayMs);
			} else if (event.getAction() == MotionEvent.ACTION_UP){
				mHandler.removeCallbacks(mDecrementCarrier);
			}

		} else if (v == leftBeatBtn){
			if (event.getAction() == MotionEvent.ACTION_DOWN){
				mHandler.removeCallbacks(mDecrementBeat);
				mHandler.postDelayed(mDecrementBeat, holdDelayMs);
			} else if (event.getAction() == MotionEvent.ACTION_UP){
				mHandler.removeCallbacks(mDecrementBeat);
			}			
		} else if (v == rightBeatBtn){
			if (event.getAction() == MotionEvent.ACTION_DOWN){
				mHandler.removeCallbacks(mIncrementBeat);
				mHandler.postDelayed(mIncrementBeat, holdDelayMs);
			} else if (event.getAction() == MotionEvent.ACTION_UP){
				mHandler.removeCallbacks(mIncrementBeat);
			}
		}


		return false;		
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub

		if (v == rightCarrBtn){
			carrierFrequency++;
			setCarrierText();
		} else if (v == leftCarrBtn) {
			carrierFrequency--;
			setCarrierText();
		} else if(v == leftBeatBtn) {
			beatFrequency--;
			setBeatText();
		} else if (v == rightBeatBtn) {
			beatFrequency++;
			setBeatText();
		} else if (v == executeBtn){
			carrierFrequency = Double.valueOf(txtCarrier.getText().toString());
			beatFrequency = Double.valueOf(txtBeat.getText().toString());
			
			setCarrierText();
			setBeatText();
			
			binaural.configureBeats(carrierFrequency, beatFrequency);
			

			binaural.start();
			if (chkBoxBackground){
				binaural.loadBackgroundTrack(this);
				binaural.enableBackgroundTrack();
			} else {
				binaural.disableBackgroundTrack();
			}
			
			
			
		} else if (v == muteBtn){

				binaural.stop();
			

		} else if (v == layoutBackground){
			beatFrequency = Double.valueOf(txtBeat.getText().toString());
			setBeatText();
			txtBeat.selectAll();
			
			
			carrierFrequency = Double.valueOf(txtCarrier.getText().toString());
			setCarrierText();
			txtCarrier.selectAll();
		}


	}
	
//
//	@Override
//	public void onFocusChange(View v, boolean hasFocus) {
//		// TODO Auto-generated method stub
//		
//		
//		 if (v == txtCarrier && !hasFocus){
//			carrierFrequency = Double.valueOf(txtCarrier.getText().toString());
//			setCarrierText();
//			txtCarrier.selectAll();
//			
//			
//		} else if (v == txtBeat && !hasFocus){
//			beatFrequency = Double.valueOf(txtBeat.getText().toString());
//			setBeatText();
//			txtBeat.selectAll();
//			
//		} 
//	}

}

