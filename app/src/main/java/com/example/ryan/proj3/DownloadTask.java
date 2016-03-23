package com.example.ryan.proj3;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Created by Ryan on 3/21/2016.
 */
public class DownloadTask extends AsyncTask<String, Void, String> {
    private static final int READ_THIS_AMOUNT = 8096;
    private static final String TAG = "DownloadTask";
    MainActivity myActivity;
    SettingsActivity temp;

    // 1 second
    private static final int TIMEOUT = 1000;
    private String myQuery = "";


    DownloadTask(MainActivity activity) {
        attach(activity);
    }

    public DownloadTask setnameValuePair(String name, String value) {
        try {
            if (name.length() != 0 && value.length() != 0) {

                // if 1st pair that include ? otherwise use the joiner char &
                if (myQuery.length() == 0)
                    myQuery += "?";
                else
                    myQuery += "&";

                myQuery += name + "=" + URLEncoder.encode(value, "utf-8");
            }
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return this;
    }

    @Override
    protected String doInBackground(String... params) {
// site we want to connect to
        String myURL = params[0];

        try {
            URL url = new URL(myURL + myQuery);

            // this does no network IO
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // can further configure connection before getting data
            // cannot do this after connected
            connection.setRequestMethod("GET");
            connection.setReadTimeout(TIMEOUT);
            connection.setConnectTimeout(TIMEOUT);
//            connection.setRequestProperty("Accept-Charset", "UTF-8");
            // this opens a connection, then sends GET & headers

            connection.connect();
            // wrap in finally so that stream bis is sure to close
            // and we disconnect the HttpURLConnection
            BufferedReader in = null;
            try {

                // lets see what we got make sure its one of
                // the 200 codes (there can be 100 of them
                // http_status / 100 != 2 does integer div any 200 code will = 2
                int statusCode = connection.getResponseCode();

                if (statusCode / 100 != 2) {
//                    myActivity.setErrorMessage(statusCode);
                    String error = "Issue when connecting to " + myURL + " Server returned code " + statusCode;
                    Toast.makeText(myActivity, error, Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error-connection.getResponseCode returned "
                            + Integer.toString(statusCode));
                    return null;
                }

                in = new BufferedReader(new InputStreamReader(connection.getInputStream()), READ_THIS_AMOUNT);

                // the following buffer will grow as needed
                String myData;
                StringBuffer sb = new StringBuffer();

                while ((myData = in.readLine()) != null) {
                    sb.append(myData);
                }
                return sb.toString();

            } finally {
                // close resource no matter what exception occurs
                in.close();
                connection.disconnect();
            }
        } catch (Exception exc) {
            exc.printStackTrace();
            return null;
        }
    }
    @Override
    protected void onPostExecute(String result) {
        if (myActivity != null) {
            myActivity.processJSON(result);
            myActivity.updateSpinner();
        }
    }
    @Override
    protected void onCancelled() {
        // TODO Auto-generated method stub
        super.onCancelled();
    }

    /**
     * important do not hold a reference so garbage collector can grab old
     * defunct dying activity
     */
    void detach() {
        myActivity = null;
    }

    /**
     * @param activity
     *            grab a reference to this activity, mindful of leaks
     */
    void attach(MainActivity activity) {
        this.myActivity = activity;
    }

}
