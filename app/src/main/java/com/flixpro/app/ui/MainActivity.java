package com.flixpro.app.ui;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.flixpro.app.R;
import com.flixpro.app.ui.catalog.CatalogFragment;
import com.flixpro.app.ui.home.HomeFragment;
import com.flixpro.app.ui.search.SearchFragment;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    private static final String STATE_TAB = "current_tab";
    private static final String HOME = "home";
    private static final String MOVIES = "movies";
    private static final String SERIES = "series";
    private static final String SEARCH = "search";

    private MaterialToolbar toolbar;
    private BottomNavigationView nav;
    private String currentTab = HOME;

    @Override protected void onCreate(Bundle savedInstanceState) {
        SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = findViewById(R.id.toolbar);
        nav = findViewById(R.id.bottomNav);

        if (savedInstanceState != null) currentTab = savedInstanceState.getString(STATE_TAB, HOME);

        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_movies) switchTo(MOVIES);
            else if (id == R.id.nav_series) switchTo(SERIES);
            else if (id == R.id.nav_search) switchTo(SEARCH);
            else switchTo(HOME);
            return true;
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override public void handleOnBackPressed() {
                if (!HOME.equals(currentTab)) nav.setSelectedItemId(R.id.nav_home);
                else finish();
            }
        });

        nav.setSelectedItemId(itemForTab(currentTab));
    }

    private void switchTo(String tag) {
        currentTab = tag;
        toolbar.setTitle(titleForTab(tag));
        FragmentManager fm = getSupportFragmentManager();
        Fragment target = fm.findFragmentByTag(tag);
        if (target == null) target = createFragment(tag);

        androidx.fragment.app.FragmentTransaction tx = fm.beginTransaction();
        for (Fragment f : fm.getFragments()) {
            if (f != target && f.isAdded()) tx.hide(f);
        }
        if (target.isAdded()) tx.show(target);
        else tx.add(R.id.fragmentContainer, target, tag);
        tx.commit();
    }

    private Fragment createFragment(String tag) {
        if (MOVIES.equals(tag)) return CatalogFragment.newInstance("movie");
        if (SERIES.equals(tag)) return CatalogFragment.newInstance("show");
        if (SEARCH.equals(tag)) return new SearchFragment();
        return new HomeFragment();
    }

    private int itemForTab(String tag) {
        if (MOVIES.equals(tag)) return R.id.nav_movies;
        if (SERIES.equals(tag)) return R.id.nav_series;
        if (SEARCH.equals(tag)) return R.id.nav_search;
        return R.id.nav_home;
    }

    private int titleForTab(String tag) {
        if (MOVIES.equals(tag)) return R.string.movies;
        if (SERIES.equals(tag)) return R.string.series;
        if (SEARCH.equals(tag)) return R.string.search;
        return R.string.app_name;
    }

    @Override protected void onSaveInstanceState(Bundle outState) {
        outState.putString(STATE_TAB, currentTab);
        super.onSaveInstanceState(outState);
    }
}
