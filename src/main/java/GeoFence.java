
import java.util.HashSet;
import java.util.Set;

// java -cp XdrHttp-1.0-SNAPSHOT.jar ResponseCodeDaily
public class GeoFence extends DailyProcess {

    Set<Long> outputSet = new HashSet();

    LocationRule locationRule;

    static public void main(String[] args) {
        String input = "/Volumes/DataDisk/CMIRaw/201610010000-HTTP.csv";
        String output = "/Volumes/DataDisk/Data/location_query.csv";

        double lat = 22.272861;
        double lon = 114.182056;
        double radius = 0.1;
        if (args.length >=3) {

            System.out.println(args[0]);
            lat = Double.parseDouble(args[0]);
            System.out.println(args[1]);
            lon = Double.parseDouble(args[1]);
            System.out.println(args[2]);
            radius = Double.parseDouble(args[2]);
        }
        GeoFence geoFence = new GeoFence();
        geoFence.locationRule = new LocationRule(lat, lon, radius);
        geoFence.process(input, output, new String[0]);
    }

    @Override
    public boolean process(XdrHttp xdrHttp) {
        long imsi = xdrHttp.getImsi();
        if (outputSet.contains(imsi)) {
            return true;
        }
        GPS gps = xdrHttp.getGps();
        if (gps != null) {
                double distance = GPS.getTravelDistance(locationRule.getGps(), gps);
                if (distance < locationRule.getRadius()) {
                    writer.write(xdrHttp.getImsi() + "\n", true);
                    outputSet.add(imsi);
                    return true;
                }
        }
        return true;
    }

}
