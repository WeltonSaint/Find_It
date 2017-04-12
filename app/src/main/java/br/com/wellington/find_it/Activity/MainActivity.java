package br.com.wellington.find_it.Activity;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcel;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;
import com.squareup.picasso.Picasso;

import java.util.Calendar;

import br.com.wellington.find_it.Bean.Cliente;
import br.com.wellington.find_it.Fragment.ConversationsFragment;
import br.com.wellington.find_it.Fragment.ItemList;
import br.com.wellington.find_it.R;
import br.com.wellington.find_it.Service.MatchNotificationService;
import br.com.wellington.find_it.Service.MessagesNotificationService;
import br.com.wellington.find_it.Utils.ConnectionManagement;
import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static int navItemIndex = 0;
    // toolbar titles respected to selected nav menu item
    private String[] activityTitles;
    // tags used to attach the fragments
    private static final String TAG_LOST_AND_FOUND_ITEM = "lost_and_found_item";
    private static final String TAG_LOST_ITEM = "lost_item";
    private static final String TAG_FOUND_ITEM = "found_item";
    private static final String TAG_RETURNED_ITEM = "returned_item";
    private static final String TAG_CONVERSATIONS = "conversations";
    public static String CURRENT_TAG = TAG_LOST_AND_FOUND_ITEM;
    private static Cliente user;
    private static boolean startedService;

    @SuppressLint("StaticFieldLeak")
    private static NavigationView mNavigationView;
    private DrawerLayout mDrawer;
    private CircleImageView mNavImage;
    private TextView mNavEmail;
    private TextView mNavName;
    @SuppressLint("StaticFieldLeak")
    private static FloatingActionButton mFabInsetItem;
    private Handler mHandler;

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mHandler = new Handler();
        startedService = false;

        mFabInsetItem = (FloatingActionButton) findViewById(R.id.fab);
        mFabInsetItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                bundle.putSerializable("user", user);
                intent.putExtras(bundle);
                intent.setClass(MainActivity.this, InsertItemActivity.class);
                startActivity(intent);
            }
        });

        // load toolbar titles from string resources
        activityTitles = getResources().getStringArray(R.array.nav_item_activity_titles);

        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawer.addDrawerListener(toggle);
        toggle.syncState();

        mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(this);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {

            user = (Cliente) extras.getSerializable("user");
            navItemIndex = extras.getInt("navItemIndex");
            if(navItemIndex == 0)
                loadHomeFragment();
            else
                getHomeFragment();

            //Start services
            if (!startedService)
                startServices(user);

        }

        if (savedInstanceState == null) {
            navItemIndex = 0;
            CURRENT_TAG = TAG_LOST_AND_FOUND_ITEM;
            loadHomeFragment();
        } else {
            startedService = savedInstanceState.getBoolean("startedService");
            navItemIndex = savedInstanceState.getInt("navItemIndex");
            user = (Cliente) savedInstanceState.getSerializable("user");
            getSupportActionBar().setTitle(activityTitles[navItemIndex]);
            getHomeFragment();
        }

    }

    public static void hasNotReadNotification(boolean value) {
        if (value)
            mNavigationView.getMenu().getItem(4).setActionView(R.layout.menu_alert);
        else
            mNavigationView.getMenu().getItem(4).setActionView(null);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putBoolean("startedService", startedService);
        savedInstanceState.putInt("navItemIndex", navItemIndex);
        savedInstanceState.putSerializable("user", user);
    }


    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @SuppressLint("ShortAlarm")
    private void startServices(Cliente user) {
        if (isServiceRunning(MatchNotificationService.class)) {
            stopService(new Intent(MainActivity.this, MatchNotificationService.class));
        }
        if (isServiceRunning(MessagesNotificationService.class)) {
            stopService(new Intent(MainActivity.this, MessagesNotificationService.class));
        }

        Calendar cal = Calendar.getInstance();
        AlarmManager amMatchNot = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        AlarmManager amMessageNot = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);


        Parcel parcel = Parcel.obtain();
        user.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);

        Intent serviceMatchNot = new Intent(getApplicationContext(), MatchNotificationService.class);
        serviceMatchNot.putExtra("user", parcel.marshall());
        PendingIntent servicePendingIntentMatchNot =
                PendingIntent.getService(getApplicationContext(),
                        MatchNotificationService.SERVICE_ID,
                        serviceMatchNot,
                        PendingIntent.FLAG_CANCEL_CURRENT);
        amMatchNot.setRepeating(
                AlarmManager.RTC_WAKEUP,
                cal.getTimeInMillis(),
                MatchNotificationService.TIME_BETWEEN_REQUESTS,
                servicePendingIntentMatchNot
        );

        Intent serviceMessageNot = new Intent(getApplicationContext(), MessagesNotificationService.class);
        serviceMessageNot.putExtra("user", parcel.marshall());
        PendingIntent servicePendingIntentMessageNot =
                PendingIntent.getService(getApplicationContext(),
                        MessagesNotificationService.SERVICE_ID,
                        serviceMessageNot,
                        PendingIntent.FLAG_CANCEL_CURRENT);
        amMessageNot.setRepeating(
                AlarmManager.RTC_WAKEUP,
                cal.getTimeInMillis(),
                MessagesNotificationService.TIME_BETWEEN_REQUESTS,
                servicePendingIntentMessageNot
        );

        startedService = true;

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

        mNavImage = (CircleImageView) findViewById(R.id.nav_image);
        mNavEmail = (TextView) findViewById(R.id.nav_email);
        mNavName = (TextView) findViewById(R.id.nav_name);

        if (mNavImage != null && user.getLinkFotoPerfilCliente() != null && !user.getLinkFotoPerfilCliente().equals(""))
            Picasso.with(getApplicationContext()).load(user.getLinkFotoPerfilCliente()).noFade().placeholder(R.drawable.profile).into(mNavImage);

        if (mNavName != null)
            mNavName.setText(user.getNomeCliente());

        if (mNavEmail != null)
            mNavEmail.setText(user.getEmailCliente());

        LinearLayout mNavProfile = (LinearLayout) findViewById(R.id.nav_profile);
        if (mNavProfile != null) {
            mNavProfile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent();
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("user", user);
                    intent.putExtras(bundle);
                    intent.setClass(MainActivity.this, ProfileActivity.class);
                    startActivityForResult(intent, 1);
                }
            });
        }

        getMenuInflater().inflate(R.menu.main_menu, menu);

        return true;
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (data != null) {
                user = (Cliente) data.getExtras().getSerializable("user");
                if ((user != null ? user.getLinkFotoPerfilCliente() : null) != null && !user.getLinkFotoPerfilCliente().equals(""))
                    Picasso.with(getApplicationContext()).load(user.getLinkFotoPerfilCliente()).noFade().placeholder(R.drawable.profile).into(mNavImage);

                mNavName.setText(user != null ? user.getNomeCliente() : null);
                mNavEmail.setText(user.getEmailCliente());
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_logoff) {
            logoff();
            disconnectFromFacebook();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void disconnectFromFacebook() {

        if (AccessToken.getCurrentAccessToken() == null) {
            return; // already logged out
        }

        new GraphRequest(AccessToken.getCurrentAccessToken(), "/me/permissions/", null, HttpMethod.DELETE, new GraphRequest
                .Callback() {
            @Override
            public void onCompleted(GraphResponse graphResponse) {

                LoginManager.getInstance().logOut();

            }
        }).executeAsync();
    }

    private void logoff() {
        ConnectionManagement.savePreferences(ConnectionManagement.getLoginSave(getApplicationContext()), "", getApplicationContext());
        stopService(new Intent(MainActivity.this, MatchNotificationService.class));
        stopService(new Intent(MainActivity.this, MessagesNotificationService.class));
        this.finish();
        Intent intent = new Intent();
        intent.setClass(this, LoginActivity.class);
        startActivity(intent);
    }

    /***
     * Returns respected fragment that user
     * selected from navigation menu
     */
    private void loadHomeFragment() {
        // selecting appropriate nav menu item
        selectNavMenu();

        // set toolbar title
        setToolbarTitle();

        // if user select the current navigation menu again, don't do anything
        // just close the navigation mDrawer
        if (getSupportFragmentManager().findFragmentByTag(CURRENT_TAG) != null) {
            mDrawer.closeDrawers();
            return;
        }

        // Sometimes, when fragment has huge data, screen seems hanging
        // when switching between navigation menus
        // So using runnable, the fragment is loaded with cross fade effect
        // This effect can be seen in GMail app
        Runnable mPendingRunnable = new Runnable() {
            @Override
            public void run() {
                // update the main content by replacing fragments
                Fragment fragment = getHomeFragment();
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.setCustomAnimations(R.anim.fade,
                        R.anim.fadeout);
                fragmentTransaction.replace(R.id.frame, fragment, CURRENT_TAG).commit();
            }
        };

        // If mPendingRunnable is not null, then add to the message queue
        mHandler.post(mPendingRunnable);

        //Closing mDrawer on item click
        mDrawer.closeDrawers();

        // refresh toolbar menu
        invalidateOptionsMenu();
    }

    @SuppressWarnings("ConstantConditions")
    private void setToolbarTitle() {
        getSupportActionBar().setTitle(activityTitles[navItemIndex]);
    }

    private void selectNavMenu() {
        mNavigationView.getMenu().getItem(navItemIndex).setChecked(true);
    }

    private Fragment getHomeFragment() {
        if (navItemIndex < 4) {
            setFabVisibility(true);
            return ItemList.newInstance(navItemIndex, (int) user.getCodigoCliente());
        } else {
            setFabVisibility(false);
            return ConversationsFragment.newInstance(navItemIndex, user);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.lost_and_found_item) {
            navItemIndex = 0;
            CURRENT_TAG = TAG_LOST_AND_FOUND_ITEM;
        } else if (id == R.id.lost_item) {
            navItemIndex = 1;
            CURRENT_TAG = TAG_LOST_ITEM;
        } else if (id == R.id.found_item) {
            navItemIndex = 2;
            CURRENT_TAG = TAG_FOUND_ITEM;
        } else if (id == R.id.returned_item) {
            navItemIndex = 3;
            CURRENT_TAG = TAG_RETURNED_ITEM;
        } else if (id == R.id.conversations) {
            navItemIndex = 4;
            CURRENT_TAG = TAG_CONVERSATIONS;
        } else if (id == R.id.nav_about_us) {
            mDrawer.closeDrawers();
        } else if (id == R.id.faq) {
            mDrawer.closeDrawers();
        }

        //Checking if the item is in checked state or not, if not make it in checked state
        if (item.isChecked()) {
            item.setChecked(false);
        } else {
            item.setChecked(true);
        }
        item.setChecked(true);

        loadHomeFragment();

        return true;
    }

    public static void setFabVisibility(boolean visibility) {
        if (visibility) {
            mFabInsetItem.show();
        } else {
            mFabInsetItem.hide();
        }
    }


}
