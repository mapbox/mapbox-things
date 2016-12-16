package com.mapbox.iotbasic;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.mapbox.services.commons.ServicesException;
import com.mapbox.services.commons.models.Position;
import com.mapbox.services.directions.v5.DirectionsCriteria;
import com.mapbox.services.directions.v5.MapboxDirections;
import com.mapbox.services.directions.v5.models.DirectionsResponse;

import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

  private static final String LOG_TAG = MainActivity.class.getName();

  private TextView textView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    textView = (TextView) findViewById(R.id.mainTextView);

    Position whiteHouse = Position.fromCoordinates(-77.0365, 38.8977);
    Position dupontCircle = Position.fromCoordinates(-77.04341, 38.90962);

    Log.d(LOG_TAG, "Building directions client.");
    MapboxDirections client;

    try {
      client = new MapboxDirections.Builder()
        .setAccessToken("")
        .setOrigin(dupontCircle)
        .setDestination(whiteHouse)
        .setProfile(DirectionsCriteria.PROFILE_WALKING)
        .build();
    } catch (ServicesException e) {
      setMessage("Client failed: " + e.getMessage());
      e.printStackTrace();
      return;
    }

    Log.d(LOG_TAG, "Sending request to Mapbox.");
    client.enqueueCall(new Callback<DirectionsResponse>() {
      @Override
      public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
        setMessage(String.format(Locale.US,
          "The distance between Dupont Circle and The White House is %.1f meters (a %.1f minutes walk): ",
          response.body().getRoutes().get(0).getDistance(),
          response.body().getRoutes().get(0).getDuration() / 60.0));
      }

      @Override
      public void onFailure(Call<DirectionsResponse> call, Throwable t) {
        setMessage("Request failed: " + t.getMessage());
        t.printStackTrace();
      }
    });
  }

  private void setMessage(final String message) {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        Log.d(LOG_TAG, "Message: " + message);
        textView.setText(message);
      }
    });
  }
}
