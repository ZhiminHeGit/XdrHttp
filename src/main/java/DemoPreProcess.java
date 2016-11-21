
import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class DemoPreProcess extends DailyProcess {
    int count = 0;
    int validGPS = 0;
    Map<Integer, Map<GPS, HashSet<Long>>> heatMaps = new HashMap();
    OpenCellId openCellId;
    AppRules appRules = new AppRules();
    ProvinceLookup provinceLookup;
    MCCLookup mccLookup ;
    PhoneTypeLookup phoneTypeLookup ;
    public DemoPreProcess(String supportDir) {
        provinceLookup =
                new ProvinceLookup(supportDir + "/cmi_all_province.csv");
        mccLookup =
                new MCCLookup(supportDir +  "/MCC.csv");
        phoneTypeLookup =
                new PhoneTypeLookup(supportDir + "/cmi_client_dim.csv");
        openCellId = new OpenCellId(supportDir + "/cell_towers_7.csv");
    }

    static public void main(String[] args) throws IOException {
        String supportDir = "/Volumes/DataDisk/Data";
        String dataDir = "/Volumes/DataDisk/GoldenWeek";
        String processedDir = "/Volumes/DataDisk/processed";

        if (args.length != 0 ) {
            supportDir = args[0];
            dataDir = args[1];
            processedDir = args[2];
        }

        DemoPreProcess demoPreProcess = new DemoPreProcess(supportDir);

        for (int date = 1; date <= 7; date++) {
            for (int hour = 0; hour <= 23; hour++) {

                String input = "";
                String output =
                        String.format(processedDir + "/2016100%d%02d.csv",
                                date, hour);
                for (int quarter = 0; quarter <= 45; quarter += 15) {
                    input +=
                    String.format(dataDir + "/2016100%d%02d%02d-HTTP.csv",
                                    date, hour, quarter) + ",";
                }
                demoPreProcess.heatMaps.clear();
                demoPreProcess.process(input, output, new String[0]);
                for (Map.Entry entry : demoPreProcess.heatMaps.entrySet()) {
                    String heatmapfile = String.format(processedDir + "/2016100%d%02d.%d.heatmap", date, hour, entry.getKey());
                    System.out.println("Heatmap: " + heatmapfile);
                    PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(
                            new FileOutputStream(heatmapfile)));
                    for (Map.Entry e : ((Map<GPS, HashSet>) entry.getValue()).entrySet()) {
                        printWriter.println(e.getKey() + "," + ((HashSet) e.getValue()).size());
                    }
                    printWriter.close();
                }

            }
        }
       // converage.close();
    }

    @Override
    public boolean process(XdrHttp xdrHttp) {
        count++;
        DemoRecord demoRecord = new DemoRecord();
        demoRecord.setImsi(xdrHttp.getImsi());
        CellTower cellTower = new CellTower(xdrHttp);
        GPS gps = openCellId.lookup(cellTower);
        if (gps == null) {
            return true;
        }
        demoRecord.setGps(gps);
        validGPS++;
        demoRecord.setApp(appRules.getCMIApp(xdrHttp.getHost()));
        if (xdrHttp.getHomeMcc() == 460) {
            long prefix = xdrHttp.getMsisdn() / 10000 - 860000000;
            demoRecord.setRegion(provinceLookup.lookup(prefix));
        } else {
            demoRecord.setRegion(mccLookup.lookup(xdrHttp.getHomeMcc()));
        }
        // get the first 8 digits of 16 digits imei
        long prefix = xdrHttp.getImei() / 100000000;
        String phoneBrand = phoneTypeLookup.lookup(prefix);
        if (phoneBrand != null) {
            demoRecord.setPhoneBrand(phoneBrand);
        } else {
            demoRecord.setPhoneBrand("NoBrand");
        }
        writer.write(demoRecord + "\n", false);
        Map<GPS, HashSet<Long>> heatmap;
        if (heatMaps.containsKey(xdrHttp.getMcc())) {
            heatmap = heatMaps.get(xdrHttp.getMcc());
        } else {
            heatmap = new HashMap();
            heatMaps.put(xdrHttp.getMcc(), heatmap);
        }
        if (heatmap.containsKey(gps)) {
            heatmap.get(gps).add(demoRecord.getImsi());
        } else {
            heatmap.put(gps, Utils.newHashSet(demoRecord.getImsi()));
        }

        return true;
    }
}
