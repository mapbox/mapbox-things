package com.mapbox.rainbowhat;

import android.app.Activity;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.util.Log;

import java.util.List;

import static android.app.Activity.RESULT_OK;

/**
 * Created by antonio on 1/6/17.
 */

public class AppSpeechRecognizer {

  private static final String LOG_TAG = AppSpeechRecognizer.class.getSimpleName();
  private static final int SPEECH_REQUEST_CODE = 0;

  private Activity activity;

  public AppSpeechRecognizer(Activity activity) {
    this.activity = activity;
  }

  // Create an intent that can start the Speech Recognizer activity
  public void displaySpeechRecognizer() {
    Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
    // Start the activity, the intent will be populated with the speech text
    activity.startActivityForResult(intent, SPEECH_REQUEST_CODE);
  }

  // This callback is invoked when the Speech Recognizer returns.
  // This is where you process the intent and extract the speech text from the intent.
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK) {
      List<String> results = data.getStringArrayListExtra(
        RecognizerIntent.EXTRA_RESULTS);
      String spokenText = results.get(0);
      Log.d(LOG_TAG, "You said: " + spokenText);
    }
  }
}
