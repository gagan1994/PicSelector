package com.example.gagan.picselector;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import java.util.Locale;

/**
 * Created by Gagan on 6/6/2018.
 */

public class MyMesasgeReceiver extends BroadcastReceiver {

    public void onReceive(Context context, Intent intent) {
        Bundle myBundle = intent.getExtras();
        SmsMessage[] messages = null;
        String strMessage = "";

        if (myBundle != null) {
            Object[] pdus = (Object[]) myBundle.get("pdus");
            messages = new SmsMessage[pdus.length];

            for (int i = 0; i < messages.length; i++) {
                messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
//                strMessage += "SMS From: " + messages[i].get();
//                strMessage += " : ";
                strMessage += messages[i].getMessageBody();
                strMessage += "\n";
            }
            Utils.getInstance().SpeekWithHeadPhone(context, strMessage);

            Toast.makeText(context, strMessage, Toast.LENGTH_SHORT).show();
        }
    }


}