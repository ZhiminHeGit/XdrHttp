import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.util.Bytes;
import org.jsoup.*;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.*;
import java.util.*;

public class CountXdr {
    Connection connection = null;
    Table table = null;
    Airports airports = new Airports();
    Map<String, Integer> routeMap = new HashMap();
    int flightCount = 0;
    AppRules appRules = new AppRules();
    String TOTAL_USER = "TOTAL_USER";
    String WORLD = "总计";
    String MAINLAND = "大陆";
    String OVERSEA = "海外";
    int totalUser;

    Map<String, Map<String, Integer>> regionalAppMap = new LinkedHashMap();
    ProvinceLookup provinceLookup;
    MCCLookup mccLookup;
    PhoneTypeLookup phoneTypeLookup;
    BufferedWriter bufferedWriter;

    public byte[] c(String column) {
        return Bytes.toBytes(column);
    }

    public CountXdr() {
        Configuration config = HBaseConfiguration.create();
        config.clear();

        config.set("hbase.zookeeper.quorum", "localhost");
        config.set("hbase.zookeeper.property.clientPort", "2181");
        config.set("hbase.master", "localhost:60000");

        try {
            connection = ConnectionFactory.createConnection(config);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String supportDir = "/Volumes/DataDisk/Data";
        provinceLookup =
                new ProvinceLookup(supportDir + "/cmi_all_province.csv");
        mccLookup =
                new MCCLookup(supportDir + "/MCC.csv");
        phoneTypeLookup =
                new PhoneTypeLookup(supportDir + "/cmi_client_dim.csv");

    }

    static public void main(String[] args) {
        CountXdr hBaseXdr = new CountXdr();
        hBaseXdr.processFromXdrHttp();
    }


    String getPhoneNumber(Result result) {
        String row = Bytes.toString(result.getRow());
        return row.split(":")[0];
    }

    String getRegion(XdrHttp xdrHttp) {
        if (xdrHttp.getHomeMcc() == 460) {
            return provinceLookup.lookupByMsisdn(xdrHttp.getMsisdn());
        } else {
            return mccLookup.lookup(xdrHttp.getHomeMcc());
        }
    }


    void processFromXdrHttp() {
        String[] dateList = {"20161001", "20161002", "20161003", "20161004"};
        try {
            for (String date : dateList) {
                if (table != null) {
                    table.close();
                }
                table = connection.getTable(TableName.valueOf("xdr_http_" + date));
                regionalAppMap.clear();

                Scan scan = new Scan();
                byte[] columnFamily = Bytes.toBytes("data");
                byte[] column = Bytes.toBytes("raw");
                List<XdrHttp> xdrHttpList = new ArrayList();
                totalUser = 0;
                regionalAppMap.put(WORLD, new HashMap());
                regionalAppMap.put(MAINLAND, new HashMap());
                regionalAppMap.put(OVERSEA, new HashMap());

                ResultScanner scanner = table.getScanner(scan);
                Result result = scanner.next();
                while (result != null) {
                    String phoneNumber = getPhoneNumber(result);
                    XdrHttp curXdrHttp = XdrHttp.parse(Bytes.toString(result.getValue(columnFamily, column)));
                    result = scanner.next();
                    while (result != null && getPhoneNumber(result).equals(phoneNumber)) {
                        xdrHttpList.add(curXdrHttp);
                        curXdrHttp = XdrHttp.parse(Bytes.toString(result.getValue(columnFamily, column)));
                        result = scanner.next();
                    }
                    if (!xdrHttpList.isEmpty()) {
                        totalUser++;
                        if (totalUser % 1000 == 0) {
                            System.out.println(new Date() + ":" + totalUser);
                        }
                        processApp(xdrHttpList);
                        xdrHttpList.clear();
                    }
                }
                generateReport("/Volumes/WDPassport/CMIData/" + date + "AppRank_Xdr.txt");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void print(String s) {
        System.out.print(s);
        try {
            bufferedWriter.write(s);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void println() {
        System.out.println();
        try {
            bufferedWriter.write("\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void printRegion(String region, String app) {
        //  System.out.println("printing:" + region);
        Map<String, Integer> appMap = regionalAppMap.get(region);
        int activeUsers = 0;
        double p = 0;
        if (appMap.containsKey(app)) {
            activeUsers = appMap.get(app);
            p = activeUsers * 100.0 / appMap.get(TOTAL_USER);
        }
        print(String.format("%d,%.02f%%,", activeUsers, p));
    }

    private void processApp(List<XdrHttp> xdrHttpList) {
        String region = getRegion(xdrHttpList.get(0));
        if (!regionalAppMap.containsKey(region)) {
            regionalAppMap.put(region, new HashMap());
        }
        Set<String> appSet = new HashSet();
        for (XdrHttp xdrhttp : xdrHttpList) {
            String url = xdrhttp.getHost() + xdrhttp.getUriData();
            appSet.add(appRules.getCMIApp(url));
        }
        Map<String, Integer> appMap = regionalAppMap.get(region);
        for (String app : appSet) {
            Increment(xdrHttpList.get(0).getHomeMcc() == 460, appMap, app);
        }
        Increment(xdrHttpList.get(0).getHomeMcc() == 460, appMap, TOTAL_USER);
    }


    void Increment(boolean isMainLand, Map<String, Integer> appMap, String app) {
        MapUtil.increment(appMap, app);
        MapUtil.increment(regionalAppMap.get(WORLD), app);
        if (isMainLand) {
            MapUtil.increment(regionalAppMap.get(MAINLAND), app);
        } else {
            MapUtil.increment(regionalAppMap.get(OVERSEA), app);
        }
    }

    void generateReport(String fileName) throws FileNotFoundException {
        System.out.println("total user:" + totalUser);
        bufferedWriter = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(fileName)));

        print(String.format("排名,应用,"));
        for (Map.Entry regionEntry : regionalAppMap.entrySet()) {
            if (shouldPrint(regionEntry)) {
                print(String.format("%s,%s%%,", regionEntry.getKey(), regionEntry.getKey()));
            }
        }
        println();

        Map<String, Integer> sortedAppMapWorld = MapUtil.sortByValueDesc(
                regionalAppMap.get(WORLD));
        int rank = 1;

        for (Map.Entry entry : sortedAppMapWorld.entrySet()) {
            if (rank > 100) break;
            String app = (String) entry.getKey();
            if (!app.equals(TOTAL_USER) && !app.equals(AppRules.NO_MATCH)) {
                print(String.format("%d,%s,", rank, app));
                for (Map.Entry regionEntry : regionalAppMap.entrySet()) {
                    if (shouldPrint(regionEntry)) {
                        printRegion((String) regionEntry.getKey(), app);
                    }
                }
                println();
                rank++;
            }
        }
    }

    private boolean shouldPrint(Map.Entry regionEntry) {
        return ((Map<String, Integer>) regionEntry.getValue()).get(TOTAL_USER) > 100;
    }


    private void processCityVisited(List<XdrHttp> xdrHttpList) {
        String lastCity = "HOME", curCity, curDateTime;
        GPS curGPS, lastGPS = null;
        for (XdrHttp xdrhttp : xdrHttpList) {
            if (xdrhttp.getGps() != null) {
                curGPS = xdrhttp.getGps();
                if (!curGPS.equals(lastGPS)) {
                    curCity = CityLookup.lookupGoogleMap(curGPS);
                    curDateTime = xdrhttp.getFormattedDateTime();
                    if (!curCity.equals(lastCity)) {
                        System.out.println(curDateTime + ": Arrived " + curCity);
                        lastCity = curCity;
                    }
                    lastGPS = curGPS;
                }
            }
        }
    }

    void detectHotspot(List<XdrHttp> xdrHttpList) {
        final String IPHONE = "iphone";
        final String ANDROID = "android";
        final String WINDOWS = "windows";
        for (XdrHttp xdrhttp : xdrHttpList) {
            HashSet<String> phoneTypes = new HashSet<>();
            String userAgent = xdrhttp.getUserAgent();
            if (userAgent.contains(IPHONE)) {
                phoneTypes.add(IPHONE);
            }
            if (userAgent.contains(ANDROID)) {
                phoneTypes.add(ANDROID);
            }
            if (userAgent.contains(WINDOWS)) {
                phoneTypes.add(WINDOWS);
            }
        }

    }

    void processFlightDetection() {
        Scan scan = new Scan();
        byte[] columnFamily = Bytes.toBytes("data");
        byte[] column = Bytes.toBytes("raw");
        int count = 0;
        List<String> routes = new ArrayList();
        try {
            ResultScanner scanner = table.getScanner(scan);
            // get first record
            Result result = scanner.next();
            boolean foundFlight = false;
            while (result != null) {
                if (foundFlight) {
                    for (String s : routes) {
                        System.out.println(s);
                    }
                }
                foundFlight = false;
                routes.clear();
                String phoneNumber = getPhoneNumber(result);
                //  System.out.println("Processing:" + phoneNumber);
                XdrHttp curXdrHttp = XdrHttp.parse(Bytes.toString(result.getValue(columnFamily, column)));
                Airport airport;
          /*      if (curXdrHttp.getGps() != null) {
                    if ( (airport = airports.lookupByMsisdn(curXdrHttp.getGps())) != null) {
                        routes.add(airport.getCityAndCountry() + ":" + curXdrHttp.getFormattedDateTime());
                    };
                } */
                result = scanner.next();
                while (result != null && getPhoneNumber(result).equals(phoneNumber)) {
                    XdrHttp lastXdrHttp = curXdrHttp;
                    curXdrHttp = XdrHttp.parse(Bytes.toString(result.getValue(columnFamily, column)));
           /*         if (curXdrHttp.getGps() != null) {
                        if ( (airport = airports.lookupByMsisdn(curXdrHttp.getGps())) != null) {
                            routes.add(airport.getCityAndCountry() + ":" + curXdrHttp.getFormattedDateTime());
                        };
                    } */
                    if (flightDetect(lastXdrHttp, curXdrHttp)) {
                        foundFlight = true;
                    }
                    result = scanner.next();
                }
                // System.out.println("Processed:" + count);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean flightDetect(XdrHttp lastXdrHttp, XdrHttp curXdrHttp) {
        int MIN_FLY_SPEED = 100;
        int MIN_FLY_DISTANCE = 200;
        if (lastXdrHttp.getGps() == null || curXdrHttp.getGps() == null) return false;
        double travel_distance = GPS.getTravelDistance(lastXdrHttp.getGps(), curXdrHttp.getGps());
        if (travel_distance > MIN_FLY_DISTANCE) {
            long time_diff = curXdrHttp.getDate() - lastXdrHttp.getDate();
            double hour = time_diff / 1000.0 / 3600.0;
            double speed = travel_distance / hour; // km per hour

            if (hour > 1.0 && speed > MIN_FLY_SPEED) {
                Airport from = airports.lookup(lastXdrHttp.getGps());
                Airport to = airports.lookup(curXdrHttp.getGps());
                if (from != null && to != null) {
                    String route = from.getCityAndCountry() + "," + from.getGPS().toString() + ","
                            + to.getCityAndCountry() + "," + to.getGPS().toString();
                    if (!routeMap.containsKey(route)) {
                        routeMap.put(route, 0);
                    }
                    routeMap.put(route, routeMap.get(route) + 1);
                    System.out.println(String.format(
                            "%d,%d,%s,%s,%s,%s,%.2fkm,%.2fh,%.2fkmph",
                            ++flightCount,
                            lastXdrHttp.getMsisdn(),
                            from.getCityAndCountry(), lastXdrHttp.getFormattedDateTime(),
                            to.getCityAndCountry(), curXdrHttp.getFormattedDateTime(),
                            travel_distance,
                            hour,
                            speed));
                    return true;
                }
            }
        }
        return false;
    }


    void getSample() {
        Scan scan = new Scan(Bytes.toBytes("86"));
        byte[] columnFamily = Bytes.toBytes("data");
        byte[] column = Bytes.toBytes("raw");
        int count = 0;
        try {
            ResultScanner scanner = table.getScanner(scan);
            Result result = scanner.next();
            while (result != null) {
                XdrHttp curXdrHttp = XdrHttp.parse(Bytes.toString(result.getValue(columnFamily, column)));
                // System.out.println(curXdrHttp.getMsisdn());
                result = scanner.next();
                if (curXdrHttp.getGps() != null && curXdrHttp.getHost() != null
                        && curXdrHttp.getUriData() != null && curXdrHttp.getUserAgent() != null) {
                    count++;
                    System.out.println(curXdrHttp.getFormattedDateTime() + "," +
                            curXdrHttp.getGps().toString() + "," +
                            curXdrHttp.getHost() + curXdrHttp.getUriData() + "," +
                            curXdrHttp.getUserAgent());
                }
                if (count > 1000) {
                    return;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void processWebsite() {
        Scan scan = new Scan(Bytes.toBytes("86"));
        byte[] columnFamily = Bytes.toBytes("data");
        byte[] column = Bytes.toBytes("raw");
        try {
            ResultScanner scanner = table.getScanner(scan);
            Result result = scanner.next();
            while (result != null) {
                XdrHttp curXdrHttp = XdrHttp.parse(Bytes.toString(result.getValue(columnFamily, column)));
                System.out.println(curXdrHttp.getMsisdn());
                getWebSiteMeta(curXdrHttp.getHost() + curXdrHttp.getUriData());
                getWebSiteMeta(curXdrHttp.getFilteredHost());
                result = scanner.next();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void apkDetection() {
        Scan scan = new Scan(Bytes.toBytes("86"));
        byte[] columnFamily = Bytes.toBytes("data");
        byte[] column = Bytes.toBytes("raw");
        int count = 0;
        try {
            ResultScanner scanner = table.getScanner(scan);
            Result result = scanner.next();
            while (result != null) {
                XdrHttp curXdrHttp = XdrHttp.parse(Bytes.toString(result.getValue(columnFamily, column)));
                if (curXdrHttp != null && curXdrHttp.getUriData() != null && curXdrHttp.getUriData().endsWith(".apk")) {
                    System.out.println(curXdrHttp.getHost() + curXdrHttp.getUriData());
                }
                ;
                result = scanner.next();
                count++;
                if (count % 10000 == 0) {
                    System.out.println(count);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void getWebSiteMeta(String url) {
        Document document = null;
        System.out.println(url);
        try {
            document = Jsoup.connect("https://" + url).followRedirects(true).get();
            System.out.println("https");
        } catch (IOException e) {
            try {
                document = Jsoup.connect("http://" + url).followRedirects(true).get();
                System.out.println("http");
            } catch (IOException e1) {
                // e1.printStackTrace();
                return;
            }
        }
        System.out.println("Title:" + document.title());
        // System.out.println("description" + document.select("meta[name=description]").first().attr("content"));
        Element keywords = null;
        String[] keywordNames = {"keyword", "Keyword", "keywords", "Keywords"};
        for (int i = 0; i < keywordNames.length; i++) {
            keywords = document.select("meta[name=" + keywordNames[i] + "]").first();
            if (keywords != null) {
                break;
            }
        }
        if (keywords != null) {
            System.out.println("Keywords:" + keywords.attr("content"));
        } else {
            System.out.println("No keywords");
        }
    }

}
