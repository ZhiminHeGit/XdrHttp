/**
 * Created by usrc on 6/21/2016.
 */

public class GPS {
    double lon;
    double lat;

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public String toString() {
        return "" + lat + "," + lon;
    }

    public GPS(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
    }

    public GPS(String gps) {
        String[] parts = gps.split(",");
        lat = Double.parseDouble(parts[0]);
        lon = Double.parseDouble(parts[1]);
    }
    public boolean equals(GPS gps) {
        return (gps != null && lat == gps.lat && lon == gps.lon);
    }


    public static double getTravelDistance(GPS gps1, GPS gps2) {
        return distance(gps1.lat, gps1.lon, gps2.lat, gps2.lon, "K");
    }

    //            System.out.println(distance(32.9697, -96.80322, 29.46786, -98.53506, "M") + " Miles\n");
    //            System.out.println(distance(32.9697, -96.80322, 29.46786, -98.53506, "K") + " Kilometers\n");
    //           System.out.println(distance(32.9697, -96.80322, 29.46786, -98.53506, "N") + " Nautical Miles\n");
    private static double distance(double lat1, double lon1, double lat2, double lon2, String unit) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) +
                Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        if (unit == "K") {
            dist = dist * 1.609344;
        } else if (unit == "N") {
            dist = dist * 0.8684;
        }
        return (dist);
    }

    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    /*::	This function converts decimal degrees to radians						 :*/
    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    private static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
	/*::	This function converts radians to decimal degrees						 :*/
	/*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    private static double rad2deg(double rad) {
        return (rad * 180 / Math.PI);
    }
}