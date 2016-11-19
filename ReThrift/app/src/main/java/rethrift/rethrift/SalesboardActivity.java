package rethrift.rethrift;

import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v7.app.ActionBarDrawerToggle;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class SalesboardActivity extends AppCompatActivity {
    private ListView mDrawerList;
    private ArrayAdapter<String> mAdapter;
    private String user, name;
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_salesboard);

        // for search
        // getIntent and pass to handler
        handleIntent(getIntent());
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) findViewById(R.id.search_bar);
        //sets up a submit button
        searchView.setSubmitButtonEnabled(true);
        //assumes current activity is the searchable activity
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        RecyclerView card_list = (RecyclerView) findViewById(R.id.card_list);
        card_list.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        card_list.setLayoutManager(llm);

        // retrieve posts
        try {
            String stringUrl = "http://rethrift-1.herokuapp.com/posts/all";
            PostAdapter ca = new PostAdapter(new GetPostsTask().execute(stringUrl).get());
            card_list.setAdapter(ca);
        } catch (InterruptedException e) {
            new AlertDialog.Builder(this)
                    .setTitle("Error")
                    .setMessage("Unable to load Salesboard")
                    .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        } catch (ExecutionException e) {
            new AlertDialog.Builder(this)
                    .setTitle("Error")
                    .setMessage("Unable to load Salesboard")
                    .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
        //PostAdapter ca = new PostAdapter(createList());
        //card_list.setAdapter(ca);

        // Initialize Navigation View
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            user = extras.getString("USERNAME");
            name = extras.getString("FIRSTNAME");
        }
        navigationView = (NavigationView) findViewById(R.id.navigation_view);
        //Setting Navigation View Item Selected Listener to handle the item click of the navigation menu
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {

            // This method will trigger on item Click of navigation menu
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {

                //Checking if the item is in checked state or not, if not make it in checked state
                if(menuItem.isChecked()) menuItem.setChecked(false);
                else menuItem.setChecked(true);

                //Closing drawer on item click
                drawerLayout.closeDrawers();

                //Check to see which item was being clicked and perform appropriate action
                switch (menuItem.getItemId()){
                    // For rest of the options we just show a toast on click
                    case R.id.profile:
                        Toast.makeText(getApplicationContext(),"profile Selected",Toast.LENGTH_SHORT).show();
                        return true;
                    case R.id.watch_list:
                        Toast.makeText(getApplicationContext(),"watchlist Selected",Toast.LENGTH_SHORT).show();
                        return true;
                    case R.id.my_posts:
                        Toast.makeText(getApplicationContext(),"myposts Selected",Toast.LENGTH_SHORT).show();
                        return true;

                    default:
                        Toast.makeText(getApplicationContext(),"Somethings Wrong",Toast.LENGTH_SHORT).show();
                        return true;
                }
            }
        });

        // Initializing Drawer Layout and ActionBarToggle
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this,drawerLayout, R.string.openDrawer, R.string.closeDrawer){

            @Override
            public void onDrawerClosed(View drawerView) {
                // Code here will be triggered once the drawer closes as we dont want anything to happen so we leave this blank
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                // Code here will be triggered once the drawer open as we dont want anything to happen so we leave this blank
                super.onDrawerOpened(drawerView);
            }
        };
        //Setting the actionbarToggle to drawer layout
        drawerLayout.setDrawerListener(actionBarDrawerToggle);

        //calling sync state is necessay or else your hamburger icon wont show up
        actionBarDrawerToggle.syncState();


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
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    // sales board (center screen)
    public void createPost(View view){
        Intent intent = new Intent(this, CreatePostActivity.class);
        intent.putExtra("USERNAME", user);
        startActivity(intent);
    }

    //for search
    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent){
        if(Intent.ACTION_SEARCH.equals(intent.getAction())){
            //obtain the query string from Intent.ACTION_SEARCH
            String query = intent.getStringExtra(SearchManager.QUERY);
            doMySearch(query);
        }
    }

    private void doMySearch(String query){
        //TO-DO: pass to back-end here
        //TO-DO: change return type to whatever back-end returns
        //TO-DO: pass to Kexin's adapter? The results of this search
        //       is shown on the Salesboard view.
        //?? So open up Salesboard and pass result to show on the view?
    }

    // TODO: replace with AsyncTask that grabs 10 most recent posts
    private List<Post> createList() {
        List<Post> result = new ArrayList<>();
        Post ci = new Post("Title goes here", "$10", "FRESH", "5678 Alley Drive", "Test description", "Test category", "First Last", "firstlast");
        Post di = new Post("Another title", "$5", "PENDING SALE", "1234 Park Lane", "This is a test", "Some test", "Last First", "lastfirst");
        result.add(ci);
        result.add(di);
        return result;
    }


    // AsyncTask that checks if the password is correct and logs in the user
    private class GetPostsTask extends AsyncTask<String, Void, List<Post>> {
        @Override
        protected List<Post> doInBackground(String... urls) {
            try {
                return getPosts(urls[0]);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        private List<Post> getPosts(String myurl) throws IOException {
            InputStream is = null;
            int len = 5000;

            try {
                URL url = new URL(myurl);
                Log.d("URL", "" + url);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);

                // Starts the query
                conn.connect();
                is = conn.getInputStream();

                // Convert the InputStream into a string
                String postArray = readIt(is, len);
                Log.d("RESULT", postArray);

                try {
                    JSONArray postArrayJson = new JSONArray(postArray);
                    List<Post> postList = new ArrayList<>();
                    for (int i = 0; i < postArrayJson.length(); i++) {
                        JSONObject postJson = postArrayJson.getJSONObject(i);
                        int postId = postJson.getInt("id");
                        int userId = postJson.getInt("UserId");
                        try {
                            JSONObject userJson = new FindUserTask().execute("http://rethrift-1.herokuapp.com/users/" + userId).get();
                            JSONObject locationJson = new FindLocationTask().execute("http://rethrift-1.herokuapp.com/").get();
                            postList.add(
                                    new Post(postJson.getString("title"),
                                            postJson.getString("price"),
                                            postJson.getString("state"),
                                            // TODO: complete location retrieval
                                            locationJson.getString("location"),
                                            postJson.getString("description"),
                                            postJson.getString("category"),
                                            userJson.getString("name"),
                                            userJson.getString("username")));
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        }
                    }
                    return postList;
                } catch (JSONException e) {
                    e.printStackTrace();
                    return null;
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return null;
            } finally {
                // Makes sure that the InputStream is closed after the app is finished using it.
                if (is != null) {
                    is.close();
                }
            }
        }

        // Reads an InputStream and converts it to a String.
        private String readIt(InputStream stream, int len) throws IOException {
            Reader reader = new InputStreamReader(stream, "UTF-8");
            char[] buffer = new char[len];
            reader.read(buffer);
            return new String(buffer);
        }
    }


    // AsyncTask that grabs info about the user
    private class FindUserTask extends AsyncTask<String, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(String... urls) {
            try {
                return findUser(urls[0]);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        private JSONObject findUser(String myurl) throws IOException {
            InputStream is = null;
            int len = 5000;

            try {
                URL url = new URL(myurl);
                Log.d("URL", "" + url);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);

                // Starts the query
                conn.connect();
                is = conn.getInputStream();

                // Convert the InputStream into a string
                String userInfoString = readIt(is, len);
                Log.d("RESULT", userInfoString);

                try {
                    JSONObject userInfoJson = new JSONObject(userInfoString);
                    JSONObject user = new JSONObject();
                    user.put("name", userInfoJson.getString("name"))
                        .put("username", userInfoJson.getString("username"));
                    return user;
                } catch (JSONException e) {
                    e.printStackTrace();
                    return null;
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return null;
            } finally {
                // Makes sure that the InputStream is closed after the app is finished using it.
                if (is != null) {
                    is.close();
                }
            }
        }

        // Reads an InputStream and converts it to a String.
        private String readIt(InputStream stream, int len) throws IOException {
            Reader reader = new InputStreamReader(stream, "UTF-8");
            char[] buffer = new char[len];
            reader.read(buffer);
            return new String(buffer);
        }
    }


    // TODO: AsyncTask that grabs post location
    private class FindLocationTask extends AsyncTask<String, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(String... urls) {
            try {
                return findUser(urls[0]);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        private JSONObject findUser(String myurl) throws IOException {
            InputStream is = null;
            int len = 5000;

            try {
                URL url = new URL(myurl);
                Log.d("URL", "" + url);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);

                // Starts the query
                conn.connect();
                is = conn.getInputStream();

                // Convert the InputStream into a string
                String userInfoString = readIt(is, len);
                Log.d("RESULT", userInfoString);

                try {
                    JSONObject userInfoJson = new JSONObject(userInfoString);
                    JSONObject user = new JSONObject();
                    user.put("name", userInfoJson.getString("name"))
                            .put("username", userInfoJson.getString("username"));
                    return user;
                } catch (JSONException e) {
                    e.printStackTrace();
                    return null;
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return null;
            } finally {
                // Makes sure that the InputStream is closed after the app is finished using it.
                if (is != null) {
                    is.close();
                }
            }
        }

        // Reads an InputStream and converts it to a String.
        private String readIt(InputStream stream, int len) throws IOException {
            Reader reader = new InputStreamReader(stream, "UTF-8");
            char[] buffer = new char[len];
            reader.read(buffer);
            return new String(buffer);
        }
    }

}
