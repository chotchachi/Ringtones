package com.gh2531.ringtones;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Environment;
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
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.gh2531.ringtones.adapter.FragmentAdapter;
import com.gh2531.ringtones.fragment.Tab_fragment;
import com.gh2531.ringtones.function.CheckConnection;
import com.gh2531.ringtones.function.CheckExternalPermission;
import com.gh2531.ringtones.function.CheckWriteSettingPermission;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
    private Dialog dialog_loading, dialog_no_connection;
    private CheckConnection checkConnection;

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
        navigationView.setItemIconTintList(null);

        //permission();
        checkConnection = new CheckConnection();
        if (checkConnection.CheckConnection(this) == true){
            initDialog();
            getData();
        } else {
            noConnectionDialog(this);
        }

        fragmentAdapter = new FragmentAdapter(getSupportFragmentManager());
        viewPager = findViewById(R.id.pager);
        tabLayout = findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(viewPager);
        viewPager.setOffscreenPageLimit(5);
        list_danh_muc = new HashSet<>();

        //Divider
        LinearLayout linearLayout = (LinearLayout)tabLayout.getChildAt(0);
        linearLayout.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(Color.GRAY);
        drawable.setSize(1, 1);
        linearLayout.setDividerPadding(20);
        linearLayout.setDividerDrawable(drawable);

    }

    private void getData(){
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

    private void addTab() {
        list_danh_muc_convert = new ArrayList<>(list_danh_muc);

        BoNhoSharedPreferences sharedPreferences = new BoNhoSharedPreferences();
        sharedPreferences.GanMangSP("list_danh_muc", list_danh_muc, this);

        for (int i = 0; i < list_danh_muc.size(); i++){
            fragmentAdapter.addFragment(new Tab_fragment(), list_danh_muc_convert.get(i));
        }
        viewPager.setAdapter(fragmentAdapter);

        dialog_loading.dismiss();
    }

    private void initDialog() {
        dialog_loading = new Dialog(this);
        dialog_loading.requestWindowFeature(Window.FEATURE_NO_TITLE);
        Window window = dialog_loading.getWindow();
        window.setBackgroundDrawableResource(android.R.color.transparent);
        dialog_loading.setContentView(R.layout.load_dialog);
        dialog_loading.setCancelable(false);
        dialog_loading.show();
    }

    private void noConnectionDialog(final Activity activity){
        dialog_no_connection = new Dialog(this);
        dialog_no_connection.requestWindowFeature(Window.FEATURE_NO_TITLE);
        Window window = dialog_no_connection.getWindow();
        window.setBackgroundDrawableResource(android.R.color.transparent);
        dialog_no_connection.setContentView(R.layout.noconnection_dialog);
        dialog_no_connection.setCancelable(false);
        Button btn_ok = dialog_no_connection.findViewById(R.id.btn_ok);
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog_no_connection.dismiss();
                if (checkConnection.CheckConnection(activity) == true){
                    initDialog();
                    getData();
                } else {
                    noConnectionDialog(activity);
                }
            }
        });
        dialog_no_connection.show();
    }

    private void permission() {
        checkWriteSettingPermission = new CheckWriteSettingPermission();
        checkWriteSettingPermission.checkWriteSettingPermisstion(this);
        checkExternalPermission = new CheckExternalPermission();
        checkExternalPermission.checkExternalPermission(this);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int id = menuItem.getItemId();

        if (id == R.id.navigation_item_1) {

        } else if (id == R.id.navigation_item_2) {
            submit();
        } else if (id == R.id.navigation_item_3) {
            openDownloadFolder();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void openDownloadFolder() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri uri = Uri.parse(Environment.getExternalStorageDirectory() + "/ringtone_app"); // a directory
        intent.setDataAndType(uri, "*/*");
        startActivity(Intent.createChooser(intent, "Open folder"));
    }

    public void submit(){
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        Window window = dialog.getWindow();
        window.setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setContentView(R.layout.submit_dialog);
        final EditText edt_u_name, edt_u_email, edt_r_name, edt_r_singer;
        edt_u_name = dialog.findViewById(R.id.edt_u_name);
        edt_u_email = dialog.findViewById(R.id.edt_u_email);
        edt_r_name = dialog.findViewById(R.id.edt_r_name);
        edt_r_singer = dialog.findViewById(R.id.edt_r_singer);

        edt_u_name.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b){
                    edt_u_name.setHint("");
                } else {
                    edt_u_name.setHint("Your Name");
                }
            }
        });

        edt_u_email.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b){
                    edt_u_email.setHint("");
                } else {
                    edt_u_email.setHint("Your Email");
                }
            }
        });

        edt_r_name.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b){
                    edt_r_name.setHint("");
                } else {
                    edt_r_name.setHint("Ringtone Name");
                }
            }
        });

        edt_r_singer.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b){
                    edt_r_singer.setHint("");
                } else {
                    edt_r_singer.setHint("Ringtone Singer");
                }
            }
        });
        Button btn_submit = dialog.findViewById(R.id.btn_submit);
        final TextView tv_icr_email, tv_icr_ringtone, tv_icr;
        tv_icr_email = dialog.findViewById(R.id.tv_icr_email);
        tv_icr_ringtone = dialog.findViewById(R.id.tv_icr_ringtone);
        tv_icr = dialog.findViewById(R.id.tv_icr);
        btn_submit.setOnClickListener(new View.OnClickListener()  {
            @Override
            public void onClick(View view) {
                String u_name = edt_u_name.getText().toString();
                String u_email = edt_u_email.getText().toString();
                String r_name = edt_r_name.getText().toString();
                String r_singer = edt_r_singer.getText().toString();
                String regex = "^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$";
                if (u_email.equals("") && r_name.equals("")){
                    tv_icr.setText("* You must not leave Ringtone Name and Your Email blank");
                    tv_icr.setVisibility(View.VISIBLE);
                    tv_icr_email.setVisibility(View.GONE);
                    tv_icr_ringtone.setVisibility(View.GONE);
                } else if(u_email.equals("")){
                    tv_icr_email.setText("* You must not leave Your Email blank");
                    tv_icr_email.setVisibility(View.VISIBLE);
                    tv_icr.setVisibility(View.GONE);
                    tv_icr_ringtone.setVisibility(View.GONE);
                } else if (r_name.equals("")) {
                    tv_icr_ringtone.setText("* You must not leave Ringtone Name blank");
                    tv_icr_ringtone.setVisibility(View.VISIBLE);
                    tv_icr.setVisibility(View.GONE);
                    tv_icr_email.setVisibility(View.GONE);
                } else if (u_email.matches(regex) == false){
                    tv_icr_email.setText("* Incorrect email format");
                    tv_icr_email.setVisibility(View.VISIBLE);
                    tv_icr.setVisibility(View.GONE);
                    tv_icr_ringtone.setVisibility(View.GONE);
                } else {
                    Map<String, String> map = new HashMap<>();
                    map.put("r_name", r_name);
                    map.put("r_singer", r_singer);
                    map.put("u_email", u_email);
                    map.put("u_name", u_name);
                    firebaseFirestore.collection("req_ringtones").document().set(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(MainActivity.this, "Submit Successfully", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        }
                    });
                }
            }
        });
        dialog.show();
    }
}
