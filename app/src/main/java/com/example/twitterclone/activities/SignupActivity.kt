package com.example.twitterclone.activities

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import com.example.twitterclone.databinding.ActivitySignupBinding
import com.example.twitterclone.util.DATA_USERS
import com.example.twitterclone.util.User
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding

    private lateinit var usernameET: TextInputEditText
    private lateinit var usernameTIL: TextInputLayout
    private lateinit var emailET: TextInputEditText
    private lateinit var emailTIL: TextInputLayout
    private lateinit var passwordET: TextInputEditText
    private lateinit var passwordTIL: TextInputLayout
    private lateinit var signupProgressLayout: LinearLayout

    private val firebaseDB = FirebaseFirestore.getInstance()
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firebaseAuthListener = FirebaseAuth.AuthStateListener {
        val user = firebaseAuth.currentUser?.uid
        user?.let {
            startActivity(HomeActivity.newIntent(this))
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        usernameET = binding.usernameET
        usernameTIL = binding.usernameTIL
        emailET = binding.emailET
        emailTIL = binding.emailTIL
        passwordET = binding.passwordET
        passwordTIL = binding.passwordTIL
        signupProgressLayout = binding.signupProgressLayout

        setTextChangeListener(usernameET, usernameTIL)
        setTextChangeListener(emailET, emailTIL)
        setTextChangeListener(passwordET, passwordTIL)

        signupProgressLayout.setOnTouchListener { v, event -> true }
    }



    fun setTextChangeListener(et: EditText, til: TextInputLayout) {
        et.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                til.isErrorEnabled = false
            }

        })
    }

    fun onSignup(v: View) {
        var proceed = true
        if(usernameET.text.isNullOrEmpty()) {
            usernameTIL.error = "Username is required"
            usernameTIL.isErrorEnabled = true
            proceed = false
        }
        if(emailET.text.isNullOrEmpty()) {
            emailTIL.error = "Email is required"
            emailTIL.isErrorEnabled = true
            proceed = false
        }
        if(passwordET.text.isNullOrEmpty()) {
            passwordTIL.error = "Password is required"
            passwordTIL.isErrorEnabled = true
            proceed = false
        }
        if(proceed) {
            signupProgressLayout.visibility = View.VISIBLE
            firebaseAuth.createUserWithEmailAndPassword(emailET.text.toString(), passwordET.text.toString())
                .addOnCompleteListener { task ->
                    if(!task.isSuccessful) {
                        Toast.makeText(this@SignupActivity, "Signup error: ${task.exception?.localizedMessage}", Toast.LENGTH_SHORT).show()
                    } else {
                        val email = emailET.text.toString()
                        val name = usernameET.text.toString()
                        val user = User(email, name, "", arrayListOf(), arrayListOf())
                        firebaseDB.collection(DATA_USERS).document(firebaseAuth.uid!!).set(user)
                    }
                    signupProgressLayout.visibility = View.GONE
                }
                .addOnFailureListener { e ->
                    e.printStackTrace()
                    signupProgressLayout.visibility = View.GONE
                }
        }
    }

    fun goToLogin(v: View) {
        startActivity(LoginActivity.newIntent(this))
        finish()
    }

    override fun onStart() {
        super.onStart()
        firebaseAuth.addAuthStateListener(firebaseAuthListener)
    }

    override fun onStop() {
        super.onStop()
        firebaseAuth.removeAuthStateListener(firebaseAuthListener)
    }

    companion object {
        fun newIntent(context: Context) = Intent(context, SignupActivity::class.java)
    }
}