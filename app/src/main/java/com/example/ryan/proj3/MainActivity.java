package com.example.ryan.proj3;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "ParseJSON";
    private String MYURL = "";

    public static final int MAX_LINES = 15;
    private static final int SPACES_TO_INDENT_FOR_EACH_LEVEL_OF_NESTING = 2;

    JSONArray jsonArray;


    int numberentries = -1;
    int currententry = -1;


    private boolean isReachable;
    private TextView tvlastname;

    private List<String> pets;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        tvlastname = (TextView) findViewById(R.id.tvlastname);

        SharedPreferences myPref = PreferenceManager.getDefaultSharedPreferences(this);


        SharedPreferences.OnSharedPreferenceChangeListener listener  = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if(key.equals("pref_list")){
                    loadPics(sharedPreferences.getString(key, ""));
                }
            }
        };

        myPref.registerOnSharedPreferenceChangeListener(listener);

        //Lets remove the title
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        ConnectivityManager mManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo current = mManager.getActiveNetworkInfo();

        if(current == null){
            isReachable = false;
        }else{
            isReachable = (current.getState() == NetworkInfo.State.CONNECTED);
        }
        if (!isReachable) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("No Network Connection");
            builder.setMessage("The Network is unavailable. Please try your request again later.");
            builder.setPositiveButton("OK", null);
            builder.create().show();
        }else{
            pets = new ArrayList<String>();

            loadPics(myPref.getString("pref_list", ""));

        }

    }

    public void loadPics(String url){
        MYURL = url + "pets.json";

        pets.clear();
        //A common async task
        DownloadTask myTask = new DownloadTask(this);

        myTask.execute(MYURL);
//        Toast.makeText(this,MYURL,Toast.LENGTH_SHORT).show();
    }

    public void setErrorMessage(int statusCode){

        String error = "Issue when connecting to " + MYURL + " Server returned code " + statusCode;
        Toast.makeText(this,error, Toast.LENGTH_SHORT).show();
    }

    public void processJSON(String string) {
        try {
            JSONObject jsonobject = new JSONObject(string);

            //*********************************
            //makes JSON indented, easier to read
            Log.d(TAG, jsonobject.toString(SPACES_TO_INDENT_FOR_EACH_LEVEL_OF_NESTING));
//            tvRaw.setText(jsonobject.toString(SPACES_TO_INDENT_FOR_EACH_LEVEL_OF_NESTING));

            // you must know what the data format is, a bit brittle
            jsonArray = jsonobject.getJSONArray("pets");

            // how many entries
            numberentries = jsonArray.length();

            currententry = 0;
            while(currententry != numberentries) {
                setJSONUI(currententry); // parse out object currententry
                currententry++;
            }


            Log.i(TAG, "Number of entries " + numberentries);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void setJSONUI(int i) {
        if (jsonArray == null) {
            Log.e(TAG, "tried to dereference null jsonArray");
            return;
        }

        // gotta wrap JSON in try catches cause it throws an exception if you
        // try to
        // get a value that does not exist
        try {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
//            tvfirstname.setText(jsonObject.getString("firstname"));
            pets.add(jsonObject.getString("name"));
            tvlastname.setText(jsonObject.getString("name"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public void updateSpinner(){
        Spinner spinner = (Spinner) findViewById(R.id.spinner);

        ArrayAdapter myAdapter = new ArrayAdapter(this,android.R.layout.simple_spinner_item,pets);

        myAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        myAdapter.notifyDataSetChanged();

        spinner.setAdapter(myAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // On selecting a spinner item

                tvlastname.setText(parent.getItemAtPosition(position).toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        //noinspection SimplifiableIfStatement
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent myIntent = new Intent(this, SettingsActivity.class);
                startActivity(myIntent);
                break;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
