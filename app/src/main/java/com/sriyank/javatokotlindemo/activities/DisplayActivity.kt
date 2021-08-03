package com.sriyank.javatokotlindemo.activities

import android.content.Context
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.MenuItem
import com.sriyank.javatokotlindemo.R
import com.sriyank.javatokotlindemo.adapters.DisplayAdapter
import com.sriyank.javatokotlindemo.app.Constants
import com.sriyank.javatokotlindemo.app.Util
import com.sriyank.javatokotlindemo.models.Repository
import com.sriyank.javatokotlindemo.models.SearchResponse
import com.sriyank.javatokotlindemo.retrofit.GithubAPIService
import com.sriyank.javatokotlindemo.retrofit.RetrofitClient
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_display.*
import kotlinx.android.synthetic.main.header.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class DisplayActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {


    private lateinit var displayAdapter: DisplayAdapter
    private var browsedRepositories: List<Repository?> = mutableListOf()
    private val githubAPIService: GithubAPIService by lazy {
        RetrofitClient.getGithubAPIService()
    }
    private var mRealm: Realm? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_display)


        setSupportActionBar(toolbar)
        supportActionBar!!.title = "Showing Browsed Results"

        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        recyclerView.layoutManager = layoutManager

        /**Function to setUsername on the drawer display*/
        setAppUsername()


        mRealm = Realm.getDefaultInstance()


        navigationView.setNavigationItemSelectedListener(this)


        val drawerToggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close)
        drawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.syncState()

        val intent = intent
        if (intent.getIntExtra(Constants.KEY_QUERY_TYPE, -1) == Constants.SEARCH_BY_REPO) {
            val queryRepo = intent.getStringExtra(Constants.KEY_REPO_SEARCH)
            val repoLanguage = intent.getStringExtra(Constants.KEY_LANGUAGE)
            fetchRepositories(queryRepo, repoLanguage)
        } else {
            val githubUser = intent.getStringExtra(Constants.KEY_GITHUB_USER)
            fetchUserRepositories(githubUser)
        }
    }

    private fun setAppUsername() {
        //Retrieve data from SharedPreferences

        val sp = getSharedPreferences(Constants.APP_SHARED_PREFERENCES, Context.MODE_PRIVATE)
        //Pull out the value. Set Default value incase user retrieval fails
        val personName = sp.getString(Constants.KEY_PERSON_NAME, "User")
        /** Fetch the headerView of NavigationView*/
        val headerview = navigationView.getHeaderView(0)
        //Fetch the textView using headerview and set value to personName
        headerview.txvName.text = personName

    }

    private fun fetchUserRepositories(githubUser: String) {

        /** Asynchronous call using method enqueue. object is used in Kotlin to create
         * an object of the annonymous inner class.
         * !! means Not Null
         */
        githubAPIService!!.searchRepositoriesByUser(githubUser).enqueue(object : Callback <List<Repository>>
        {
            override fun onResponse(call: Call<List<Repository>>, response: Response<List<Repository>>) {
            //If else condition if success or a failure
                if(response.isSuccessful){
                    //msg if successful
                    Log.i(TAG, "Posts loaded from API $response")

                    //Initialize browsedRepositories
                    browsedRepositories = response.body()!!

                    //If not empty initialize recyclerview
                    if (browsedRepositories!!.isNotEmpty()){
                       setupRecyclerView(browsedRepositories)
                    } else {
                        Util.showMessage(this@DisplayActivity, "No items found")
                    }


                }else
                    Log.i(TAG, "Error $response")
                    Util.showErrorMessage(this@DisplayActivity, response.errorBody()!!)


            }

            override fun onFailure(call: Call<List<Repository>>, t: Throwable) {
                    Util.showMessage(this@DisplayActivity, t.message)
            }

        })

    }

    private fun fetchRepositories(queryRepo: String, repoLanguage: String?) {

        var queryRepo = queryRepo
        val query: MutableMap<String, String> = HashMap()

        if (repoLanguage != null && !repoLanguage.isEmpty())
            queryRepo += " language:$repoLanguage"
            query["q"] = queryRepo

        githubAPIService!!.searchRepositories(query).enqueue(object : Callback<SearchResponse> {

            override fun onResponse(call: Call<SearchResponse>, response: Response<SearchResponse>) {

                if (response.isSuccessful) {
                    Log.i(TAG, "posts loaded from API $response")
                    browsedRepositories = response.body()!!.items
                    if ((browsedRepositories)!!.isNotEmpty())
                        setupRecyclerView(browsedRepositories)
                    else
                        Util.showMessage(this@DisplayActivity, "No Items Found")
                } else {
                    Log.i(TAG, "error $response")
                    Util.showErrorMessage(this@DisplayActivity, response.errorBody())
                }
            }

            override fun onFailure(call: Call<SearchResponse>, t: Throwable) {
                Util.showMessage(this@DisplayActivity, t.toString())
            }
        })
    }

    private fun setupRecyclerView(items: List<Repository?>?) {
        displayAdapter = DisplayAdapter(this, items)
        recyclerView!!.adapter = displayAdapter
    }

    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
        menuItem.isChecked = true
        closeDrawer()
        when (menuItem.itemId) {
            R.id.item_bookmark -> {
                showBookmarks()
                supportActionBar!!.title = "Showing Bookmarks"
            }
            R.id.item_browsed_results -> {
                showBrowsedResults()
                supportActionBar!!.title = "Showing Browsed Results"
            }
        }
        return true
    }

    private fun showBrowsedResults() {
        displayAdapter!!.swap(browsedRepositories)
    }

    private fun showBookmarks() {
        mRealm!!.executeTransaction { realm ->
            val repositories = realm.where(Repository::class.java).findAll()
            displayAdapter!!.swap(repositories)
        }
    }

    private fun closeDrawer() {
        drawerLayout!!.closeDrawer(GravityCompat.START)
    }

    override fun onBackPressed() {
        if (drawerLayout!!.isDrawerOpen(GravityCompat.START)) closeDrawer() else {
            super.onBackPressed()
            mRealm!!.close()
        }
    }

    companion object {
        private val TAG = DisplayActivity::class.java.simpleName
    }
}