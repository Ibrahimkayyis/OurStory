package com.dicoding.picodiploma.loginwithanimation.view

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class PasswordEditText(context: Context, attrs: AttributeSet) : TextInputEditText(context, attrs) {

    init {
        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.length < 8) {
                    (parent.parent as? TextInputLayout)?.error = "Password tidak boleh kurang dari 8 karakter"
                } else {
                    (parent.parent as? TextInputLayout)?.error = null
                }
            }

            override fun afterTextChanged(editable: Editable?) {}
        })
    }
}
