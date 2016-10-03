package com.example.nikhilgupta.moviesapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.GridView;

public class MainActivity extends AppCompatActivity implements MoviePostersFragment.OnMovieSelectedListener {

    String storedPref;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        storedPref = sharedPref.getString(getString(R.string.sort_order_key), getString(R.string.sort_order_value_mostpopular));
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String pref = sharedPref.getString(getString(R.string.sort_order_key), getString(R.string.sort_order_value_mostpopular));

        if(!storedPref.equals(pref)) {
            recreate();
            if (getResources().getConfiguration().orientation
                    == Configuration.ORIENTATION_LANDSCAPE) {
                ((GridView) (findViewById(R.id.gridView))).setNumColumns(1);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_settings:
                Intent i = new Intent(this, SettingsActivity.class);
                startActivity(i);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_LANDSCAPE) {
            ((GridView)(findViewById(R.id.gridView))).setNumColumns(1);
        }
    }

    MovieDetailsFragment details;
    @Override
    public void onMovieSelected(Movie[] movies, int i) {
        details = (MovieDetailsFragment) getFragmentManager().findFragmentById(R.id.details);
        // Check for landscape mode
        if (details!= null && details.isVisible())
        {
            details.setNewPage(movies[i]);
        }
        else
        {
            Intent intent = new Intent(this , MovieDetailsActivity.class);
            intent.putExtra("movie", movies[i]);
            startActivity(intent);
        }
    }
}
