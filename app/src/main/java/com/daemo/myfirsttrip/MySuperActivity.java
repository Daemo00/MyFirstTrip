package com.daemo.myfirsttrip;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.NavigationView;
import android.support.design.widget.NavigationView.OnNavigationItemSelectedListener;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentManager.BackStackEntry;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.widget.Toast;

import com.daemo.myfirsttrip.MySuperFragment.OnFragmentInteractionListener;
import com.daemo.myfirsttrip.R.id;
import com.daemo.myfirsttrip.R.layout;
import com.daemo.myfirsttrip.R.string;
import com.daemo.myfirsttrip.common.Constants;
import com.daemo.myfirsttrip.common.Utils;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class MySuperActivity extends AppCompatActivity
        implements OnNavigationItemSelectedListener, OnFragmentInteractionListener {

    FirebaseFirestore mFirestore;
    private DrawerLayout drawer;
    private ActionBarDrawerToggle toggle;
    private MyBroadcastReceiver tripsReceiver;

    private static void clearBackStack(MySuperActivity mySuperActivity) {
        FragmentManager manager = mySuperActivity.getSupportFragmentManager();
        if (manager.getBackStackEntryCount() > 0) {
            BackStackEntry entry = manager.getBackStackEntryAt(0);
            manager.popBackStack(entry.getId(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.activity_trips);
        // Enable Firestore logging
        FirebaseFirestore.setLoggingEnabled(true);

        // Firestore
        mFirestore = FirebaseFirestore.getInstance();
//        initDummyData();

        setupActionBar();

        setupDrawer();

        registerReceiver(tripsReceiver = new MyBroadcastReceiver(), new IntentFilter(Constants.ACTION_TRIP_SELECTED));
    }

//    private void initDummyData() {
//        for (Trip trip : Data.getTrips(null)) {
//            DocumentReference curTrip = mFirestore.collection("trips")
//                    .document(String.valueOf(trip.id));
//            for (Person person : Data.getPeople(trip))
//                trip.peopleIds.put(String.valueOf(person.id), 1);
//
//            curTrip.set(trip);
//        }
//        for (Person person: Data.getPeople(null)) {
//            DocumentReference curPerson = mFirestore.collection("people")
//                    .document(String.valueOf(person.id));
//            for (Trip trip : Data.getTrips(person))
//                person.tripsIds.put(String.valueOf(trip.id), 1);
//
//            curPerson.set(person);
//        }
//    }

    private void setupDrawer() {
        drawer = findViewById(id.drawer_layout);
        toggle = new ActionBarDrawerToggle(
                this, drawer, string.navigation_drawer_open, string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void setupActionBar() {
        setSupportActionBar(findViewById(id.toolbar));
        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setDisplayShowTitleEnabled(true);
            bar.setTitle(this.getTitle() == null ? Utils.getTag(this) : this.getTitle());
            bar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public void onBackPressed() {
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        boolean allowed = true;
        for (Fragment fragment : fragments) {
            if (fragment instanceof MySuperFragment) {
                MySuperFragment mySuperFragment = (MySuperFragment) fragment;
                allowed &= mySuperFragment.allowBackPress();
            }
        }
        if (!allowed) return;
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        } else {
            showOkCancelDialog("Sure?", "Are you sure you want to exit?", (dialog, id) -> {
                MySuperActivity.this.finish();
                MySuperActivity.super.onBackPressed();
            });
        }
    }

    public void showOkCancelDialog(final CharSequence title, final String message, final DialogInterface.OnClickListener okClickListener) {
        showOkCancelDialog(title, message, okClickListener, null);
    }

    private void showOkCancelDialog(final CharSequence title, final String message, final DialogInterface.OnClickListener okClickListener, final DialogInterface.OnClickListener cancelClickListener) {
        final Activity thisActivity = this;
        this.runOnUiThread(() -> {
            AlertDialog alertDialog = new AlertDialog.Builder(thisActivity)
                    .setMessage(message)
                    .setTitle(title)
                    .setPositiveButton("Ok", okClickListener)
                    .setNegativeButton("Cancel", cancelClickListener)
                    .setCancelable(false)
                    .create();
            alertDialog.show();
        });
    }

    public void showToast(final String message) {
        final Activity thisActivity = this;
        Log.v(Utils.getTag(this), "Toast shown: " + message);
        this.runOnUiThread(() -> {
            Toast toast = Toast.makeText(thisActivity, message, Toast.LENGTH_SHORT);
            toast.show();
        });
    }

    public void showUndoSnackbar(BaseTransientBottomBar.BaseCallback<Snackbar> callback) {
        final Activity thisActivity = this;
        this.runOnUiThread(() -> {
            Snackbar snackbar = Snackbar
                    .make(thisActivity.getWindow().getDecorView().findViewById(id.main_content), "Sure?", Snackbar.LENGTH_LONG)
                    .addCallback(callback)
                    .setAction(string.undo, view -> {
                        // Snackbar is automatically dismissed when its action is clicked
                    });
            snackbar.show();
        });
    }

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
        switch (item.getItemId()) {
            case id.nav_trips:
                clearBackStack(this);
                replaceFragment(
                        TripsListFragment.class.getName(),
                        new Bundle(),
                        false);
                break;
            case id.nav_people:
                replaceFragment(
                        PeopleListFragment.class.getName(),
                        new Bundle(),
                        false);
                break;
            case id.nav_import:
            case id.nav_tools:
                showOkCancelDialog("Sorry!", "Not yet implemented",
                        (dialogInterface, i) -> {
                        },
                        (dialogInterface, i) -> {
                        });
            case id.nav_share:
            case id.nav_send:
                showOkCancelDialog("Sorry!", "Not yet implemented",
                        (dialogInterface, i) -> {
                        });
                break;
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
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
            ft.addToBackStack(Utils.getTag(fragment));
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
