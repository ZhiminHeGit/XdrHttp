import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class Main {
    // some static HashMaps to store the data
    private static HashMap<Long, Long> IMSI_MSISDN_MAPPINGS, IMSI_COUNTS, IMSI_DURATIONS;
    private static HashMap<String, Long> FQDN_COUNTS, FQDN_TWOTIERS_COUNTS, IMSI_4G_LOCATIONS, IMSI_23G_LOCATIONS;
    private static HashSet<Long> MOST_FREQUENT_USERS;
    private static HashMap<Long, HashMap<Long, String>> FREQUENT_USER_ACTIVITIES;

    private static HashMap<Long, Long> IMSI_4G, IMSI_23G;
    private static HashMap<Long, HashSet<String>> HANDSET_TYPES;
    private static HashMap<Long, MobileUser> MOBILE_USERS;
    private static HashSet<Integer> UNIQUE_HOME_MCCS, UNIQUE_SERVING_MCCS;
    private static HashSet<Roaming> ROAMING_MAPS;

    // some constants
    private static String rootPath = "point to the root directory of the Xdr Http files";   // needs to be updated by user
//    private static String rootPath = "/Users/jianli/Downloads/cmidata/xdr_http/";   // needs to be updated by user
    private static String filePath = "create a file that contains all the file names to be processed under the rootPath defined above"; // needs to be updated by user
//    private static String filePath = "/Users/jianli/Downloads/cmidata/xdr_http/files.txt";
    private static String suffix = ".csv";
    private static String COMMA = ",";
    private static String DOT = "\\.";
    private static final boolean DEBUG = false;

    // regular expression to validate IP address
    private static final String IPADDRESS_PATTERN =
            "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                    "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                    "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                    "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";
    private static Pattern pattern = Pattern.compile(IPADDRESS_PATTERN);

    public static void main(String[] args) {

        // initialization
        IMSI_COUNTS = new HashMap<>();
        IMSI_DURATIONS = new HashMap<>();
        IMSI_MSISDN_MAPPINGS = new HashMap<>();
        FQDN_TWOTIERS_COUNTS = new HashMap<>();
        IMSI_4G = new HashMap<>();
        IMSI_23G = new HashMap<>();
        IMSI_4G_LOCATIONS = new HashMap<>();
        IMSI_23G_LOCATIONS = new HashMap<>();
        long total_4g=0L, total_2g=0L;
        FQDN_COUNTS = new HashMap<>();
        HashSet<Long> imsis = new HashSet<>();
        HANDSET_TYPES = new HashMap<>();
        MOBILE_USERS = new HashMap<>();
        UNIQUE_HOME_MCCS = new HashSet<>();
        UNIQUE_SERVING_MCCS = new HashSet<>();
        ROAMING_MAPS = new HashSet<>();

        // this is kind of additional after the first run to get the most frequent users
        // the IMSIs below represent the ones that consumes the most durations in connection
        // or with the most number of entries in the Xdr Http records
        MOST_FREQUENT_USERS = new HashSet<>();
//        MOST_FREQUENT_USERS.add(8672710287647400L);
//        MOST_FREQUENT_USERS.add(3553420655276839L);
//        MOST_FREQUENT_USERS.add(3580280597854708L);
//        MOST_FREQUENT_USERS.add(3537400606063004L);
//        MOST_FREQUENT_USERS.add(3580220506491403L);
//        MOST_FREQUENT_USERS.add(3551010555087764L);
//        MOST_FREQUENT_USERS.add(3589040521883002L);
//        MOST_FREQUENT_USERS.add(3517750703811506L);
//        MOST_FREQUENT_USERS.add(8698050221511778L);
//        MOST_FREQUENT_USERS.add(3591490513963501L);
        FREQUENT_USER_ACTIVITIES = new HashMap<>();

        System.out.println(new Date() + " start processing");

        try {
            // the below file has all the file names to be processed
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            String line = br.readLine();
            while (line != null) {
                // process each comma separated csv files
                BufferedReader br2 = new BufferedReader(new FileReader(rootPath + line + suffix));
                String line2 = br2.readLine();
                while(line2 != null){
                    XdrHttp xdrHttp = parseXdrHttp(line2);
                    if(xdrHttp.getImsi()!=0 && xdrHttp.getMsisdn() != 0) {
                        // IMSI is the primary key for most of the HashMaps
                        Long imsi = xdrHttp.getImsi();

                        // keep a HashSet of IMSIs for quick lookup in 2/3/4G locations
                        imsis.add(imsi);

                        MobileUser mobileUser = MOBILE_USERS.containsKey(imsi) ? MOBILE_USERS.get(imsi) : new MobileUser();
                        //TODO: need more granularity for parsing the user agent string
                        if(xdrHttp.getUserAgent() != null){
                            if(HANDSET_TYPES.containsKey(imsi)){
                                HashSet<String> types = HANDSET_TYPES.get(imsi);
                                types.add(xdrHttp.getUserAgent());
                                HANDSET_TYPES.put(imsi, types);
                            } else {
                                HashSet<String> types = new HashSet<>();
                                types.add(xdrHttp.getUserAgent());
                                HANDSET_TYPES.put(imsi, types);
                            }

                            //TODO: handset information to be added to the user record
                        }

                        // IMSI to MSISDN mapping
                        if (!IMSI_MSISDN_MAPPINGS.containsKey(imsi))
                            IMSI_MSISDN_MAPPINGS.put(imsi, xdrHttp.getMsisdn());

                        // the number of occurrences in the xdr of each IMSI
                        if (!IMSI_COUNTS.containsKey(imsi))
                            IMSI_COUNTS.put(imsi, 1L);
                        else
                            IMSI_COUNTS.put(imsi, IMSI_COUNTS.get(imsi) + 1);

                        // process mcc/mnc/tac
                        if(mobileUser.getHomeMcc() == 0)
                            mobileUser.setHomeMcc(xdrHttp.getHomeMcc());

                        if(mobileUser.getHomeMnc() == 0)
                            mobileUser.setHomeMnc(xdrHttp.getHomeMnc());

                        if(mobileUser.getTac() == 0)
                            mobileUser.setTac(xdrHttp.getTac());

                        UNIQUE_SERVING_MCCS.add(xdrHttp.getServingMcc());
                        UNIQUE_HOME_MCCS.add(xdrHttp.getHomeMcc());
                        ROAMING_MAPS.add(new Roaming(xdrHttp.getHomeMcc(), xdrHttp.getServingMcc()));
                        // footprint
                        HashMap<Integer, FootPrint> footPrintHashMap = mobileUser.getFootPrintHashMap();
                        if(footPrintHashMap.containsKey(xdrHttp.getServingMcc())){
                            // the serving MCC already recorded
                            FootPrint footPrint = footPrintHashMap.get(xdrHttp.getServingMcc());
                            if(footPrint.getEnterDate() > xdrHttp.getDate()) {
                                footPrint.setEnterDate(xdrHttp.getDate());
                            }
                            if(footPrint.getExitDate() < xdrHttp.getDate()){
                                footPrint.setExitDate(xdrHttp.getDate());
                            }
                            footPrintHashMap.put(xdrHttp.getServingMcc(), footPrint);
                        } else {
                            // the serving MCC is not there yet, this means that user enters a new roaming destination
                            FootPrint footPrint = new FootPrint();
                            footPrint.setExitDate(xdrHttp.getDate());
                            footPrint.setEnterDate(xdrHttp.getDate());
                            footPrint.setServingMcc(xdrHttp.getServingMcc());
                            footPrintHashMap.put(xdrHttp.getServingMcc(), footPrint);
                        }

                        mobileUser.setFootPrintHashMap(footPrintHashMap);

                        // process the hosts
                        if (xdrHttp.getHost() != null) {
                            String host = xdrHttp.getHost();
                            String shortHost = consolidateFQDN(host);

                            // list in 2 ways: one is the exact FQDN from the hosts field
                            // and the other one is the consolidated domains, for example,
                            // xxx.qq.com and yyy.qq.com are consolidated as qq.com
                            if (!FQDN_COUNTS.containsKey(host))
                                FQDN_COUNTS.put(host, 1L);
                            else
                                FQDN_COUNTS.put(host, FQDN_COUNTS.get(host) + 1);

                            if (!FQDN_TWOTIERS_COUNTS.containsKey(shortHost))
                                FQDN_TWOTIERS_COUNTS.put(shortHost, 1L);
                            else
                                FQDN_TWOTIERS_COUNTS.put(shortHost, FQDN_TWOTIERS_COUNTS.get(shortHost) + 1);
                            if (MOST_FREQUENT_USERS.contains(imsi)) {
                                if (xdrHttp.getDate() != 0L && xdrHttp.getHost() != null) {
                                    if (FREQUENT_USER_ACTIVITIES.containsKey(imsi)) {
                                        HashMap<Long, String> contents = FREQUENT_USER_ACTIVITIES.get(imsi);
                                        contents.put(xdrHttp.getDate(), shortHost);
                                        FREQUENT_USER_ACTIVITIES.put(imsi, contents);
                                    } else {
                                        HashMap<Long, String> contents = new HashMap<>();
                                        contents.put(xdrHttp.getDate(), shortHost);
                                        FREQUENT_USER_ACTIVITIES.put(imsi, contents);
                                    }
                                }
                            }

                            HashMap<String, Integer> visitedSites = mobileUser.getVisitedSites();
                            if(visitedSites.containsKey(shortHost)){
                                visitedSites.put(shortHost, visitedSites.get(shortHost) + 1);
                            } else {
                                visitedSites.put(shortHost, 1);
                            }
                            mobileUser.setVisitedSites(visitedSites);
                            if (xdrHttp.getDuration() != 0) {
                                HashMap<String, Integer> browseDurations = mobileUser.getBrowseDurations();
                                if (browseDurations.containsKey(shortHost)) {
                                    browseDurations.put(shortHost, browseDurations.get(shortHost) + xdrHttp.getDuration());
                                } else {
                                    browseDurations.put(shortHost, xdrHttp.getDuration());
                                }
                                mobileUser.setBrowseDurations(browseDurations);
                            }
                        }

                        // process the durations based on the 2/3/4G locations when it happened
                        if (xdrHttp.getDuration() != 0) {
                            if (xdrHttp.getTai() != 0 && xdrHttp.getEcgi() != 0) {
                                // 4G
                                if (IMSI_4G.containsKey(imsi)){
                                    IMSI_4G.put(imsi, IMSI_4G.get(imsi) + xdrHttp.getDuration());
                                } else {
                                    IMSI_4G.put(imsi, ((long)xdrHttp.getDuration()));
                                }

                                total_4g += ((long)xdrHttp.getDuration());
                                String location4g = xdrHttp.getTai() + "," + xdrHttp.getEcgi();
                                if (IMSI_4G_LOCATIONS.containsKey(location4g)){
                                    IMSI_4G_LOCATIONS.put(location4g, IMSI_4G_LOCATIONS.get(location4g) + xdrHttp.getDuration());
                                } else {
                                    IMSI_4G_LOCATIONS.put(location4g, ((long)xdrHttp.getDuration()));
                                }
                            }
                            if(xdrHttp.getCellCI()!= 0 && xdrHttp.getCellLAC() != 0){
                                if (IMSI_23G.containsKey(imsi)){
                                    IMSI_23G.put(imsi, IMSI_23G.get(imsi) + xdrHttp.getDuration());
                                } else {
                                    IMSI_23G.put(imsi, ((long)xdrHttp.getDuration()));
                                }

                                total_2g += ((long)xdrHttp.getDuration());
                                String location23g = xdrHttp.getCellCI() + "," + xdrHttp.getCellLAC();
                                if (IMSI_23G_LOCATIONS.containsKey(location23g)){
                                    IMSI_23G_LOCATIONS.put(location23g, IMSI_23G_LOCATIONS.get(location23g) + xdrHttp.getDuration());
                                } else {
                                    IMSI_23G_LOCATIONS.put(location23g, ((long)xdrHttp.getDuration()));
                                }
                            }

                            // the durations in the xdr of each IMSI
                            if (!IMSI_DURATIONS.containsKey(imsi))
                                IMSI_DURATIONS.put(imsi, (long)xdrHttp.getDuration());
                            else
                                IMSI_DURATIONS.put(imsi, IMSI_DURATIONS.get(imsi) + xdrHttp.getDuration());
                        }

                        mobileUser.setLastTripDate(xdrHttp.getDate());
                        mobileUser.setLastTripMcc(xdrHttp.getServingMcc());
                        MOBILE_USERS.put(imsi, mobileUser);
                    }
                    line2 = br2.readLine();
                }
                br2.close();
                line = br.readLine();
            }
            br.close();
        }catch (Exception ex){
            ex.printStackTrace();
        }

        System.out.println(new Date() + " completed processing");

        if(DEBUG) {
            System.out.println("total time under 2/3G: " + total_2g + "ms");
            System.out.println("total time under 4G: " + total_4g + "ms");
        }

        try{
            File file = new File(rootPath + "imsi-4g.csv");
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            for (Long imsi : imsis) {
                long time2g = IMSI_23G.containsKey(imsi) ? IMSI_23G.get(imsi) : 0;
                long time4g = IMSI_4G.containsKey(imsi) ? IMSI_4G.get(imsi) : 0;
                long total = time2g + time4g;
                bw.write(imsi + "," + time2g + "," + time4g + "," + total + "\n");
            }
            bw.close();
        }catch (Exception ex){
            ex.printStackTrace();
        }

        try{
            File file = new File(rootPath + "homeMccs.csv");
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            for (Integer mcc : UNIQUE_HOME_MCCS) {
                bw.write(mcc + "\n");
            }
            bw.close();
        }catch (Exception ex){
            ex.printStackTrace();
        }

        try{
            File file = new File(rootPath + "servingMccs.csv");
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            for (Integer mcc : UNIQUE_SERVING_MCCS) {
                bw.write(mcc + "\n");
            }
            bw.close();
        }catch (Exception ex){
            ex.printStackTrace();
        }

        try{
            File file = new File(rootPath + "RoamingMaps.csv");
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            for (Roaming roaming : ROAMING_MAPS) {
                bw.write(roaming.getHomeMcc() + COMMA + roaming.getServingMcc() + "\n");
            }
            bw.close();
        }catch (Exception ex){
            ex.printStackTrace();
        }

        if(DEBUG) {
            for (Long imei : IMSI_MSISDN_MAPPINGS.keySet()) {
                System.out.println("IMSI: " + imei + " maps to MSISDN: " + IMSI_MSISDN_MAPPINGS.get(imei));
            }

            for (Long imei : IMSI_COUNTS.keySet()) {
                System.out.println("IMSI: " + imei + " appears: " + IMSI_COUNTS.get(imei) + " times");
            }

            for (String host : FQDN_COUNTS.keySet()) {
                System.out.println("host: " + host + " appears: " + FQDN_COUNTS.get(host) + " times");
            }

            for (String host : FQDN_TWOTIERS_COUNTS.keySet()) {
                System.out.println("host: " + host + " appears: " + FQDN_TWOTIERS_COUNTS.get(host) + " times");
            }
        }

        // write the various results to files
//        writeToFileInValueOrder("consolidate.csv", FQDN_TWOTIERS_COUNTS);
//        writeToFileInValueOrder("fqdn.csv", FQDN_COUNTS);
        writeToFileInValueOrder("imsi.csv", IMSI_COUNTS);
        writeToFileInValueOrder("imsi-durations.csv", IMSI_DURATIONS);
        writeToFileInValueOrder("imsi-mapping.csv", IMSI_MSISDN_MAPPINGS);

        for(Long imei : FREQUENT_USER_ACTIVITIES.keySet()){
            writeToFileInValueOrder(imei + ".csv", FREQUENT_USER_ACTIVITIES.get(imei));
        }
        writeToFileInValueOrder("imsi-4g-locations.csv", IMSI_4G_LOCATIONS);
        writeToFileInValueOrder("imsi-23g-locations.csv", IMSI_23G_LOCATIONS);

        System.out.println(new Date() + " starting write results for each mobile user");
        for(Map.Entry<Long, MobileUser> entry : MOBILE_USERS.entrySet()){
            try{
                File file = new File(rootPath + "imsis/" + entry.getKey() + ".csv");
                if (!file.exists()) {
                    file.createNewFile();
                }
                FileWriter fw = new FileWriter(file.getAbsoluteFile());
                BufferedWriter bw = new BufferedWriter(fw);
                MobileUser mobileUser = entry.getValue();
                bw.write("home MCC:" + mobileUser.getHomeMcc() + "\n");
                bw.write("home MNC:" + mobileUser.getHomeMnc() + "\n");
                bw.write("last trip date:" + mobileUser.getLastTripDate() + "\n");
                bw.write("last trip serving MCC:" + mobileUser.getLastTripMcc() + "\n");
                bw.write("Footprints: \n" );
                HashMap<FootPrint, Long> footPrintLongHashMap = new HashMap<>();
                for(Map.Entry<Integer, FootPrint> footPrintEntry : mobileUser.getFootPrintHashMap().entrySet()){
                    footPrintLongHashMap.put(footPrintEntry.getValue(), footPrintEntry.getValue().getEnterDate());
                }
                footPrintLongHashMap = sortByValue(footPrintLongHashMap);
                for(FootPrint footPrint : footPrintLongHashMap.keySet()){
                    bw.write("Roaming MCC: " + footPrint.getServingMcc() + ",");
                    bw.write("Entering: " + new Date(footPrint.getEnterDate()) + ",");
                    bw.write("Exiting: " + new Date(footPrint.getExitDate()) + "\n");
                }
                bw.write("Most visited sites by counts:\n");
                HashMap<String, Integer> myMap = sortByValue(mobileUser.getVisitedSites());
                for(Map.Entry<String, Integer> mapEntry : myMap.entrySet()){
                    bw.write(mapEntry.getKey() + COMMA + mapEntry.getValue() + "\n");
                }
                bw.write("Most visited sites by browsing durations:\n");
                myMap = sortByValue(mobileUser.getBrowseDurations());
                for(Map.Entry<String, Integer> mapEntry : myMap.entrySet()){
                    bw.write(mapEntry.getKey() + COMMA + mapEntry.getValue() + "\n");
                }
                bw.close();
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }

        System.out.println(new Date() + " completed writing mobile user data");

//        try {
//            File file = new File(rootPath + "handsets.csv");
//            if (!file.exists()) {
//                file.createNewFile();
//            }
//            FileWriter fw = new FileWriter(file.getAbsoluteFile());
//            BufferedWriter bw = new BufferedWriter(fw);
//            for (Long imei : HANDSET_TYPES.keySet()) {
//                StringBuilder sb = new StringBuilder();
//                sb.append(imei);
//                sb.append(COMMA);
//                HashSet<String> types = HANDSET_TYPES.get(imei);
//                for (String type : types) {
//                    sb.append(type);
//                    sb.append(";");
//                }
//                sb.append("\n");
//                bw.write(sb.toString());
//            }
//            bw.close();
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
    }

    // write the content of HashMap in the order by values to a file specified by filename
    private static <K, V extends Comparable<? super V>> void writeToFileInValueOrder(String filename, HashMap<K, V> hashMap){
        hashMap = sortByValue(hashMap);
        try {
            File file = new File(rootPath + filename);

            // if file doesnt exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }

            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            for(HashMap.Entry<K, V> entry : hashMap.entrySet()){
                bw.write(entry.getKey() + "," + entry.getValue() + "\n");
            }
            bw.close();
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    // parse the XdrHttp record from each line of the .csv files
    // TODO: to take all fields into account
    private static XdrHttp parseXdrHttp(String line){
        if(line == null || line.isEmpty())
            return null;

        String[] parts = line.split(COMMA);
        XdrHttp xdrHttp = new XdrHttp();
        if(notEmpty(parts[1])){
            String[] parts2 = parts[1].split(DOT);
            xdrHttp.setDate(Long.parseLong(parts2[0]));
        }
        if(notEmpty(parts[2])){
            xdrHttp.setDuration(Integer.parseInt(parts[2]));
        }

        if(notEmpty(parts[3]))
            xdrHttp.setImsi(Long.parseLong(parts[3]));
        if(notEmpty(parts[4]))
            xdrHttp.setMsisdn(Long.parseLong(parts[4]));
        if(notEmpty(parts[5]))
            xdrHttp.setImei(Long.parseLong(parts[5]));

        if(notEmpty(parts[6])){
            xdrHttp.setHomeMcc(Integer.parseInt(parts[6]));
        }
        if(notEmpty(parts[7])){
            xdrHttp.setHomeMnc(Integer.parseInt(parts[7]));
        }
        if(notEmpty(parts[8])){
            xdrHttp.setTac(Integer.parseInt(parts[8]));
        }
        if(notEmpty(parts[9])){
            xdrHttp.setServingMcc(Integer.parseInt(parts[9]));
        }
        if(notEmpty(parts[10])){
            xdrHttp.setServingMnc(Integer.parseInt(parts[10]));
        }
        if(notEmpty(parts[13]))
            xdrHttp.setTai(Integer.parseInt(parts[13]));
        if(notEmpty(parts[14]))
            xdrHttp.setEcgi(Integer.parseInt(parts[14]));
        if(notEmpty(parts[15]))
            xdrHttp.setCellLAC(Integer.parseInt(parts[15]));
        if(notEmpty(parts[16]))
            xdrHttp.setCellCI(Integer.parseInt(parts[16]));

        if(notEmpty(parts[23]))
            xdrHttp.setHost(parts[23].toLowerCase());

        if(notEmpty(parts[27]))
            xdrHttp.setUserAgent(parts[27].toLowerCase());
        return xdrHttp;
    }

    private static boolean notEmpty(String string){
        return string != null && !string.isEmpty();
    }

    // utility to reach to the root level of a FQDN
    private static String consolidateFQDN(String fqdn){
        if(fqdn == null || fqdn.isEmpty())
            return null;

        if(isIPAddress(fqdn))
            return fqdn;

        String[] parts = fqdn.split(DOT);
        if (parts.length <= 2)
            return fqdn;
        else if(parts[parts.length-2].equals("com") || parts[parts.length-2].equals("net") || parts[parts.length-2].equals("org") || parts[parts.length-2].equals("gov"))
            return parts[parts.length-3] + "." + parts[parts.length-2] + "." + parts[parts.length-1];
        else
            return parts[parts.length-2] + "." + parts[parts.length-1];
    }

    private static boolean isIPAddress(final String ip){
        Matcher matcher = pattern.matcher(ip);
        return matcher.matches();
    }

    private static <K, V extends Comparable<? super V>> HashMap<K, V>  sortByValue( HashMap<K, V> map )
    {
        HashMap<K, V> result = new LinkedHashMap<>();
        Stream<Map.Entry<K, V>> st = map.entrySet().stream();

        st.sorted( Map.Entry.comparingByValue() )
                .forEachOrdered( e -> result.put(e.getKey(), e.getValue()) );

        return result;
    }
}
