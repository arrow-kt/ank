Welcome to doc two

```java:ank
import com.mapbox.api.directions.v5.MapboxDirections;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.geojson.Point;

import retrofit2.Response;

MapboxDirections.Builder builder = MapboxDirections.builder();

builder.accessToken("");
builder.origin(Point.fromLngLat(-95.6332, 29.7890));
builder.destination(Point.fromLngLat(-95.3591, 29.7576));

Response<DirectionsResponse> response = builder.build().executeCall();

Double distance = response.body().routes().get(0).distance();
return distance;
```