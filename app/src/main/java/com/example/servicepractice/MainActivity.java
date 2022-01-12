package com.example.servicepractice;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    SeekBar sbPosition;
    TextView playtime, time;
    ImageView btn_play;
    Button btn_stop;

    private MusicBinder musicBinder;
    private Handler handler = new Handler();
    Intent MediaServiceIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MediaServiceIntent = new Intent(this, MediaService.class);
        startService(MediaServiceIntent);


        bindService(MediaServiceIntent, serviceConnection, BIND_AUTO_CREATE);
        playtime= findViewById(R.id.tv_playtime);
        time = findViewById(R.id.tv_time);
        sbPosition = findViewById(R.id.seekBar);
        btn_play = findViewById(R.id.iv_play);
        btn_stop = findViewById(R.id.button);
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            musicBinder = (MusicBinder) service;
            if (musicBinder.isPlaying()) {
                btn_play.setImageResource(R.drawable.ic_baseline_pause);
            } else {
                btn_play.setImageResource(R.drawable.ic_baseline_play_arrow);
            }
            sbPosition.setMax(musicBinder.getProgress());
            sbPosition.setOnSeekBarChangeListener(position);

            btn_play.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    setIsPlayButton(btn_play);
                }
            });
            btn_stop.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    handler.removeCallbacks(runnable);
                    musicBinder.closeMedia();
                    stopService(MediaServiceIntent);
                    finish();
                }
            });

            handler.post(runnable);

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            try {
                sbPosition.setProgress(musicBinder.getPlayPosition());

                String elapsedTime = createTimeLabel(musicBinder.getPlayPosition());
                playtime.setText(elapsedTime);
                String remainingTime = createTimeLabel(musicBinder.getProgress() - musicBinder.getPlayPosition());
                time.setText("- " + remainingTime);


                handler.postDelayed(runnable, 1000);
            }catch (Exception e){

            }
        }
    };

    private void setIsPlayButton(ImageView bt) {
        if (musicBinder.isPlaying()) {
            musicBinder.pauseMusic();
            bt.setImageResource(R.drawable.ic_baseline_play_arrow);
        } else {
            musicBinder.playMusic();
            bt.setImageResource(R.drawable.ic_baseline_pause);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(serviceConnection);
    }

    private SeekBar.OnSeekBarChangeListener position = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean isSeekbarOnTouch) {
            if (isSeekbarOnTouch) {
                musicBinder.seekToPosition(i);
            }

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

    private String createTimeLabel(int time) {
        String timeLabel = "";
        int min = time / 1000 / 60;
        int sec = time / 1000 % 60;

        timeLabel = min + ":";
        if (sec < 10) timeLabel += "0";
        timeLabel += sec;

        return timeLabel;
    }
}