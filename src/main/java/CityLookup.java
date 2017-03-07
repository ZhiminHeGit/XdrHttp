import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.model.*;

public class CityLookup {

    public static void main (String[] args) {
        System.out.println(CityLookup.lookupGoogleMap(new GPS(22.639258,113.810664)));
    }


    public static String lookupGoogleMap(GPS gps) {
        GeoApiContext context = new GeoApiContext();
        context.setApiKey("AIzaSyB_GOOW6aZsUtDxaJ3yyUPis8M1QG6WqXk");

        try {
            GeocodingResult[] results = GeocodingApi.newRequest(context)
                    .latlng(new LatLng(gps.getLat(), gps.getLon())).language("zh-CN").await();

        String neighbourhood = "", city = "", region = "",country = "";
            for (int i=0; i<results[0].addressComponents.length; i++)
            {
                for (int j = 0 ; j< results[0].addressComponents[i].types.length; j++) {
                    if (results[0].addressComponents[i].types[j] == AddressComponentType.SUBLOCALITY_LEVEL_1) {
                        //this is the object you are looking for
                        neighbourhood = results[0].addressComponents[i].longName + ",";
                    }
                    if (results[0].addressComponents[i].types[j] == AddressComponentType.LOCALITY) {
                        //this is the object you are looking for
                        city = results[0].addressComponents[i].longName + ",";
                    }
                    if (results[0].addressComponents[i].types[j] == AddressComponentType.ADMINISTRATIVE_AREA_LEVEL_1) {
                        //this is the object you are looking for
                        region = results[0].addressComponents[i].longName + ",";
                    }
                    if (results[0].addressComponents[i].types[j] == AddressComponentType.COUNTRY) {
                        //this is the object you are looking for
                        country = results[0].addressComponents[i].longName;
                    }
                }
            }
            return neighbourhood + city + region + country;



        } catch (Exception e) {
            e.printStackTrace();
            return "unknown";
        }
    }

}
