package com.mapbox.rainbowhat;

import com.mapbox.services.api.ServicesException;
import com.mapbox.services.api.directions.v5.DirectionsCriteria;
import com.mapbox.services.api.directions.v5.MapboxDirections;
import com.mapbox.services.api.directions.v5.models.DirectionsResponse;
import com.mapbox.services.api.geocoding.v5.GeocodingCriteria;
import com.mapbox.services.api.geocoding.v5.MapboxGeocoding;
import com.mapbox.services.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.services.api.geocoding.v5.models.GeocodingResponse;
import com.mapbox.services.api.staticimage.v1.MapboxStaticImage;
import com.mapbox.services.api.staticimage.v1.models.StaticMarkerAnnotation;
import com.mapbox.services.api.staticimage.v1.models.StaticPolylineAnnotation;
import com.mapbox.services.commons.models.Position;

import java.io.IOException;
import java.util.Locale;
import java.util.logging.Logger;

import retrofit2.Response;

/**
 * Created by antonio on 12/19/16.
 */
public class ActionManager {

  private static final Logger logger = Logger.getLogger(ActionManager.class.getName());

  private final Position wdc = Position.fromCoordinates(-77.016389, 38.904722);
  private final Position nyc = Position.fromCoordinates(-74.0059, 40.7127);

  // 1200w x 800h
  private static final int largeWidth = 1200;
  private static final int largeHeight = 800;

  // https://www.mapbox.com/base/styling/color/
  private static final String COLOR_GREEN = "56b881";
  private static final String COLOR_RED = "e55e5e";
  private static final String COLOR_BLUE = "3887be";

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
          .setCoordinates(nyc)
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

  public String[] getActionRouteCommute() {
    try {
      MapboxDirections client = new MapboxDirections.Builder()
          .setAccessToken(Constants.MAPBOX_ACCESS_TOKEN)
          .setOrigin(wdc)
          .setDestination(nyc)
          .setProfile(DirectionsCriteria.PROFILE_DRIVING_TRAFFIC)
          .setOverview(DirectionsCriteria.OVERVIEW_SIMPLIFIED)
          .build();
      Response<DirectionsResponse> result = client.executeCall();
      String voice = String.format(Locale.US,
          "New York to Washington DC is about %.0f kilometers away, that's a %.0f minutes drive.",
          result.body().getRoutes().get(0).getDistance() / 1000.0,
          result.body().getRoutes().get(0).getDuration() / 60.0);
      String url = getRouteMap(wdc, nyc, result.body().getRoutes().get(0).getGeometry());
      return new String[] {voice, url};
    } catch (ServicesException | IOException e) {
      e.printStackTrace();
      return new String[] {"Oops, that didn't work. Could you try again in a few?", null};
    }
  }

  public static String getRouteMap(Position origin, Position destination, String geometry) {
    StaticMarkerAnnotation markerOrigin = new StaticMarkerAnnotation.Builder()
        .setName(com.mapbox.services.Constants.PIN_LARGE)
        .setPosition(origin)
        .setColor(COLOR_GREEN)
        .build();
    StaticMarkerAnnotation markerDestination = new StaticMarkerAnnotation.Builder()
        .setName(com.mapbox.services.Constants.PIN_LARGE)
        .setPosition(destination)
        .setColor(COLOR_RED)
        .build();
    StaticPolylineAnnotation route = new StaticPolylineAnnotation.Builder()
        .setPolyline(geometry)
        .setStrokeColor(COLOR_BLUE)
        .setStrokeOpacity(1)
        .setStrokeWidth(5)
        .build();
    MapboxStaticImage client = new MapboxStaticImage.Builder()
        .setAccessToken(Constants.MAPBOX_ACCESS_TOKEN)
        .setWidth(largeWidth)
        .setHeight(largeHeight)
        .setStyleId(com.mapbox.services.Constants.MAPBOX_STYLE_TRAFFIC_DAY)
        .setAuto(true)
        .setStaticMarkerAnnotations(markerOrigin, markerDestination)
        .setStaticPolylineAnnotations(route)
        .setZoom(15)
        .build();
    return client.getUrl().toString();
  }

}
