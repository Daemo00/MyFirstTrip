package com.daemo.myfirsttrip;

import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.NavigationView.OnNavigationItemSelectedListener;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentManager.BackStackEntry;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;

import com.daemo.myfirsttrip.MySuperFragment.OnFragmentInteractionListener;
import com.daemo.myfirsttrip.R.id;
import com.daemo.myfirsttrip.R.layout;
import com.daemo.myfirsttrip.R.string;
import com.daemo.myfirsttrip.common.Constants;
import com.daemo.myfirsttrip.common.Utils;

public class TripsActivity extends AppCompatActivity
        implements OnNavigationItemSelectedListener, OnFragmentInteractionListener {

    private DrawerLayout drawer;
    private ActionBarDrawerToggle toggle;
    private MyBroadcastReceiver tripsReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.activity_trips);
        setSupportActionBar(findViewById(id.toolbar));

        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setDisplayShowTitleEnabled(true);
            bar.setTitle(this.getTitle() == null ? Utils.getTag(this) : this.getTitle());
            bar.setDisplayHomeAsUpEnabled(true);
        }

        drawer = findViewById(id.drawer_layout);
        toggle = new ActionBarDrawerToggle(
                this, drawer, string.navigation_drawer_open, string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        registerReceiver(tripsReceiver = new MyBroadcastReceiver(), new IntentFilter(Constants.ACTION_TRIP_SELECTED));
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.trips, menu);
//        return true;
//    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case android.R.id.home:
                drawer.openDrawer(Gravity.START);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        clearBackStack();
        switch (item.getItemId()) {
            case id.nav_trips:
                replaceFragment(
                        TripsListFragment.class.getName(),
                        new Bundle(),
                        false);
                break;
            case id.nav_import:
            case id.nav_tools:
            case id.nav_share:
            case id.nav_send:
                break;
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void clearBackStack() {
        FragmentManager manager = getSupportFragmentManager();
        if (manager.getBackStackEntryCount() > 0) {
            BackStackEntry entry = manager.getBackStackEntryAt(0);
            manager.popBackStack(entry.getId(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
    }

    @Override
    public void onFragmentInteraction(Bundle bundle) {
        Log.d(Utils.getTag(this), "Received bundle " + Utils.debugBundle(bundle));
        String fragment_name = bundle.getString(Constants.EXTRA_REPLACE_FRAGMENT, "");
        if (!fragment_name.isEmpty()) {
            replaceFragment(
                    fragment_name,
                    bundle.getBundle(Constants.EXTRA_BUNDLE_FOR_FRAGMENT),
                    bundle.getBoolean(Constants.EXTRA_ADD_TO_BACKSTACK));
        }
    }

    private void replaceFragment(String fragment_name, Bundle bundle_for_fragment, boolean addToBackStack) {
        MySuperFragment fragment = (MySuperFragment) Fragment.instantiate(
                this,
                fragment_name,
                bundle_for_fragment
        );
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        if (addToBackStack)
            ft.addToBackStack("replace with " + Utils.getTag(fragment));
        else
            ft.disallowAddToBackStack();
        ft.replace(id.main_content, fragment, Utils.getTag(fragment));
        ft.commit();

        // update selected item title, then close the drawer
        setTitle(fragment.title);
        drawer.closeDrawers();
    }

    @Override
    protected void onDestroy() {
        drawer.removeDrawerListener(toggle);
        unregisterReceiver(tripsReceiver);
        super.onDestroy();
    }
}
