package com.musicplayer;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;

import com.gauravk.audiovisualizer.visualizer.BarVisualizer;
import com.musicplayer.databinding.ActivityPlayerBinding;

import java.io.File;
import java.util.ArrayList;

public class PlayerActivity extends AppCompatActivity {
    private ActivityPlayerBinding binding;
    String sname;
    public static final String EXTRA_NAME="song_name";
    static MediaPlayer mediaPlayer;
    int position;
    ArrayList<File> mySongs;
    Thread updateSeekbar;
    BarVisualizer visualizer;

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==android.R.id.home){
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        if(visualizer!=null){
            visualizer.release();
        }
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPlayerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

//        getSupportActionBar().setTitle("Now Playing");
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().setDisplayShowHomeEnabled(true);

        visualizer=findViewById(R.id.visualizer);

        if(mediaPlayer!=null){
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        Intent i=getIntent();
        Bundle bundle=i.getExtras();

        mySongs=(ArrayList) bundle.getParcelableArrayList("songs");

        String songName=i.getStringExtra("songname");

        position=bundle.getInt("pos",0);

        binding.txtsn.setSelected(true);

        Uri uri=Uri.parse(mySongs.get(position).toString());

        sname=mySongs.get(position).getName();

        binding.txtsn.setText(sname);

        mediaPlayer=MediaPlayer.create(getApplicationContext(),uri);
        mediaPlayer.start();

        updateSeekbar=new Thread(){
            @Override
            public void run() {
                int totalDuration=mediaPlayer.getDuration();
                int currentposition=0;
                while(currentposition<totalDuration){
                    try{
                        sleep(500);
                        currentposition=mediaPlayer.getCurrentPosition();
                        binding.seekbar.setProgress(currentposition);
                    }catch (InterruptedException| IllegalStateException e){
                        e.printStackTrace();
                    }
                }
            }
        };
        binding.seekbar.setMax(mediaPlayer.getDuration());
        updateSeekbar.start();
        binding.seekbar.getProgressDrawable().setColorFilter(getResources().getColor(R.color.red), PorterDuff.Mode.MULTIPLY);
        binding.seekbar.getThumb().setColorFilter(getResources().getColor(R.color.red), PorterDuff.Mode.SRC_IN);

        binding.seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mediaPlayer.seekTo(binding.seekbar.getProgress());

            }
        });
        String endTime=createTime(mediaPlayer.getDuration());
        binding.textend.setText(endTime);

        final Handler handler=new Handler();
        final int delay=1000;

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                String currentTime=createTime(mediaPlayer.getCurrentPosition());
                binding.textstart.setText(currentTime);
                handler.postDelayed(this,delay);
            }
        },delay);

        binding.playbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mediaPlayer.isPlaying()){
                    binding.playbtn.setBackgroundResource(R.drawable.baseline_play_arrow_24);
                    mediaPlayer.pause();
                }else{
                    binding.playbtn.setBackgroundResource(R.drawable.baseline_pause_24);
                    mediaPlayer.start();
                }


            }
        });
        binding.nextbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.stop();
                mediaPlayer.release();
                position=((position+1)%mySongs.size());

                Uri u=Uri.parse(mySongs.get(position).toString());

                sname=mySongs.get(position).getName();

                binding.txtsn.setText(sname);

                mediaPlayer=MediaPlayer.create(getApplicationContext(),u);
                mediaPlayer.start();
                binding.playbtn.setBackgroundResource(R.drawable.baseline_pause_24);
                startAnimation(binding.imageview);

                int audioSessionId=mediaPlayer.getAudioSessionId();
                if(audioSessionId!=-1){
                    visualizer.setAudioSessionId(audioSessionId);
                }
            }
        });

        binding.prevbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.stop();
                mediaPlayer.release();
                position=((position-1)<0)?(mySongs.size()-1):(position-1);

                Uri u=Uri.parse(mySongs.get(position).toString());

                sname=mySongs.get(position).getName();

                binding.txtsn.setText(sname);

                mediaPlayer=MediaPlayer.create(getApplicationContext(),u);
                mediaPlayer.start();
                binding.playbtn.setBackgroundResource(R.drawable.baseline_pause_24);
                startAnimation(binding.imageview);

                int audioSessionId=mediaPlayer.getAudioSessionId();
                if(audioSessionId!=-1){
                    visualizer.setAudioSessionId(audioSessionId);
                }
            }
        });
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                binding.nextbtn.performClick();
            }
        });


        int audioSessionId=mediaPlayer.getAudioSessionId();

        if(audioSessionId != -1)
        {
            visualizer.setAudioSessionId(audioSessionId);
        }



        binding.forwardbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mediaPlayer.isPlaying()){
                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition()+10000);
                }
            }
        });
        binding.rewindbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mediaPlayer.isPlaying()){
                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition()-10000);
                }
            }
        });
    }
    public void startAnimation( View view){
        ObjectAnimator animator=ObjectAnimator.ofFloat(binding.imageview,"rotation",0f,360f);
        animator.setDuration(1000);
        AnimatorSet animatorSet=new AnimatorSet();
        animatorSet.playTogether(animator);
        animatorSet.start();
    }
    public String createTime(int duration){
        String time="";
        int min=duration/1000/60;
        int sec=duration/1000%60;

        time+=min+":";

        if(sec<10){
            time+="0";
        }
        time+=sec;

        return time;
    }
}