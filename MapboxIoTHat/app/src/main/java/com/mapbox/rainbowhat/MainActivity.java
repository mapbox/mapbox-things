package com.mapbox.rainbowhat;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.things.contrib.driver.button.Button;
import com.google.android.things.contrib.driver.ht16k33.AlphanumericDisplay;
import com.google.android.things.contrib.driver.ht16k33.Ht16k33;
import com.google.android.things.contrib.driver.rainbowhat.RainbowHat;
import com.google.android.things.pio.Gpio;
import com.squareup.picasso.Picasso;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

  private final static String LOG_TAG = MainActivity.class.getSimpleName();

  private TextView textView;
  private ImageView imageView;

  private AppTextToSpeech appTextToSpeech;
  private ActionManager actionManager;

  private Gpio ledRed;
  private Gpio ledGreen;
  private AlphanumericDisplay segment;
  private Button buttonA;
  private Button buttonB;
  private Button buttonC;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    textView = (TextView) findViewById(R.id.text_view);
    imageView = (ImageView) findViewById(R.id.static_map);

    appTextToSpeech = new AppTextToSpeech(this);
    actionManager = new ActionManager();
  }

  @Override
  protected void onStart() {
    super.onStart();
    setMessage("Ready.");

    try {
      setupLedRed();
      setupLedGreen();
      setupSegment();
      setupButtonA();
      setupButtonB();
      setupButtonC();
    } catch (IOException e) {
      setMessage("Error: " + e.getMessage());
    }
  }

  private void setupLedRed() throws IOException {
    ledRed = RainbowHat.openLedRed();
  }

  private void switchRedLed(boolean on) throws IOException {
    ledRed.setValue(on);
  }

  private void setupLedGreen() throws IOException {
    ledGreen = RainbowHat.openLedGreen();
  }

  private void switchGreenLed(boolean on) throws IOException {
    ledGreen.setValue(on);
  }

  private void setupSegment() throws IOException {
    segment = RainbowHat.openDisplay();
    segment.setBrightness(Ht16k33.HT16K33_BRIGHTNESS_MAX);
    segment.setEnabled(true);
    setSegmentText("MPBX");
  }

  private void setSegmentText(String text) {
    try {
      segment.display(text);
      setMessage(text);
    } catch (IOException e) {
      setMessage("Error: " + e.getMessage());
    }
  }

  private void setupButtonA() throws IOException {
    buttonA = RainbowHat.openButtonA();
    buttonA.setOnButtonEventListener(new Button.OnButtonEventListener() {
      @Override
      public void onButtonEvent(Button button, boolean pressed) {
        if (pressed) {
          Log.d(LOG_TAG, "button A pressed.");
          doLedDance();
          setSegmentText("CAR");
          new Thread(new Runnable() {
            @Override
            public void run() {
              String[] result = actionManager.getActionRouteCommute();
              appTextToSpeech.say(result[0]);
              loadImage(result[1]);
            }
          }).start();
        }
      }
    });
  }

  private void loadImage(String url) {
    try {
      Picasso.with(this).load(url).centerCrop().into(imageView);
    } catch (Exception e) {
      setMessage("Error: " + e.getMessage());
    }
  }

  private void setupButtonB() throws IOException {
    buttonB = RainbowHat.openButtonB();
    buttonB.setOnButtonEventListener(new Button.OnButtonEventListener() {
      @Override
      public void onButtonEvent(Button button, boolean pressed) {
        if (pressed) {
          Log.d(LOG_TAG, "button B pressed.");
          doLedDance();
          setSegmentText("GEO");
          new Thread(new Runnable() {
            @Override
            public void run() {
              appTextToSpeech.say(actionManager.getActionSearchPlaces());
            }
          }).start();
        }
      }
    });
  }

  private void setupButtonC() throws IOException {
    buttonC = RainbowHat.openButtonC();
    buttonC.setOnButtonEventListener(new Button.OnButtonEventListener() {
      @Override
      public void onButtonEvent(Button button, boolean pressed) {
        if (pressed) {
          Log.d(LOG_TAG, "button C pressed.");
          doLedDance();
          setSegmentText("BLOG");
          new Thread(new Runnable() {
            @Override
            public void run() {
              appTextToSpeech.say(actionManager.getActionReadBlog(false));
            }
          }).start();
        }
      }
    });
  }

  private void doLedDance() {
    new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          switchGreenLed(false);
          switchRedLed(true);
          for (int i = 0; i < 10; i++) {
            switchGreenLed(!ledGreen.getValue());
            switchRedLed(!ledRed.getValue());
            Thread.sleep(200);
          }
          switchGreenLed(true);
          switchRedLed(false);
        } catch (InterruptedException | IOException e) {
          setMessage("Error doing LED dance: " + e.getMessage());
        }
      }
    }).start();
  }

  @Override
  protected void onStop() {
    super.onStop();
    setMessage("Done.");

    appTextToSpeech.shutdown();

    try {
      ledRed.close();
      ledGreen.close();
      segment.close();
      buttonA.close();
      buttonB.close();
      buttonC.close();
    } catch (IOException e) {
      setMessage("Error: " + e.getMessage());
    }
  }

  private void setMessage(String message) {
    Log.d(LOG_TAG, message);
    textView.setText(String.format("%s\n%s", message, textView.getText()));
  }
}
