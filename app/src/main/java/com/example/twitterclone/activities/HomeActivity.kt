package com.example.twitterclone.activities

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.example.twitterclone.R
import com.example.twitterclone.databinding.ActivityHomeBinding
import com.example.twitterclone.databinding.ActivityLoginBinding
import com.example.twitterclone.fragments.HomeFragment
import com.example.twitterclone.fragments.MyActivityFragment
import com.example.twitterclone.fragments.SearchFragment
import com.example.twitterclone.fragments.TwitterFragment
import com.example.twitterclone.util.DATA_USERS
import com.example.twitterclone.util.User
import com.example.twitterclone.util.loadUrl
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding

    private lateinit var container: ViewPager
    private lateinit var tabs: TabLayout
    private lateinit var titleBar: TextView
    private lateinit var searchBar: CardView
    private lateinit var logo: ImageView
    private lateinit var fab: FloatingActionButton
    private lateinit var homeProgressLayout: LinearLayout

    private val firebaseDB = FirebaseFirestore.getInstance()
    private val firebaseAuth = FirebaseAuth.getInstance()
    private var sectionsPagerAdapter: SectionPageAdapter? = null
    private val homeFragment = HomeFragment()
    private val searchFragment = SearchFragment()
    private val myActivityFragment = MyActivityFragment()
    private var userId = FirebaseAuth.getInstance().currentUser?.uid
    private var currentFragment: TwitterFragment = homeFragment
    private var user: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        container = binding.container
        tabs = binding.tabs
        titleBar = binding.titleBar
        searchBar = binding.searchBar
        logo = binding.logo
        fab = binding.fab
        homeProgressLayout = binding.homeProgressLayout

        sectionsPagerAdapter = SectionPageAdapter(supportFragmentManager)

        container.adapter = sectionsPagerAdapter
        container.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabs))
        tabs.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(container))
        tabs.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener {
            override fun onTabReselected(p0: TabLayout.Tab?) {
            }

            override fun onTabUnselected(p0: TabLayout.Tab?) {
            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                when(tab?.position) {
                    0 -> {
                        titleBar.visibility = View.VISIBLE
                        titleBar.text = "Home"
                        searchBar.visibility = View.GONE
                        currentFragment = homeFragment
                    }
                    1 -> {
                        titleBar.visibility = View.GONE
                        searchBar.visibility = View.VISIBLE
                        currentFragment = searchFragment
                    }
                    2 -> {

                        titleBar.visibility = View.VISIBLE
                        titleBar.text = "My Activity"
                        searchBar.visibility = View.GONE
                        currentFragment = myActivityFragment
                    }
                }
            }
        })

        logo.setOnClickListener { view ->
            startActivity(ProfileActivity.newIntent(this))
        }

        fab.setOnClickListener {
            startActivity(TweetActivity.newIntent(this, userId, user?.username))
        }

        homeProgressLayout.setOnTouchListener { v, event -> true }

    }

    inner class SectionPageAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        override fun getItem(position: Int): Fragment {
            return when(position) {
                0 -> homeFragment
                1 -> searchFragment
                else -> myActivityFragment
            }
        }

        override fun getCount() = 3

    }

    override fun onResume() {
        super.onResume()
        userId = FirebaseAuth.getInstance().currentUser?.uid
        if(userId == null) {
            startActivity(LoginActivity.newIntent(this))
            finish()
        } else {
            populate()
        }
    }

    fun populate() {
        homeProgressLayout.visibility = View.VISIBLE
        firebaseDB.collection(DATA_USERS).document(userId!!).get()
            .addOnSuccessListener { documentSnapshot ->
                homeProgressLayout.visibility = View.GONE
                user = documentSnapshot.toObject(User::class.java)
                user?.imageUrl?.let {
                    logo.loadUrl(it, R.drawable.logo)
                }
                //updateFragmentUser()
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
                finish()
            }
    }

    companion object {
        fun newIntent(context: Context) = Intent(context, HomeActivity::class.java)
    }
}