package com.example.lubna.cloverweb;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.lubna.cloverweb.Database.DataSource.CartRepository;
import com.example.lubna.cloverweb.Database.Local.CartDataSource;
import com.example.lubna.cloverweb.Database.Local.CartDatabase;
import com.example.lubna.cloverweb.Utils.Common;
import com.google.firebase.auth.FirebaseAuth;
import com.nex3z.notificationbadge.NotificationBadge;

import io.reactivex.disposables.CompositeDisposable;

public class egrocery extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    ImageView im;
    private SharedPreferences sharedp;
    private Boolean UserLogin;
    EditText input;
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    public static NotificationBadge badge;
    ImageView cart_icon;

    boolean doubleBackToExitPressedOnce = false;

    private FirebaseAuth firebaseAuth;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_egrocery);

        firebaseAuth = FirebaseAuth.getInstance();

        sharedp = getSharedPreferences("Pre", Context.MODE_PRIVATE);
        UserLogin = sharedp.getBoolean("UserLogin", false);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        if (UserLogin.equals(true)) {
            //Init Database
            initDB();
            displaySelectedScreen(R.id.home);
        } else {
            displaySelectedScreen(R.id.loginfrag);
        }
    }

    private void initDB() {

        Common.cartDatabase = CartDatabase.getInstance(this);
        Common.cartRepository = CartRepository.getInstance(CartDataSource.getInstance(Common.cartDatabase.cartDAO()));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_action_bar, menu);
        View view = menu.findItem(R.id.cart_menu).getActionView();
        badge = (NotificationBadge) view.findViewById(R.id.badge);
        if (UserLogin.equals(true)) {
            badge.setText(String.valueOf(Common.cartRepository.countCartItems()));
        } else {
            badge.setText("0");
        }
        cart_icon = view.findViewById(R.id.cart_icon);
        cart_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Fragment fragment = new Cart();
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.content_egrocery, fragment);
                ft.commit();
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.cart_menu) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void displaySelectedScreen(int id) {
        if (UserLogin.equals(true)) {
            NavigationView navigationView = findViewById(R.id.nav_view);
            Menu menu = navigationView.getMenu();
            menu.findItem(R.id.loginfrag).setVisible(false);
        } else if (UserLogin.equals(false)) {
            NavigationView navigationView = findViewById(R.id.nav_view);
            Menu menu = navigationView.getMenu();
            menu.findItem(R.id.logout).setVisible(false);
        }
        Fragment fragment = null;
        switch (id) {
            case R.id.loginfrag:
                fragment = new loginfragment();
                break;
            case R.id.home:
                fragment = new Home();
                break;
            case R.id.cart:
                fragment = new Cart();
                break;
            case R.id.terms:
                fragment = new Fragment_Signup();
                break;
            case R.id.privacy:
                fragment = new FragmentPolicy();
                break;
            case R.id.contact:
                fragment = new FragmentContact();
                break;
            case R.id.locchan:
                pref = getSharedPreferences("MyPre", Context.MODE_PRIVATE);
                editor = pref.edit();
                editor.clear();
                editor.apply();
                startActivity(getIntent());
                break;
            case R.id.about:
                fragment = new FragmentAbout();
                break;
            //        case R.id.Membership:
            //            fragment = new Membership();
            //            break;
            case R.id.logout:
                firebaseAuth.signOut();
                pref = getSharedPreferences("Pre", Context.MODE_PRIVATE);
                editor = pref.edit();
                editor.remove("UserLogin");
                editor.apply();
                Toast.makeText(getApplicationContext(), "User Logged Out", Toast.LENGTH_LONG).show();
                startActivity(getIntent());
                break;
        }
        if (fragment != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content_egrocery, fragment);
            ft.addToBackStack(null);
            ft.commit();
        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        //        if(id == R.id.logout)
        //    {
        //        finish();
        //    }
        displaySelectedScreen(id);
        return true;
    }

    @Override
    public void onStop() {
        super.onStop();
        pref = getSharedPreferences("MyPre", Context.MODE_PRIVATE);
        editor = pref.edit();
        //editor.clear();
        editor.remove("location");
        editor.remove("StoreID");
        editor.apply();
        Common.cartRepository.emptyCart();
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }

    public void updateCartCount() {

        if (badge == null) return;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (Common.cartRepository.countCartItems() == 0)
                    badge.setVisibility(View.INVISIBLE);
                else {
                    badge.setVisibility(View.VISIBLE);
                    badge.setText(String.valueOf(Common.cartRepository.countCartItems()));
                }
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        //updateCartCount();
    }

}
