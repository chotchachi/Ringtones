package com.gh2531.ringtones.function;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import com.gh2531.ringtones.R;
import com.gh2531.ringtones.model.Ringtone;

import java.io.File;
import java.util.ArrayList;

public class DownloadAndSetRingtone {
    CheckWriteSettingPermission checkWriteSettingPermission = new CheckWriteSettingPermission();
    CheckExternalPermission checkExternalPermission = new CheckExternalPermission();

    public void downloadFile(ArrayList<Ringtone> arrayList, int position, final int type, final Activity activity){
        if (checkStoragePermission(activity) == true){
            //Tạo đường dẫn
            File path = new File(Environment.getExternalStorageDirectory() + "/ringtone_app");

            //Kiểm tra đã có Folder hay chưa
            if (!path.exists()) {
                //Tạo Folder
                path.mkdirs();
            }

            //Tạo file
            final String fileName = arrayList.get(position).getFile_name()+".mp3";
            final File ringtone = new File(path, fileName);

            //Kiểm tra bài hát tồn tại hay chưa, nếu chưa thì Download về
            if (!ringtone.exists()) {
                Uri uri = Uri.parse(arrayList.get(position).getFile_url());
                DownloadManager downloadManager = (DownloadManager) activity.getSystemService(Context.DOWNLOAD_SERVICE);
                DownloadManager.Request request = new DownloadManager.Request(uri);
                request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
                request.setTitle("Download "+arrayList.get(position).getFile_name()+".mp3");
                request.allowScanningByMediaScanner();
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                request.setDestinationInExternalPublicDir("/ringtone_app",arrayList.get(position).getFile_name()+".mp3");
                downloadManager.enqueue(request);
            } else {
                setRingTone(ringtone, fileName, type, activity);
            }

            BroadcastReceiver onComplete = new BroadcastReceiver() {
                public void onReceive(Context ctxt, Intent intent) {
                    setRingTone(ringtone, fileName, type, activity);
                }
            };
            activity.registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        }
        else {
            checkExternalPermission.checkExternalPermission(activity);
        }
    }

    private void setRingTone(File file, String fileName, int type, Activity activity) {
        switch (type){
            case 1:
                try {
                    ContentValues values = new ContentValues();
                    values.put(MediaStore.MediaColumns.DATA, file.getAbsolutePath());
                    values.put(MediaStore.MediaColumns.TITLE, fileName);
                    values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/mp3");
                    values.put("_size", file.length());
                    values.put(MediaStore.Audio.Media.IS_RINGTONE, true);
                    values.put(MediaStore.Audio.Media.IS_NOTIFICATION, true);
                    values.put(MediaStore.Audio.Media.IS_ALARM, true);
                    Uri uri = MediaStore.Audio.Media.getContentUriForPath(file.getAbsolutePath());
                    activity.getContentResolver().delete(uri, MediaStore.MediaColumns.DATA + "=\"" + file.getAbsolutePath() + "\"", null);
                    Uri newUri = activity.getContentResolver().insert(uri, values);

                    try {
                        RingtoneManager.setActualDefaultRingtoneUri(activity, RingtoneManager.TYPE_RINGTONE, newUri);
                        Toast.makeText(activity, activity.getString(R.string.cuocgoithanhcong), Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        checkWriteSettingPermission.checkWriteSettingPermisstion(activity);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case 2:
                try {
                    ContentValues values = new ContentValues();
                    values.put(MediaStore.MediaColumns.DATA, file.getAbsolutePath());
                    values.put(MediaStore.MediaColumns.TITLE, fileName);
                    values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/mp3");
                    values.put("_size", file.length());
                    values.put(MediaStore.Audio.Media.IS_NOTIFICATION, true);
                    values.put(MediaStore.Audio.Media.IS_RINGTONE, true);
                    values.put(MediaStore.Audio.Media.IS_ALARM, true);
                    Uri uri = MediaStore.Audio.Media.getContentUriForPath(file.getAbsolutePath());
                    activity.getContentResolver().delete(uri, MediaStore.MediaColumns.DATA + "=\"" + file.getAbsolutePath() + "\"", null);
                    Uri newUri = activity.getContentResolver().insert(uri, values);

                    try {
                        RingtoneManager.setActualDefaultRingtoneUri(activity, RingtoneManager.TYPE_NOTIFICATION, newUri);
                        Toast.makeText(activity, activity.getString(R.string.thongbaothanhcong), Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        checkWriteSettingPermission.checkWriteSettingPermisstion(activity);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case 3:
                try {
                    ContentValues values = new ContentValues();
                    values.put(MediaStore.MediaColumns.DATA, file.getAbsolutePath());
                    values.put(MediaStore.MediaColumns.TITLE, fileName);
                    values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/mp3");
                    values.put("_size", file.length());
                    values.put(MediaStore.Audio.Media.IS_NOTIFICATION, true);
                    values.put(MediaStore.Audio.Media.IS_RINGTONE, true);
                    values.put(MediaStore.Audio.Media.IS_ALARM, true);
                    Uri uri = MediaStore.Audio.Media.getContentUriForPath(file.getAbsolutePath());
                    activity.getContentResolver().delete(uri, MediaStore.MediaColumns.DATA + "=\"" + file.getAbsolutePath() + "\"", null);
                    Uri newUri = activity.getContentResolver().insert(uri, values);

                    try {
                        RingtoneManager.setActualDefaultRingtoneUri(activity, RingtoneManager.TYPE_ALARM, newUri);
                        Toast.makeText(activity, activity.getString(R.string.baothucthanhcong), Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        checkWriteSettingPermission.checkWriteSettingPermisstion(activity);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    private boolean checkStoragePermission(Activity activity) {
        String permission = android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
        int res = activity.checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }
}
