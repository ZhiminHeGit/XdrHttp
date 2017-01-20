import java.util.*;

/**
 * Created by usrc on 5/25/2016.
 */
public class LocationProcess extends DailyProcess {
    OpenCellId openCellId;

    Map<Long, Location> lastLocationMap = new HashMap();

    public class Location {
        GPS gps;
        int hop;

        public String toString() {
            return gps.toString() + "," + hop;
        }
    }

    int no_base_station = 0;
    int found_2G3G = 0;
    int found_4G = 0;
    int not_found_2G3G = 0;
    int not_found_4G = 0;

    public LocationProcess(int mcc) {
        headLine = "imsi,time, home_mcc, home_mnc, mcc,mnc,area,cell,lon,lat,hop,travel_distance \n";
        openCellId = new OpenCellId("/Volumes/DataDisk/Data/cell_towers_7.csv");
        this.mcc = mcc;
    }

    public static void main(String[] args) {
        int mcc = 454;
        System.out.println("Processing MCC: " + mcc);
        String input = "/Volumes/DataDisk/GoldenWeek/201610010000-HTTP.csv";
        String output = "/Volumes/DataDisk/Data/location" + mcc + ".csv";
        LocationProcess bs2GPSProcess = new LocationProcess(mcc);
        if (input.contains("STP")) {
            bs2GPSProcess.recordType = RecordType.SCCP;
        }
        bs2GPSProcess.process(input, output, args);
    }

    @Override
    public boolean process(XdrHttp xdrHttp) {
        long imsi = xdrHttp.getImsi();

        CellTower cellTower = new CellTower(xdrHttp);
        GPS gps = openCellId.lookup(cellTower);
        if (gps != null) {
            Location last_location = lastLocationMap.get(imsi);
            double travel_distance = 0;
            if (last_location == null) {
                last_location = new Location();
                lastLocationMap.put(imsi, last_location);
            } else if (last_location.gps != gps) {
                travel_distance = GPS.getTravelDistance(gps, last_location.gps);
            } else { // location.gps == gps. same as last location
                return true;
            }
            last_location.hop++;
            last_location.gps = gps;
            writer.write(imsi + "," + xdrHttp.getFormattedDateTime() + "," + xdrHttp.getHomeMcc() + "," + xdrHttp.getHomeMnc() + "," +
                    xdrHttp.getMcc() + "," + xdrHttp.getMnc() + "," +
                    cellTower.getArea() + "," + cellTower.getCell() + "," + last_location + "," + travel_distance + "\n", false);
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

