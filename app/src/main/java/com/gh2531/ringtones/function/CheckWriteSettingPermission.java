package com.gh2531.ringtones.function;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;

import com.gh2531.ringtones.R;

public class CheckWriteSettingPermission {

    @TargetApi(Build.VERSION_CODES.M)
    public boolean checkWriteSettingPermisstion(final Activity activity) {
        boolean settingsCanWrite = Settings.System.canWrite(activity);
        if(!settingsCanWrite) {
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setMessage(activity.getString(R.string.thongbao1));
            builder.setNegativeButton(activity.getString(R.string.mocaidat), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                    intent.setData(Uri.parse("package:" + activity.getPackageName()));
                    activity.startActivity(intent);
                    dialog.dismiss();
                }
            });
            builder.setPositiveButton(activity.getString(R.string.huy), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            Dialog dialog = builder.create();
            dialog.show();
            return false;
        }else {
            return true;
        }
    }
}
