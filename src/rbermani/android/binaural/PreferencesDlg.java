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

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class PreferencesDlg extends PreferenceActivity
{	
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		//getPrefs();
		addPreferencesFromResource(R.xml.preferences);
        
	}
	
	
}

