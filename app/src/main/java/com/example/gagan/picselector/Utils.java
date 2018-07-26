package com.example.gagan.picselector;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.speech.tts.TextToSpeech;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.TableRow;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by Gagan on 6/6/2018.
 */

public class Utils {
    private static Utils _instance = new Utils();
    private TextToSpeech tts;

    public static final Utils getInstance() {
        return _instance;
    }

    private Utils() {

    }

    public void Speek(Context context, String strMessage) {

        tts = new TextToSpeech(context, new TextToSpeech.OnInitListener() {

            @Override
            public void onInit(int status) {
                // TODO Auto-generated method stub
                if (status == TextToSpeech.SUCCESS) {
                    int result = tts.setLanguage(Locale.US);
                    if (result == TextToSpeech.LANG_MISSING_DATA ||
                            result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("error", "This Language is not supported");
                    } else {

                    }
                } else
                    Log.e("error", "Initilization Failed!");
            }
        });
        tts.speak(strMessage, TextToSpeech.QUEUE_FLUSH, null);
    }

    public boolean isHeadphonesPlugged(Context context) {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        return audioManager.isWiredHeadsetOn();
    }

    public void SpeekWithHeadPhone(Context context, String strMessage) {
//            Utils.getInstance().Speek(context, strMessage);
//        }
        ArrayList<String> allMessagesToRead = new ArrayList<>();
        allMessagesToRead.add(strMessage);
        SpeekWithHeadPhone(context, allMessagesToRead);

    }

    public void SpeekWithHeadPhone(Context context, ArrayList<String> strMessage) {
        if (Utils.getInstance().isHeadphonesPlugged(context)) {
            Intent speechIntent = new Intent(context, TextToSpeechService.class);
            speechIntent.putStringArrayListExtra(TextToSpeechService.TEXT_TO_READ, strMessage);
            context.startService(speechIntent);
        }
    }

    private BroadcastReceiver onNotice = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String pack = intent.getStringExtra("package");
            String title = intent.getStringExtra("title");
            String text = intent.getStringExtra("text");


        }
    };
}
