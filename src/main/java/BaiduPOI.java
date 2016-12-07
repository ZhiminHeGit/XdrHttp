import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by usrc on 6/22/2016.
 */
public class BaiduPOI {

    // get data object
    //String projectname = data.getString("name"); /

    // http://api.map.baidu.com/geocoder/v2/?ak=E4805d16520de693a3fe707cdc962045&callback=renderReverse&location=39.983424,116.322987&output=json&pois=1
    public String getBaiDuPoi(GPS gps) {
        String urlString = String.format("http://api.map.baidu.com/geocoder/v2/?ak=2YCvWTNS5lnQn75GRMhY5zxP&location=%f,%f&output=json&pois=1", gps.getLat(), gps.getLon());
        String responseString = "";

        try {
            URL url = new URL(urlString);
            URLConnection con = url.openConnection();
            InputStream in = con.getInputStream();
            String encoding = con.getContentEncoding();
            encoding = encoding == null ? "UTF-8" : encoding;
           // responseString = IOUtils.toString(in, encoding);
            //  System.out.println(response_string);
            JSONObject response = new JSONObject(responseString); // json
            JSONObject result = response.getJSONObject("result");
            JSONArray pois = result.getJSONArray("pois");
            for (Object poi : pois) {
                String[] tags = ((JSONObject) poi).get("tag").toString().split(";");
                for (String tag : tags) {
                    System.out.print(tag + ",");
                }
                System.out.print(((JSONObject) poi).get("poiType") + ",");
            }
            JSONArray poiRegions = result.getJSONArray("poiRegions");
            for (Object poiRegion : poiRegions) {
                System.out.print(((JSONObject) poiRegion).get("tag") + ",");
            }
            System.out.println();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return responseString;
    }
}
