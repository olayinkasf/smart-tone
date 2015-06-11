package com.olayinka.smart.tone.task;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.CountDownTimer;

import java.io.IOException;

/**
 * Created by Olayinka on 11/4/2014.
 */
public class MediaPlayBackTask {

    private static MediaPlayer sMediaPlayer;
    private static AudioManager sAudioManager;

    public static synchronized void play(Context context, String path) throws IOException {
        stop();
        sAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        int result = sAudioManager.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            sMediaPlayer = new MediaPlayer();
            sMediaPlayer.setDataSource(path);
            sMediaPlayer.prepare();
            sMediaPlayer.start();
        }

        CountDownTimer timer = new CountDownTimer(50000, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                stop();
            }
        };
        timer.start();
    }

    public static synchronized void stop() {
        if (sMediaPlayer != null && sMediaPlayer.isPlaying()) {
            sMediaPlayer.stop();
            sMediaPlayer.release();
            sMediaPlayer = null;
        }
        if (sAudioManager != null) {
            sAudioManager.abandonAudioFocus(null);
            sAudioManager = null;
        }
    }
}