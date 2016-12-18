package com.mapbox.iotled;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.android.things.contrib.driver.ht16k33.AlphanumericDisplay;
import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.GpioCallback;
import com.google.android.things.pio.PeripheralManagerService;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

  private static final String LOG_TAG = MainActivity.class.getName();

  private TextView textView;

  private PeripheralManagerService service;
  private Gpio mButtonGpio;
  private Gpio mLedGreenGpio;
  private Gpio mLedRedGpio;
  private AlphanumericDisplay mDisplay;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_main);
    textView = (TextView) findViewById(R.id.textView);
    setText("Created.");

    service = new PeripheralManagerService();
    setupButton();
    setupGreenLED();
    setupRedLED();
    setupDisplay();
  }

  private void setupButton() {
    try {
      String pinName = "BCM21";
      mButtonGpio = service.openGpio(pinName);
      mButtonGpio.setDirection(Gpio.DIRECTION_IN);
      mButtonGpio.setEdgeTriggerType(Gpio.EDGE_FALLING);
      mButtonGpio.registerGpioCallback(new GpioCallback() {
        @Override
        public boolean onGpioEdge(Gpio gpio) {
          setText("GPIO changed, button pressed");

          // Toggle the GPIO state
          try {
            mLedGreenGpio.setValue(!mLedGreenGpio.getValue());
            setText("State green set to " + mLedGreenGpio.getValue());

            mLedRedGpio.setValue(!mLedRedGpio.getValue());
            setText("State red set to " + mLedRedGpio.getValue());
          } catch (IOException e) {
            setText("Error switching the green LED: " + e.getMessage());
          }

          // Return true to continue listening to events
          return true;
        }
      });
    } catch (IOException e) {
      setText("Error on PeripheralIO API: " + e.getMessage());
    }
  }

  private void setupGreenLED() {
    try {
      String pinName = "BCM5";
      mLedGreenGpio = service.openGpio(pinName);
      mLedGreenGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
    } catch (IOException e) {
      setText("Error on PeripheralIO API: " + e.getMessage());
    }
  }

  private void setupRedLED() {
    try {
      String pinName = "BCM6";
      mLedRedGpio = service.openGpio(pinName);
      mLedRedGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
    } catch (IOException e) {
      setText("Error on PeripheralIO API: " + e.getMessage());
    }
  }

  private void setupDisplay() {
    try {
      mDisplay = new AlphanumericDisplay("I2C1");
      mDisplay.setEnabled(true);
      mDisplay.clear();
      setText("Initialized I2C Display");
      mDisplay.display("Okey");
    } catch (IOException e) {
      setText("Error initializing display: " + e.getMessage());
      mDisplay = null;
    }
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (mButtonGpio != null) {
      // Close the Gpio pins
      setText("Closing GPIO pins");
      try {
        mButtonGpio.close();
        mLedGreenGpio.close();
      } catch (IOException e) {
        setText("Error on PeripheralIO API: " + e.getMessage());
      } finally {
        mButtonGpio = null;
        mLedGreenGpio = null;
      }
    }
  }

  private void setText(String message) {
    Log.d(LOG_TAG, message);
    textView.setText(message);
  }
}
