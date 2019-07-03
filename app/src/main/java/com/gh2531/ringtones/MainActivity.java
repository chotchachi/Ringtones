package com.gh2531.ringtones;

import android.app.Dialog;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Toast;

import com.gh2531.ringtones.adapter.FragmentAdapter;
import com.gh2531.ringtones.fragment.Tab_fragment;
import com.gh2531.ringtones.function.CheckExternalPermission;
import com.gh2531.ringtones.function.CheckWriteSettingPermission;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private FragmentAdapter fragmentAdapter;
    private CheckWriteSettingPermission checkWriteSettingPermission;
    private CheckExternalPermission checkExternalPermission;
    private FirebaseFirestore firebaseFirestore;
    private Set<String> list_danh_muc;
    private List<String> list_danh_muc_convert;
    private Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTheme(R.style.LightTheme);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        //permission();
        initDialog();

        fragmentAdapter = new FragmentAdapter(getSupportFragmentManager());
        viewPager = findViewById(R.id.pager);
        tabLayout = findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(viewPager);
        list_danh_muc = new HashSet<>();

        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection("ringtones")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                list_danh_muc.add(document.getId());
                            }
                            addTab();
                        } else {
                            Log.d("TAG", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    private void initDialog() {
        dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        Window window = dialog.getWindow();
        window.setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setContentView(R.layout.load_dialog);
        dialog.show();
    }

    private void permission() {
        checkWriteSettingPermission = new CheckWriteSettingPermission();
        checkWriteSettingPermission.checkWriteSettingPermisstion(this);
        checkExternalPermission = new CheckExternalPermission();
        checkExternalPermission.checkExternalPermission(this);
    }

    private void addTab() {
        list_danh_muc_convert = new ArrayList<>(list_danh_muc);

        BoNhoSharedPreferences sharedPreferences = new BoNhoSharedPreferences();
        sharedPreferences.GanMangSP("list_danh_muc", list_danh_muc, this);

        for (int i = 0; i < list_danh_muc.size(); i++){
            fragmentAdapter.addFragment(new Tab_fragment(), list_danh_muc_convert.get(i));
        }
        viewPager.setAdapter(fragmentAdapter);

        dialog.dismiss();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int id = menuItem.getItemId();

        if (id == R.id.nav_home) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_tools) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
