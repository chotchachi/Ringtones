package com.gh2531.ringtones.fragment;

import android.app.Dialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.gh2531.ringtones.BoNhoSharedPreferences;
import com.gh2531.ringtones.R;
import com.gh2531.ringtones.adapter.RingtoneAdapter;
import com.gh2531.ringtones.function.CheckExternalPermission;
import com.gh2531.ringtones.function.CheckWriteSettingPermission;
import com.gh2531.ringtones.function.DownloadAndSetRingtone;
import com.gh2531.ringtones.function.GetListRingtone;
import com.gh2531.ringtones.model.Ringtone;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Tab_fragment extends Fragment {
    private View view;

    private static final String PATH_COLLECTION_RINGTONE_FIREBASE = "ringtones";
    private static final String PATH_DATA_RINGTONE_FIREBASE = "data";

    private BoNhoSharedPreferences sharedPreferences;
    private Set<String> list_danh_muc;
    private List<String> list_danh_muc_convert;
    private String ten_danh_muc;

    private RecyclerView recyclerView;
    private ArrayList<Ringtone> arrayListRingtone;
    private RingtoneAdapter ringtoneAdapter;
    private DownloadAndSetRingtone downloadAndSetRingtone;
    private GetListRingtone getListRingtone;
    private CheckWriteSettingPermission checkWriteSettingPermission;
    private CheckExternalPermission checkExternalPermission;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_tab, container, false);

        checkWriteSettingPermission = new CheckWriteSettingPermission();
        checkExternalPermission = new CheckExternalPermission();

        sharedPreferences = new BoNhoSharedPreferences();
        list_danh_muc = new HashSet<>();
        list_danh_muc = sharedPreferences.TraMangSP("list_danh_muc", getActivity());
        list_danh_muc_convert = new ArrayList<>(list_danh_muc);
        int danh_muc_position = getArguments().getInt("position");
        ten_danh_muc = list_danh_muc_convert.get(danh_muc_position);

        initRecyclerView(view);

        arrayListRingtone = new ArrayList<>();
        ringtoneAdapter = new RingtoneAdapter(arrayListRingtone, getActivity());
        recyclerView.setAdapter(ringtoneAdapter);

        downloadAndSetRingtone = new DownloadAndSetRingtone();
        getListRingtone = new GetListRingtone();
        getListRingtone.GetListRingtone(
                PATH_COLLECTION_RINGTONE_FIREBASE,
                ten_danh_muc,
                PATH_DATA_RINGTONE_FIREBASE,
                arrayListRingtone,
                ringtoneAdapter);

        clickItemMore();

        return view;
    }

    public static Tab_fragment newInstance() {
        return new Tab_fragment();
    }

    private void clickItemMore() {
        ringtoneAdapter.setOnItemClickListener(new RingtoneAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, int position) {
                dialogMore(position);
            }
        });
    }

    private void dialogMore(final int position) {
        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        Window window = dialog.getWindow();
        window.setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setContentView(R.layout.more_dialog);
        dialog.show();
        RelativeLayout set_phone, set_notify, set_alarm, download;
        set_phone = dialog.findViewById(R.id.set_phone_ringtone);
        set_notify = dialog.findViewById(R.id.set_noti_ringtone);
        set_alarm = dialog.findViewById(R.id.set_alarm_ringtone);
        download = dialog.findViewById(R.id.download_ringtone);

        set_phone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                downloadAndSetRingtone.downloadFile(arrayListRingtone, position, 1, getActivity());
                dialog.dismiss();
            }
        });

        set_notify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                downloadAndSetRingtone.downloadFile(arrayListRingtone, position, 2, getActivity());
                dialog.dismiss();
            }
        });

        set_alarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                downloadAndSetRingtone.downloadFile(arrayListRingtone, position, 3, getActivity());
                dialog.dismiss();
            }
        });

        download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    if (checkStoragePermission() == true){
                        //Tạo đường dẫn
                        File path = new File(Environment.getExternalStorageDirectory() + "/ringtone_app");

                        //Kiểm tra đã có Folder hay chưa
                        if (!path.exists()) {
                            //Tạo Folder
                            path.mkdirs();
                        }

                        //Tạo file
                        final String fileName = arrayListRingtone.get(position).getFile_name()+".mp3";
                        final File ringtone = new File(path, fileName);

                        //Kiểm tra bài hát tồn tại hay chưa, nếu chưa thì Download về
                        if (!ringtone.exists()) {
                            Uri uri = Uri.parse(arrayListRingtone.get(position).getFile_url());
                            DownloadManager downloadManager = (DownloadManager) getActivity().getSystemService(Context.DOWNLOAD_SERVICE);
                            DownloadManager.Request request = new DownloadManager.Request(uri);
                            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
                            request.setTitle("Download "+arrayListRingtone.get(position).getFile_name()+".mp3");
                            request.allowScanningByMediaScanner();
                            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                            request.setDestinationInExternalPublicDir("/ringtone_app",arrayListRingtone.get(position).getFile_name()+".mp3");
                            downloadManager.enqueue(request);
                        } else {
                            Toast.makeText(getActivity(), getActivity().getString(R.string.dadownload), Toast.LENGTH_SHORT).show();
                        }
                        BroadcastReceiver onComplete = new BroadcastReceiver() {
                            public void onReceive(Context ctxt, Intent intent) {
                                Toast.makeText(getActivity(), getActivity().getString(R.string.downloadthanhcong), Toast.LENGTH_SHORT).show();
                            }
                        };
                        getActivity().registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
                    }
                    else {
                        checkExternalPermission.checkExternalPermission(getActivity());
                    }
            }
        });
    }

    private boolean checkStoragePermission() {
        String permission = android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
        int res = getActivity().checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }

    private void initRecyclerView(View v) {
        recyclerView = v.findViewById(R.id.recyclerview);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
    }

    @Override
    public void onPause() {
        super.onPause();
        ringtoneAdapter.stopPlayer();
    }

    @Override
    public void onResume() {
        super.onResume();
        ringtoneAdapter.stopPlayer();
    }
}