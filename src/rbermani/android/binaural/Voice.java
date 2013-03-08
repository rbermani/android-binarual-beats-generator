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

public class Voice {
	
	public Voice(VoiceType type, double amplitude, double carrierFrequency,
			double beatFrequency) {

		this.type = type;
		this.amplitude = amplitude;
		this.carrierFrequency = carrierFrequency;
		this.beatFrequency = beatFrequency;
	}
	
	public double getAmplitude() {
		return amplitude;
	}
	public void setAmplitude(double amplitude) {
		this.amplitude = amplitude;
	}
	public double getCarrierFrequency() {
		return carrierFrequency;
	}
	public void setCarrierFrequency(double carrierFrequency) {
		this.carrierFrequency = carrierFrequency;
	}
	public double getBeatFrequency() {
		return beatFrequency;
	}
	public void setBeatFrequency(double beatFrequency) {
		this.beatFrequency = beatFrequency;
	}
	public VoiceType getType() {
		return type;
	}
	
	
	private VoiceType type;
	
	private double amplitude;
	private double carrierFrequency;
	private double beatFrequency;
	
}
