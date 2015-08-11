/*
 * Copyright 2015
 *
 * Olayinka S. Folorunso <mail@olayinkasf.com>
 * http://olayinkasf.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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