package com.mapbox.rainbowhat;

import com.mapbox.services.commons.ServicesException;
import com.mapbox.services.commons.models.Position;
import com.mapbox.services.directions.v5.DirectionsCriteria;
import com.mapbox.services.directions.v5.MapboxDirections;
import com.mapbox.services.directions.v5.models.DirectionsResponse;
import com.mapbox.services.geocoding.v5.GeocodingCriteria;
import com.mapbox.services.geocoding.v5.MapboxGeocoding;
import com.mapbox.services.geocoding.v5.models.CarmenFeature;
import com.mapbox.services.geocoding.v5.models.GeocodingResponse;

import java.io.IOException;
import java.util.logging.Logger;

import retrofit2.Response;

/**
 * Created by antonio on 12/19/16.
 */
public class ActionManager {

  private static final Logger logger = Logger.getLogger(ActionManager.class.getName());

  private BlogComponent blog;

  public ActionManager() {
    blog = new BlogComponent();
  }

  public String getActionReadBlog(boolean markdown) {
    return blog.lastWeek(markdown);
  }

  public String getActionSearchPlaces() {
    try {
      MapboxGeocoding client = new MapboxGeocoding.Builder()
        .setAccessToken(Constants.MAPBOX_ACCESS_TOKEN)
        .setCoordinates(Position.fromCoordinates(-77.045984, 38.933721))
        .setGeocodingType(GeocodingCriteria.TYPE_POI)
        .build();
      Response<GeocodingResponse> result = client.executeCall();
      if (result.body().getFeatures().size() == 0) {
        return "Sorry, couldn't find any interesting places nearby.";
      } else {
        CarmenFeature place = result.body().getFeatures().get(0);
        return String.format("Have you tried %s?", place.getPlaceName());
      }
    } catch (ServicesException | IOException e) {
      e.printStackTrace();
      return "Oops, that didn't work. Could you try again in a few?";
    }
  }

  public String getActionRouteCommute() {
    try {
      MapboxDirections client = new MapboxDirections.Builder()
        .setAccessToken(Constants.MAPBOX_ACCESS_TOKEN)
        .setOrigin(Position.fromCoordinates(-77.045984, 38.933721))
        .setDestination(Position.fromCoordinates(-77.032328, 38.91318))
        .setProfile(DirectionsCriteria.PROFILE_DRIVING)
        .setOverview(DirectionsCriteria.OVERVIEW_SIMPLIFIED)
        .build();
      Response<DirectionsResponse> result = client.executeCall();
      return String.format("The DC office is about %.0f kilometers away, that's a %.0f minutes drive.",
        result.body().getRoutes().get(0).getDistance() / 1000.0,
        result.body().getRoutes().get(0).getDuration() / 60.0);
    } catch (ServicesException | IOException e) {
      e.printStackTrace();
      return "Oops, that didn't work. Could you try again in a few?";
    }
  }

}
