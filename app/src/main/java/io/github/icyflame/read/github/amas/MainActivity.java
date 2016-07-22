package io.github.icyflame.read.github.amas;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import com.google.gson.JsonObject;

import java.util.List;
import java.util.Locale;

import in.uncod.android.bypass.Bypass;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, MainAdapter.parentProvidesOnClickListener {

    public static final String TAG = "activity-main";
    private static final String ANSWER_NOT_FOUND_REASONS = "Although this issue was closed, the repository " +
            "owner hasn't commented on the issue.";
    private static final String ANSWER_NOT_FOUND = "Answer from the repository owner not found!";
    private static final CharSequence FETCHING_ISSUES = "Fetching all closed issues for this repository";
    private RecyclerView mRecyclerView;
    private MainAdapter mAdapter;
    private Retrofit mRetrofitInstance;
    private GitHubAPI mApiInstance;
    private String mUser = "sindresorhus";
    private String mRepo = "ama";
    private CharSequence FETCHING_COMMENTS = "Fetching the answer from GitHub";
    private CharSequence FETCHING_DESCRIPTION = "Hold on!";
    private ProgressDialog mWaiting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder b1 = new AlertDialog.Builder(MainActivity.this);
                b1.setTitle("Enter username of the user who's AMA you would like to check out:");
                final EditText input = new EditText(MainActivity.this);
                b1.setView(input);
                b1.setPositiveButton("OKAY", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mUser = input.getText().toString();
                        updateAdapterListing();
                    }
                });
                b1.setNegativeButton("CLOSE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                b1.show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mRetrofitInstance = new Retrofit.Builder()
                .baseUrl("https://api.github.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    private void updateAdapterListing() {
        mWaiting = ProgressDialog.show(
                MainActivity.this,
                FETCHING_ISSUES,
                FETCHING_DESCRIPTION,
                false, false
        );
        mApiInstance.listIssues(mUser, mRepo).enqueue(new Callback<List<JsonObject>>() {
            @Override
            public void onResponse(Call<List<JsonObject>> call, Response<List<JsonObject>> response) {
                Log.d(TAG, "List Issues: onResponse: " + response.body());

                if (mAdapter == null) {
                    mAdapter = new MainAdapter(MainActivity.this, response.body(), MainActivity.this);
                    mRecyclerView.setAdapter(mAdapter);
                } else {
                    mAdapter.replaceDataset(response.body());
                }

                mWaiting.dismiss();
            }

            @Override
            public void onFailure(Call<List<JsonObject>> call, Throwable t) {
                mWaiting.dismiss();
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("ERROR!")
                        .setMessage("There was an error fetching data from the GitHub API. Try again later.")
                        .setNegativeButton("CLOSE", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                MainActivity.this.finish();
                            }
                        })
                        .show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        mRecyclerView = ((RecyclerView) findViewById(R.id.main_recycler_view));
        mRecyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));

        mApiInstance = mRetrofitInstance.create(GitHubAPI.class);

        updateAdapterListing();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
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
    public boolean onNavigationItemSelected(MenuItem item) {
        return false;
    }

    @Override
    public View.OnClickListener setListenerForClick(final JsonObject item) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String number = item.get("number").getAsString();

                final ProgressDialog progressDialog = ProgressDialog.show(
                        MainActivity.this,
                        FETCHING_COMMENTS,
                        FETCHING_DESCRIPTION,
                        false, false
                );

                mApiInstance.listIssueComments(mUser, mRepo, number).enqueue(new Callback<List<JsonObject>>() {
                    @Override
                    public void onResponse(Call<List<JsonObject>> call, Response<List<JsonObject>> response) {
                        Log.d(TAG, "List Issue Comments: onResponse: " + response.body());
                        for (JsonObject comment :
                                response.body()) {
                            if (comment.get("user").getAsJsonObject().get("login").getAsString().equals(mUser)) {
                                String answer = comment.get("body").getAsString();
                                AlertDialog.Builder b1 = new AlertDialog.Builder(MainActivity.this);
                                b1.setTitle(String.format(Locale.US, "Issue #%s on %s/%s", number, mUser, mRepo));

                                b1.setMessage(new Bypass(MainActivity.this).markdownToSpannable(answer));

                                b1.setNegativeButton("CLOSE", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        // USER CLOSED THIS DIALOG!
                                    }
                                });
                                progressDialog.dismiss();
                                b1.show();
                                break;
                            }
                        }

                        if (progressDialog.isShowing()) {
                            // THERE WAS NO ANSWER FOUND!
                            progressDialog.dismiss();
                            new AlertDialog.Builder(MainActivity.this)
                                    .setTitle(ANSWER_NOT_FOUND)
                                    .setMessage(ANSWER_NOT_FOUND_REASONS)
                                    .setNegativeButton("OKAY", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {

                                        }
                                    })
                                    .show();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<JsonObject>> call, Throwable t) {

                    }
                });

            }
        };
    }
}