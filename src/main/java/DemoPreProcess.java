import java.util.*;

public class DemoPreProcess extends DailyProcess {
    int count = 0;
    int validGPS = 0;
    Map<Integer, Map<GPS, HashSet<Long>>> heatMaps = new HashMap();
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
    }



    @Override
    public boolean process(XdrHttp xdrHttp) {
        count++;
        DemoRecord demoRecord = new DemoRecord();
        demoRecord.setImsi(xdrHttp.getImsi());
        GPS gps = xdrHttp.getGps();
        if (gps == null) {
            return true;
        }
        demoRecord.setGps(gps);
        demoRecord.setServingMcc(xdrHttp.getMcc());
        validGPS++;
        demoRecord.setApp(appRules.getCMIApp(xdrHttp.getHost()));
        if (xdrHttp.getHomeMcc() == 460) {
            demoRecord.setRegion(provinceLookup.lookupByMsisdn(xdrHttp.getMsisdn()));
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
