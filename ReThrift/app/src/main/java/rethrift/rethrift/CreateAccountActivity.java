package rethrift.rethrift;

import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class CreateAccountActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_create_account);
  }

  // send user account info
  public void createAcct(View view) {
    Log.d("POST", "Creating JSON POST request");

    String stringUrl = "http://localhost:3000/users";
    // check that they have a connection
    ConnectivityManager cxnMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo networkInfo = cxnMgr.getActiveNetworkInfo();
    if (networkInfo != null && networkInfo.isConnected()) {
      new CreateAccountTask().execute(stringUrl);
    } else {
      new AlertDialog.Builder(this)
              .setTitle("Error")
              .setMessage("No network connection available.")
              .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                  dialog.dismiss();
                }
              })
              .setIcon(android.R.drawable.ic_dialog_alert)
              .show();
    }
  }

  private class CreateAccountTask extends AsyncTask<String, Void, String> {
    @Override
    protected String doInBackground(String... urls) {
      try {
        // TODO: figure out why connection isn't working
        URL url = new URL(urls[0]);
        System.setProperty("http.proxyHost", "localhost.com");
        System.setProperty("http.proxyPort", "3000");
        HttpURLConnection cxn = (HttpURLConnection) url.openConnection();
        cxn.setDoOutput(true);
        cxn.setRequestMethod("POST");
        cxn.setRequestProperty("Content-Type", "application/json");

        // TODO: replace with JSONObject
        String input = "{\"qty\":100,\"name\":\"iPad 4\"}";

        Log.d("POST", "Writing JSON POST request");
        OutputStreamWriter os = new OutputStreamWriter(cxn.getOutputStream());
        Log.d("POST", "Got output stream");
        os.write(input);
        Log.d("POST", "Writing bytes to output stream");
        os.close();
        Log.d("POST", "Finished writing JSON POST request");

        if (cxn.getResponseCode() != HttpURLConnection.HTTP_CREATED) {
          throw new RuntimeException("Failed -- HTTP error code: " + cxn.getResponseCode());
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(cxn.getInputStream()));

        String output;
        System.out.println("Output from server .... \n");
        while ((output = br.readLine()) != null) {
          System.out.println(output);
        }

        Log.d("POST", "Disconnecting...");
        cxn.disconnect();
        return "Successfully created account!";
      } catch (IOException e) {
        return "Unable to create account. Please try again later.";
      }
    }
  }
}
