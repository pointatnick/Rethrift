package rethrift.rethrift;

import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;

public class CreatePostActivity extends AppCompatActivity {
    private TextInputEditText title, price, description;
    private Spinner category;
    private String user;
    private double latitude, longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_create_post);
        title = (TextInputEditText) findViewById(R.id.title_field);

        price = (TextInputEditText) findViewById(R.id.price_field);
        //price.addTextChangedListener(new CurrencyTextWatcher());

        description = (TextInputEditText) findViewById(R.id.description_field);
        description.setHorizontallyScrolling(false);
        description.setMaxLines(Integer.MAX_VALUE);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            user = extras.getString("USERNAME");
            latitude = extras.getDouble("LATITUDE");
            longitude = extras.getDouble("LONGITUDE");
        }

        category = (Spinner) findViewById(R.id.category_spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.category_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        category.setAdapter(adapter);
    }

    public void addPost(View view){
        // check that they have a connection
        ConnectivityManager cxnMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cxnMgr.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            String stringUrl = "http://rethrift-1.herokuapp.com/posts/create/" + user;
            new CreatePostTask().execute(stringUrl);
            // go back to SalesboardActivity
            finish();
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

    // TODO: look at this again later
    /*
    private class CurrencyTextWatcher implements TextWatcher {
        private String current = "";

        public CurrencyTextWatcher() {}

        public void afterTextChanged(Editable s) {}

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if(!s.toString().equals(current)){
                price.removeTextChangedListener(this);

                String cleanString = s.toString().replaceAll("[$,.]", "");

                //double parsed = Double.parseDouble(cleanString);
                int parsed = Integer.parseInt(cleanString);
                String formatted = NumberFormat.getCurrencyInstance().format(parsed);   //.format(parsed/100);

                current = formatted;
                price.setText(formatted);

                price.addTextChangedListener(this);
            }
        }
    }
    */

    // AsyncTask which creates the post in the background
    private class CreatePostTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            try {
                return createAccountUrl(urls[0]);
            } catch (IOException e) {
                e.printStackTrace();
                return "Unable to create post";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            Log.d("CREATE ACCOUNT", result);
        }

            private String createAccountUrl(String myurl) throws IOException {
            OutputStream os = null;

            try {
                URL url = new URL(myurl);
                Log.d("URL", "" + url);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                // Starts the query
                conn.connect();
                os = conn.getOutputStream();

                JSONObject postInfoJson = new JSONObject();
                try {
                    postInfoJson.put("title", title.getText().toString())
                                .put("description", description.getText().toString())
                                .put("price", Double.parseDouble(price.getText().toString()))
                                .put("category", category.getSelectedItem().toString())
                                .put("state", "FRESH")
                                .put("latitude", latitude)
                                .put("longitude", longitude);
                    // TODO: add image
                    //.put("image", )

                    Log.d("JSONOBJECT", postInfoJson.toString(2));
                    // Write JSONObject to output stream
                    writeIt(os, postInfoJson.toString(2));
                    int response = conn.getResponseCode();
                    conn.disconnect();
                    return "good";
                } catch (JSONException e) {
                    e.printStackTrace();
                    return "Unable to create post";
                }
                // Makes sure that the OutputStream is closed after the app is finished using it.
            } finally {
                if (os != null) {
                    os.close();
                }
            }
        }

        // Writes an OutputStream
        private void writeIt(OutputStream stream, String msg) throws IOException {
            Writer writer = new OutputStreamWriter(stream, "UTF-8");
            writer.write(msg);
            writer.flush();
            writer.close();
        }
    }
}
