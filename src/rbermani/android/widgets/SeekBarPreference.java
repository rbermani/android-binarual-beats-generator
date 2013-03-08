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
package rbermani.android.widgets;

import rbermani.android.binaural.R;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class SeekBarPreference extends Preference 
implements OnSeekBarChangeListener{


	public static int maximum    = 100;
	public static int interval   = 1;

	private float oldValue = 50;
	private TextView monitorBox;


	public SeekBarPreference(Context context) {
		super(context);
	}

	public SeekBarPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public SeekBarPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected View onCreateView(ViewGroup parent){
		LinearLayout layout = new LinearLayout(getContext());

		LinearLayout.LayoutParams descText = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		descText.gravity = Gravity.LEFT;
		descText.weight  = 1.0f;


		LinearLayout.LayoutParams sliderBar = new LinearLayout.LayoutParams(
				150,
				LayoutParams.WRAP_CONTENT);
		sliderBar.gravity = Gravity.RIGHT;


		LinearLayout.LayoutParams sliderVal = new LinearLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		sliderVal.gravity = Gravity.CENTER;

		layout.setPadding(15, 5, 10, 5);
		layout.setOrientation(LinearLayout.HORIZONTAL);

		TextView view = new TextView(getContext());
		view.setText(getTitle());
		view.setTextSize(14);
		view.setTypeface(Typeface.SANS_SERIF, Typeface.BOLD);
		view.setGravity(Gravity.LEFT);
		view.setLayoutParams(descText);
		view.setPadding(0, 0, 5, 0);


		SeekBar bar = new SeekBar(getContext());
		bar.setMax(maximum);
		bar.setProgress((int)this.oldValue);
		bar.setLayoutParams(sliderBar);
		bar.setOnSeekBarChangeListener(this);
		bar.setPadding(0, 0, 5, 0);
		
		this.monitorBox = new TextView(getContext());
		this.monitorBox.setTextSize(12);
		this.monitorBox.setTypeface(Typeface.MONOSPACE, Typeface.ITALIC);
		this.monitorBox.setLayoutParams(sliderVal);
		this.monitorBox.setPadding(0, 5, 5, 0);
		
		this.monitorBox.setText(bar.getProgress()+"");


		layout.addView(view);
		layout.addView(bar);
		layout.addView(this.monitorBox);
		layout.setId(android.R.id.widget_frame);


		return layout; 

	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {

		progress = Math.round(((float)progress)/interval)*interval;

		if(!callChangeListener(progress)){
			seekBar.setProgress((int)this.oldValue); 
			return; 
		}

		seekBar.setProgress(progress);
		this.oldValue = progress;
		this.monitorBox.setText(progress+"");
		updatePreference(progress);

		notifyChanged();
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
	}


	@Override 
	protected Object onGetDefaultValue(TypedArray ta,int index){

		int dValue = (int)ta.getInt(index,50);

		return validateValue(dValue);
	}


	@Override
	protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {

		int temp = restoreValue ? getPersistedInt(50) : (Integer)defaultValue;

		if(!restoreValue)
			persistInt(temp);

		this.oldValue = temp;
	}


	private int validateValue(int value){

		if(value > maximum)
			value = maximum;
		else if(value < 0)
			value = 0;
		else if(value % interval != 0)
			value = Math.round(((float)value)/interval)*interval;  


		return value;  
	}


	private void updatePreference(int newValue){

		SharedPreferences.Editor editor =  getEditor();
		editor.putInt(getKey(), newValue);
		editor.commit();
	}

}
