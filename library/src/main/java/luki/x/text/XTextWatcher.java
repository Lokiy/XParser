/**
 * Copyright (C) 2014 Luki(liulongke@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package luki.x.text;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

/**
 * @author Luki
 *
 */
public class XTextWatcher implements TextWatcher {

	protected EditText mTarget;

	/**
	 * 
	 */
	public XTextWatcher(EditText tv) {
		this.mTarget = tv;
	}


	/* (non-Javadoc)
	 * @see android.text.TextWatcher#beforeTextChanged(java.lang.CharSequence, int, int, int)
	 */
	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

	/* (non-Javadoc)
	 * @see android.text.TextWatcher#onTextChanged(java.lang.CharSequence, int, int, int)
	 */
	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {}

	/* (non-Javadoc)
	 * @see android.text.TextWatcher#afterTextChanged(android.text.Editable)
	 */
	@Override
	public void afterTextChanged(Editable s) {}
	
	protected void setSelection(){
		mTarget.setSelection(mTarget.getText().length());
	}

	public String wipeOffChar(String s, char cr) {
		char[] cs = s.toCharArray();
		StringBuffer sb = new StringBuffer();
		for (char c : cs) {
			if (c != cr) {
				sb.append(c);
			}
		}
		return sb.toString();
	}

}