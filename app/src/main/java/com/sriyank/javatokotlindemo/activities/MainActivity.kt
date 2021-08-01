package com.sriyank.javatokotlindemo.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.TextInputLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.View
import android.widget.EditText
import com.sriyank.javatokotlindemo.R
import com.sriyank.javatokotlindemo.app.Constants
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    /** Java equivalent would be
     *
     * Private final String TAG = MainActivity.class.getSimpleName();*/
    companion object {

        private val TAG: String = MainActivity::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(toolbar)

        /**Accessing the TAG in MainActivity.kt
         * To access in any other class,
         * Make it public

        MainActivity.TAG

         */

    }

    /** Save app username in Shared Preferences*/
    fun saveName(view: View) {

        /**Save person's name in SharedPreferences using editor(Kotlin)
         * This name will be used in the drawer
         * */

        //Validation layer

        if(isNotEmpty(etName, inputLayoutName)) {
            val personName = etName.text.toString()
            //Initialize SharedPreferences file
            val sp = getSharedPreferences(Constants.APP_SHARED_PREFERENCES, Context.MODE_PRIVATE)
            //get reference to editor
            val editor = sp.edit()
            //Put person name within file using editor
            editor.putString(Constants.KEY_PERSON_NAME, personName)
            //Commit
            editor.apply()
        }
    }

    /**Search repositories on Github after passing data to DisplayActivity*/
    fun listRepositories(view: View) {

        /**Add validation rule */

        if (isNotEmpty(etRepoName, inputLayoutRepoName)) {
            /**Fetch specific EditText using KotlinAndroid Extensions*/
            val queryRepo = etRepoName.text.toString()
            val repoLanguage = etLanguage.text.toString()

            /**Passing Intents using the Constance Java Class to DisplayActivity*/
            val intent = Intent(this@MainActivity, DisplayActivity::class.java)
            intent.putExtra(Constants.KEY_QUERY_TYPE, Constants.SEARCH_BY_REPO)
            intent.putExtra(Constants.KEY_REPO_SEARCH, queryRepo)
            intent.putExtra(Constants.KEY_LANGUAGE, repoLanguage)
            startActivity(intent)
        }



    }


    /**Search repositories of a particular Github user after passing data to DisplayActivity*/
    fun listUserRepositories(view: View) {

        if(isNotEmpty(etGithubUser, inputLayoutGithubUser)) {


            val githubUser = etGithubUser.text.toString()

            //Code to show Kotlin Reflection classes.
            val cls: Class<DisplayActivity> = DisplayActivity::class.java

            val intent = Intent(this@MainActivity, cls)

            //cls = DisplayActivity::class.java
            /**Specify query type*/
            intent.putExtra(Constants.KEY_QUERY_TYPE, Constants.SEARCH_BY_USER)
            /**Pass the githubUser intent*/
            intent.putExtra(Constants.KEY_GITHUB_USER, githubUser)
            startActivity(intent)
        }
    }

    /** Validation Rule
     * This function checks to see if the editTexts are blank
     * and returns an error to the user.
     */

    fun isNotEmpty(editText: EditText, textInputLayout: TextInputLayout) : Boolean {

        if (editText.text.toString().isEmpty()){
            //set validation error
            textInputLayout.error = "Cannot be blank !"
            return false
        }
        else{
            textInputLayout.isErrorEnabled = false
            return true
        }
    }







}