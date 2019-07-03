package com.gh2531.ringtones.adapter;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.gh2531.ringtones.R;
import com.gh2531.ringtones.function.CheckExternalPermission;
import com.gh2531.ringtones.model.Ringtone;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.netopen.hotbitmapgg.library.view.RingProgressBar;

public class RingtoneAdapter extends RecyclerView.Adapter<RingtoneAdapter.ViewHolder>{
    private ArrayList<Ringtone> arrayList;
    private Activity activity;
    private MediaPlayer player;
    private int playingPosition;
    private ViewHolder playingHolder;
    private OnItemClickListener listener;

    public RingtoneAdapter(ArrayList<Ringtone> arrayList, Activity activity) {
        this.arrayList = arrayList;
        this.activity = activity;
        this.playingPosition = -1;
    }

    @NonNull
    @Override
    public RingtoneAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View view = inflater.inflate(R.layout.item_list_ringtone, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, final int i) {
        if (i == playingPosition) {
            playingHolder = viewHolder;
            updatePlayingView();
        } else {
            updateNoPlayingView(viewHolder);
        }

        viewHolder.tv_ringtonename.setText(arrayList.get(i).getFile_name());
        viewHolder.btn_more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onItemClick(view, i);
            }
        });

        viewHolder.btn_play_pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    new AsyncTask<Integer, Integer, Integer>() {
                        @Override
                        protected void onPreExecute() {
                            super.onPreExecute();
                            viewHolder.loading_progress.setVisibility(View.INVISIBLE);
                            viewHolder.progress_bar.setVisibility(View.VISIBLE);
                            viewHolder.btn_play_pause.setBackgroundResource(R.drawable.ic_pause);
                        }

                        @Override
                        protected Integer doInBackground(Integer... integers) {
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Integer integer) {
                            super.onPostExecute(integer);
                            if (playingPosition == i) {
                                if (player.isPlaying()) {
                                    player.pause();
                                } else {
                                    player.start();
                                }
                            } else {
                                playingPosition = i;
                                if (player != null) {
                                    if (null != playingHolder) {
                                        updateNoPlayingView(playingHolder);
                                    }
                                    player.release();
                                }
                                playingHolder = viewHolder;
                                if (checkStoragePermission() == true){
                                    startMediaPlayer(arrayList.get(playingPosition).getFile_name(), arrayList.get(playingPosition).getFile_url());
                                } else {
                                    startMediaPlayerOnline(arrayList.get(playingPosition).getFile_url());
                                }
                            }
                            updatePlayingView();
                            playingHolder.progress_bar.setMax(player.getDuration());                //Set giá trị max cho progressbar
                            playingHolder.progress_bar.setEnabled(true);                            //Set nổi màu progressbar
                            if (player.isPlaying()) {
                                playingHolder.progress_bar.setVisibility(View.VISIBLE);
                                playingHolder.btn_play_pause.setBackgroundResource(R.drawable.ic_pause);
                            } else {
                                playingHolder.btn_play_pause.setBackgroundResource(R.drawable.ic_play);
                            }
                            viewHolder.loading_progress.setVisibility(View.INVISIBLE);
                        }
                    }.execute();
            }
        });
    }

    private boolean checkStoragePermission() {
        String permission = android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
        int res = activity.checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        super.onViewRecycled(holder);
        if (playingPosition == holder.getAdapterPosition()) {
            updateNoPlayingView(playingHolder);
            playingHolder = null;
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        ProgressBar progress_bar;
        ImageView btn_play_pause, btn_more;
        TextView tv_ringtonename;
        ProgressBar loading_progress;

        ViewHolder(View itemView) {
            super(itemView);
            btn_play_pause = itemView.findViewById(R.id.btn_music);
            loading_progress = itemView.findViewById(R.id.pro);
            btn_more = itemView.findViewById(R.id.btn_more);
            progress_bar = itemView.findViewById(R.id.progress_bar);
            tv_ringtonename = itemView.findViewById(R.id.tv_ringtonename);
        }
    }


    private void updatePlayingView() {
        playingHolder.progress_bar.setMax(player.getDuration());                //Set giá trị max cho progressbar
        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (player != null){
                    playingHolder.progress_bar.setProgress(player.getCurrentPosition());    //Set giá trị hiện tại cho progressbar
                }
                handler.postDelayed(this, 100);
            }
        });
        playingHolder.progress_bar.setEnabled(true);                            //Set nổi màu progressbar
        if (player.isPlaying()) {
            playingHolder.progress_bar.setVisibility(View.VISIBLE);
            playingHolder.btn_play_pause.setBackgroundResource(R.drawable.ic_pause);
        } else {
            playingHolder.btn_play_pause.setBackgroundResource(R.drawable.ic_play);
        }
    }

    private void updateNoPlayingView(ViewHolder holder) {
        holder.progress_bar.setVisibility(View.GONE);
        holder.progress_bar.setEnabled(false);                                  //Làm chìm màu progressbar
        holder.progress_bar.setProgress(0);                                     //Trả progressbar về 0
        holder.btn_play_pause.setBackgroundResource(R.drawable.ic_play);                //Set icon play cho button
    }

    private void startMediaPlayerOnline(String file_url) {
        Uri uri = Uri.parse(file_url);
        player = MediaPlayer.create(activity, uri);
        player.start();
        //Nếu chơi xong thì release và update ui
        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                releaseMediaPlayer();
            }
        });
    }

    private void startMediaPlayer(String file_name, String file_url) {
        File path = new File(Environment.getExternalStorageDirectory() + "/ringtone_app");
        String fileName = file_name+".mp3";
        File ringtone = new File(path, fileName);
        if (ringtone.exists()){
            Uri uri = Uri.parse(Environment.getExternalStorageDirectory() + "/ringtone_app/" + fileName);
            player = MediaPlayer.create(activity, uri);
            player.start();
            //Nếu chơi xong thì release và update ui
            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    releaseMediaPlayer();
                }
            });
        } else {
            Uri uri = Uri.parse(file_url);
            player = MediaPlayer.create(activity, uri);
            player.start();
            //Nếu chơi xong thì release và update ui
            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    releaseMediaPlayer();
                }
            });
        }

    }

    private void releaseMediaPlayer() {
        if (playingHolder != null) {
            updateNoPlayingView(playingHolder);
        }
        player.release();
        player = null;
        playingPosition = -1;
    }

    public void stopPlayer() {
        if (player != null) {
            releaseMediaPlayer();
        }
    }

    //Viết phương thức Click dùng ở ngoài
    public interface OnItemClickListener {
        void onItemClick(View itemView, int position);
    }
    public void setOnItemClickListener(OnItemClickListener listener){
        this.listener = listener;
    }
}
