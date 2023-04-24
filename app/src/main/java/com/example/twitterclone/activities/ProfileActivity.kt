package com.example.twitterclone.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.example.twitterclone.R
import com.example.twitterclone.databinding.ActivityHomeBinding
import com.example.twitterclone.databinding.ActivityProfileBinding
import com.example.twitterclone.util.*
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var profileProgressLayout: LinearLayout
    private lateinit var usernameET: TextInputEditText
    private lateinit var emailET: TextInputEditText
    private lateinit var photoIV: ImageView


    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firebaseDB = FirebaseFirestore.getInstance()
    private val firebaseStorage = FirebaseStorage.getInstance().reference
    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    private var imageUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        profileProgressLayout = binding.profileProgressLayout
        usernameET = binding.usernameET
        emailET = binding.emailET
        photoIV = binding.photoIV

        if(userId == null) {
            finish()
        }

        profileProgressLayout.setOnTouchListener { v, event -> true }

        photoIV.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, REQUEST_CODE_PHOTO)
        }

        populateInfo()
    }

    fun populateInfo() {
        profileProgressLayout.visibility = View.VISIBLE
        firebaseDB.collection(DATA_USERS).document(userId!!).get()
            .addOnSuccessListener { documentSnapshot ->
                val user = documentSnapshot.toObject(User::class.java)
                usernameET.setText(user?.username, TextView.BufferType.EDITABLE)
                emailET.setText(user?.email, TextView.BufferType.EDITABLE)
                imageUrl = user?.imageUrl
                imageUrl?.let {
                    photoIV.loadUrl(user?.imageUrl, R.drawable.logo)
                }
                profileProgressLayout.visibility = View.GONE
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
                finish()
            }
    }

    fun onApply(v: View) {
        profileProgressLayout.visibility = View.VISIBLE
        val username = usernameET.text.toString()
        val email = emailET.text.toString()
        val map = HashMap<String, Any>()
        map[DATA_USER_USERNAME] = username
        map[DATA_USER_EMAIL] = email

        firebaseDB.collection(DATA_USERS).document(userId!!).update(map)
            .addOnSuccessListener {
                Toast.makeText(this, "Update successful", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
                Toast.makeText(this, "Update failed. Please try again.", Toast.LENGTH_SHORT).show()
                profileProgressLayout.visibility = View.GONE
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_PHOTO) {
            storeImage(data?.data)
        }
    }

    fun storeImage(imageUri: Uri?) {
        imageUri?.let {
            Toast.makeText(this, "Uploading...", Toast.LENGTH_SHORT).show()
            profileProgressLayout.visibility = View.VISIBLE

            val filePath = firebaseStorage.child(DATA_IMAGES).child(userId!!)
            filePath.putFile(imageUri)
                .addOnSuccessListener {
                    filePath.downloadUrl
                        .addOnSuccessListener {uri ->
                            val url = uri.toString()
                            firebaseDB.collection(DATA_USERS).document(userId!!).update(DATA_USER_IMAGE_URL, url)
                                .addOnSuccessListener {
                                    imageUrl = url
                                    photoIV.loadUrl(imageUrl, R.drawable.logo)
                                }
                            profileProgressLayout.visibility = View.GONE
                        }
                        .addOnFailureListener {
                            onUploadFailure()
                        }
                }
                .addOnFailureListener {
                    onUploadFailure()
                }
        }
    }

    fun onUploadFailure() {
        Toast.makeText(this, "Image upload failed. Please try agail later.", Toast.LENGTH_SHORT).show()
        profileProgressLayout.visibility = View.GONE
    }

    fun onSignout(v: View) {
        firebaseAuth.signOut()
        finish()
    }

    companion object {
        fun newIntent(context: Context) = Intent(context, ProfileActivity::class.java)
    }
}