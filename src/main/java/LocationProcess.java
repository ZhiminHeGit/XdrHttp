import java.util.*;

/**
 * Created by usrc on 5/25/2016.
 */
public class LocationProcess extends DailyProcess {

    Map<Long, Location> lastLocationMap = new HashMap();
    Airports airports = new Airports();
    int count = 0;
    Map<String, Integer> routeMap = new HashMap();
    public class Location {
        GPS gps;
        int hop;
        long time;
        public String toString() {
            return gps.toString() + "," + hop;
        }
    }

    public LocationProcess(String supportDir) {
        headLine = "imsi,time, home_mcc, home_mnc, mcc,mnc,area,cell,lon,lat,hop,travel_distance \n";
    }

    public static void main(String[] args) {
        String input = "/Volumes/DataDisk/CMIRaw/201610010000-HTTP.csv";
        String output = "/Volumes/DataDisk/Data/location.csv";
        LocationProcess locationProcess = new LocationProcess("/Volumes/DataDisk/Data/");
        if (input.contains("STP")) {
            locationProcess.recordType = RecordType.SCCP;
        }
        locationProcess.process(input, output, args);
    }

    @Override
    public boolean process(XdrHttp xdrHttp) {
        long imsi = xdrHttp.getImsi();
        final int MIN_FLY_DISTANCE = 200;
        final int MIN_FLY_SPEED = 100;
        GPS gps = xdrHttp.getGps();
        if (gps != null) {
            Location last_location = lastLocationMap.get(imsi);
            double travel_distance = 0;
            if (last_location == null) {
                last_location = new Location();
                lastLocationMap.put(imsi, last_location);

            } else if (last_location.gps != gps) {
                travel_distance = GPS.getTravelDistance(gps, last_location.gps);
                if (travel_distance > MIN_FLY_DISTANCE) {
                    long time_diff = xdrHttp.getDate() - last_location.time;
                    double hour = time_diff / 1000.0 / 3600.0;
                    double speed = travel_distance / hour; // km per hour

                    if (speed > MIN_FLY_SPEED) {
                        Airport from = airports.lookup(last_location.gps);
                        Airport to = airports.lookup(gps);
                        if (from != null && to != null) {
                            String route = from.getCityAndCountry() + "," + from.getGPS().toString() + ","
                                    + to.getCityAndCountry() + "," + to.getGPS().toString();
                            if (!routeMap.containsKey(route)) {
                                routeMap.put(route, 0);
                            }
                            routeMap.put(route, routeMap.get(route) + 1);

                            System.out.println(String.format(
                                    "%d:%d From: %s To: %s Distance: %.2fkm Duration: %.2fh Speed: %.2fkmph",
                                    ++count,
                                    xdrHttp.getImsi(),
                                    from.getCityAndCountry(),
                                    to.getCityAndCountry() ,
                                    travel_distance ,
                                    hour ,
                                    speed));
                        }
                    }
                }
            } else { // location.gps == gps. same as last location
                return true;
            }
            last_location.hop++;
            last_location.gps = gps;
            last_location.time = xdrHttp.getDate();
//            writer.write(imsi + "," + xdrHttp.getFormattedDateTime() + "," + xdrHttp.getHomeMcc() + "," + xdrHttp.getHomeMnc() + "," +
//                    xdrHttp.getMcc() + "," + xdrHttp.getMnc() + "," +
//                    cellTower.getArea() + "," + cellTower.getCell() + "," + last_location + "," + travel_distance + "\n", false);
        }
        return true;
    }

    @Override
    public void writeOut(boolean print_to_screan) {
  /*    System.out.println("found 2G 3G:" + found_2G3G);
        System.out.println("not found 2G 3G:" + not_found_2G3G);
        System.out.println("found 4G:" + found_4G);
        System.out.println("not found 4G:" + not_found_4G);
        System.out.println("no_base_station :" + no_base_station); */
    }
}

