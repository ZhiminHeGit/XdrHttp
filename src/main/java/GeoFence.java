
import java.util.HashSet;
import java.util.Set;

// java -cp XdrHttp-1.0-SNAPSHOT.jar ResponseCodeDaily
public class GeoFence extends DailyProcess {

    ImsiToNumber imsiToNumber;
    OpenCellId openCellId;
    Set<Long> outputSet = new HashSet();

    LocationRule locationRule;

    GeoFence(String imsiToNumberMapFile) {
        imsiToNumber = new ImsiToNumber(imsiToNumberMapFile);
    }

    static public void main(String[] args) {
        String input = "C:/xdr_http/hk_data_small.csv";
        String openCellFile = "C:/xdr_http/cell_towers_select.csv";
        String mappingFile = "c:/xdr_http/number_imsi.csv";
        String output = "C:/xdr_http/hk_visitors.csv";


        double lat = 22.272861;
        double lon = 114.182056;
        double radius = 5.0;

        if (args.length >= 7) {
             System.out.println(args[0]);
             lat = Double.parseDouble(args[0]);
            System.out.println(args[1]);
             lon = Double.parseDouble(args[1]);
            System.out.println(args[2]);
             radius = Double.parseDouble(args[2]);
            System.out.println(args[3]);
             input = args[3];
            System.out.println(args[4]);
             openCellFile = args[4];
            System.out.println(args[5]);
             mappingFile = args[5];
            System.out.println(args[6]);
             output = args[6];
        }

        GeoFence geoFence = new GeoFence(mappingFile);
        geoFence.headLine = "number,imsi,date,mcc,mnc,rule\n";
        geoFence.mcc = HONGKONG;
        geoFence.locationRule = new LocationRule(lat, lon, radius);
        geoFence.openCellId = new OpenCellId(openCellFile);

        System.out.println("Processing MCC: " + HONGKONG);
        // String input = "C:/sccp/20150701_233001_AllSTPxDR_ixp0001-1f.csv";

        geoFence.process(input, output, args);
    }

    @Override
    public boolean process(XdrHttp xdrHttp) {
        long imsi = xdrHttp.getImsi();
        if (outputSet.contains(imsi)) {
            return true;
        }
        CellTower cellTower = new CellTower(xdrHttp);
        GPS gps = openCellId.lookup(cellTower);
        if (gps != null) {
                double distance = GPS.getTravelDistance(locationRule.getGps(), gps);
                if (distance < locationRule.getRadius()) {
                    if (lookupNumber(xdrHttp, imsi)) return true;
                }
        }
        return true;
    }

    private boolean lookupNumber(XdrHttp xdrHttp, long imsi) {
        Long number = imsiToNumber.lookup(imsi);
        if (number != null) {
            writer.write(number + "," +
                    xdrHttp.getImsi() + "," + xdrHttp.getReadableDate() + "," +
                    xdrHttp.getMcc() + "," + xdrHttp.getMnc() + "," + locationRule.getName() + "\n", false);
            outputSet.add(imsi);
            return true;
        }
        return false;
    }
}
