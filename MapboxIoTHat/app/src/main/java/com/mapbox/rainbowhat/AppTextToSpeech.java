package com.mapbox.rainbowhat;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import java.util.Locale;
import java.util.UUID;

/**
 * Created by antonio on 1/6/17.
 */

public class AppTextToSpeech implements TextToSpeech.OnInitListener {

  private final static String LOG_TAG = AppTextToSpeech.class.getSimpleName();

  private TextToSpeech textToSpeech;
  private boolean isReady;

  public AppTextToSpeech(Context context) {
    textToSpeech = new TextToSpeech(context, this);
    textToSpeech.setLanguage(Locale.US);
    isReady = false;
  }

  public boolean isReady() {
    return isReady;
  }

  public void shutdown() {
    if (textToSpeech != null) {
      textToSpeech.shutdown();
    }
  }

  public void say(String text) {
    if (isReady()) {
      Log.d(LOG_TAG, "Saying: " + text);
      textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, UUID.randomUUID().toString());
      Log.d(LOG_TAG, "Done.");
    }
  }

  @Override
  public void onInit(int status) {
    if (status == TextToSpeech.SUCCESS) {
      isReady = true;
      Log.d(LOG_TAG, "AppTextToSpeech is now ready.");
      say("Hey, this is Mapbox, how can I help you?");
    } else {
      Log.d(LOG_TAG, "AppTextToSpeech initialization failed: " + String.valueOf(status));
    }
  }
}
