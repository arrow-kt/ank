package arrow.android;


import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.geojson.Point;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;
import retrofit2.Response;

import java.io.IOException;

class Sample {

    public static void main(String[] args) throws IOException {

//        MapboxDirections.Builder builder = MapboxDirections.builder();
//
//        builder.accessToken("YOUR_MAPBOX_ACCESS_TOKEN_GOES_HERE");
//        builder.origin(Point.fromLngLat(-95.6332, 29.7890));
//        builder.destination(Point.fromLngLat(-95.3591, 29.7576));
//
//        Response<DirectionsResponse> response = builder.build().executeCall();
//
//        Double distance = response.body().routes().get(0).distance();
//        System.out.println("Distance: " + distance);
//
        Point origin = Point.fromLngLat(-77.03613, 38.90992);
        Point destination = Point.fromLngLat(-77.0365, 38.8977);

        Response<DirectionsResponse> response = NavigationRoute.builder()
                .accessToken("YOUR_MAPBOX_ACCESS_TOKEN_GOES_HERE")
                .origin(origin)
                .destination(destination)
                .build()
                .getCall().execute();

        String voiceLanguage = response.body().routes().get(0).voiceLanguage();
        System.out.println("Voice language: " + voiceLanguage);
    }
}
