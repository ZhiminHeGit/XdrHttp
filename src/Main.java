import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class Main {
    // some static HashMaps to store the data
    private static HashMap<Long, Long> IMEI_MSISDN_MAPPINGS, IMEI_COUNTS, IMEI_DURATIONS;
    private static HashMap<String, Long> FQDN_COUNTS, FQDN_TWOTIERS_COUNTS, IMEI_4G_LOCATIONS, IMEI_23G_LOCATIONS;
    private static HashSet<Long> MOST_FREQUENT_USERS;
    private static HashMap<Long, HashMap<Long, String>> FREQUENT_USER_ACTIVITIES;

    private static HashMap<Long, Long> IMEI_4G, IMEI_23G;
    private static HashMap<Long, HashSet<String>> HANDSET_TYPES;

    // some constants
//    private static String rootPath = "point to the root directory of the Xdr Http files";   // needs to be updated by user
    private static String rootPath = "/Users/jianli/Downloads/cmidata/xdr_http/";   // needs to be updated by user
    private static String filePath = "/Users/jianli/Downloads/cmidata/xdr_http/files.txt"; // needs to be updated by user
//    private static String filePath = "create a file that contains all the file names to be processed under the rootPath defined above"; // needs to be updated by user
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
        IMEI_COUNTS = new HashMap<>();
        IMEI_DURATIONS = new HashMap<>();
        IMEI_MSISDN_MAPPINGS = new HashMap<>();
        FQDN_TWOTIERS_COUNTS = new HashMap<>();
        IMEI_4G = new HashMap<>();
        IMEI_23G = new HashMap<>();
        IMEI_4G_LOCATIONS = new HashMap<>();
        IMEI_23G_LOCATIONS = new HashMap<>();
        long total_4g=0L, total_2g=0L;
        FQDN_COUNTS = new HashMap<>();
        HashSet<Long> imeis = new HashSet<>();
        HANDSET_TYPES = new HashMap<>();

        // this is kind of additional after the first run to get the most frequent users
        // the IMEIs below represent the ones that consumes the most durations in connection
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
                    if(xdrHttp.getImei()!=0 && xdrHttp.getMsisdn() != 0) {
                        // IMEI is the primary key for most of the HashMaps
                        Long imei = xdrHttp.getImei();

                        // keep a HashSet of IMEIs for quick lookup in 2/3/4G locations
                        imeis.add(imei);

                        //TODO: need more granularity for parsing the user agent string
                        if(xdrHttp.getUserAgent() != null){
                            if(HANDSET_TYPES.containsKey(imei)){
                                HashSet<String> types = HANDSET_TYPES.get(imei);
                                types.add(xdrHttp.getUserAgent());
                                HANDSET_TYPES.put(imei, types);
                            } else {
                                HashSet<String> types = new HashSet<>();
                                types.add(xdrHttp.getUserAgent());
                                HANDSET_TYPES.put(imei, types);
                            }
                        }

                        // IMEI to MSISDN mapping
                        if (!IMEI_MSISDN_MAPPINGS.containsKey(imei))
                            IMEI_MSISDN_MAPPINGS.put(imei, xdrHttp.getMsisdn());

                        // the number of occurrences in the xdr of each IMEI
                        if (!IMEI_COUNTS.containsKey(imei))
                            IMEI_COUNTS.put(imei, 1L);
                        else
                            IMEI_COUNTS.put(imei, IMEI_COUNTS.get(imei) + 1);

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
                            if (MOST_FREQUENT_USERS.contains(imei)) {
                                if (xdrHttp.getDate() != 0L && xdrHttp.getHost() != null) {
                                    if (FREQUENT_USER_ACTIVITIES.containsKey(imei)) {
                                        HashMap<Long, String> contents = FREQUENT_USER_ACTIVITIES.get(imei);
                                        contents.put(xdrHttp.getDate(), shortHost);
                                        FREQUENT_USER_ACTIVITIES.put(imei, contents);
                                    } else {
                                        HashMap<Long, String> contents = new HashMap<>();
                                        contents.put(xdrHttp.getDate(), shortHost);
                                        FREQUENT_USER_ACTIVITIES.put(imei, contents);
                                    }
                                }
                            }
                        }

                        // process the durations based on the 2/3/4G locations when it happened
                        if (xdrHttp.getDuration() != 0) {
                            if (xdrHttp.getTai() != 0 && xdrHttp.getEcgi() != 0) {
                                // 4G
                                if (IMEI_4G.containsKey(imei)){
                                    IMEI_4G.put(imei, IMEI_4G.get(imei) + xdrHttp.getDuration());
                                } else {
                                    IMEI_4G.put(imei, ((long)xdrHttp.getDuration()));
                                }

                                total_4g += ((long)xdrHttp.getDuration());
                                String location4g = xdrHttp.getTai() + "," + xdrHttp.getEcgi();
                                if (IMEI_4G_LOCATIONS.containsKey(location4g)){
                                    IMEI_4G_LOCATIONS.put(location4g, IMEI_4G_LOCATIONS.get(location4g) + xdrHttp.getDuration());
                                } else {
                                    IMEI_4G_LOCATIONS.put(location4g, ((long)xdrHttp.getDuration()));
                                }
                            }
                            if(xdrHttp.getCellCI()!= 0 && xdrHttp.getCellLAC() != 0){
                                if (IMEI_23G.containsKey(imei)){
                                    IMEI_23G.put(imei, IMEI_23G.get(imei) + xdrHttp.getDuration());
                                } else {
                                    IMEI_23G.put(imei, ((long)xdrHttp.getDuration()));
                                }

                                total_2g += ((long)xdrHttp.getDuration());
                                String location23g = xdrHttp.getCellCI() + "," + xdrHttp.getCellLAC();
                                if (IMEI_23G_LOCATIONS.containsKey(location23g)){
                                    IMEI_23G_LOCATIONS.put(location23g, IMEI_23G_LOCATIONS.get(location23g) + xdrHttp.getDuration());
                                } else {
                                    IMEI_23G_LOCATIONS.put(location23g, ((long)xdrHttp.getDuration()));
                                }
                            }

                            // the durations in the xdr of each IMEI
                            if (!IMEI_DURATIONS.containsKey(imei))
                                IMEI_DURATIONS.put(imei, (long)xdrHttp.getDuration());
                            else
                                IMEI_DURATIONS.put(imei, IMEI_DURATIONS.get(imei) + xdrHttp.getDuration());
                        }
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

//        try{
//            File file = new File(rootPath + "4g.csv");
//            if (!file.exists()) {
//                file.createNewFile();
//            }
//            FileWriter fw = new FileWriter(file.getAbsoluteFile());
//            BufferedWriter bw = new BufferedWriter(fw);
//            for (Long imei : imeis) {
//                long time2g = IMEI_23G.containsKey(imei) ? IMEI_23G.get(imei) : 0;
//                long time4g = IMEI_4G.containsKey(imei) ? IMEI_4G.get(imei) : 0;
//                long total = time2g + time4g;
//                bw.write(imei + "," + time2g + "," + time4g + "," + total + "\n");
//            }
//            bw.close();
//        }catch (Exception ex){
//            ex.printStackTrace();
//        }

        if(DEBUG) {
            for (Long imei : IMEI_MSISDN_MAPPINGS.keySet()) {
                System.out.println("IMEI: " + imei + " maps to MSISDN: " + IMEI_MSISDN_MAPPINGS.get(imei));
            }

            for (Long imei : IMEI_COUNTS.keySet()) {
                System.out.println("IMEI: " + imei + " appears: " + IMEI_COUNTS.get(imei) + " times");
            }

            for (String host : FQDN_COUNTS.keySet()) {
                System.out.println("host: " + host + " appears: " + FQDN_COUNTS.get(host) + " times");
            }

            for (String host : FQDN_TWOTIERS_COUNTS.keySet()) {
                System.out.println("host: " + host + " appears: " + FQDN_TWOTIERS_COUNTS.get(host) + " times");
            }
        }

        // write the various results to files
        writeToFileInValueOrder("consolidate.csv", FQDN_TWOTIERS_COUNTS);
        writeToFileInValueOrder("fqdn.csv", FQDN_COUNTS);
        writeToFileInValueOrder("imei.csv", IMEI_COUNTS);
        writeToFileInValueOrder("imei-durations.csv", IMEI_DURATIONS);
        writeToFileInValueOrder("imei-mapping.csv", IMEI_MSISDN_MAPPINGS);

//        for(Long imei : FREQUENT_USER_ACTIVITIES.keySet()){
//            writeToFileInValueOrder(imei + ".csv", FREQUENT_USER_ACTIVITIES.get(imei));
//        }
        writeToFileInValueOrder("4g-locations.csv", IMEI_4G_LOCATIONS);
        writeToFileInValueOrder("23g-locations.csv", IMEI_23G_LOCATIONS);

//        try{
//            File file = new File(rootPath + "handsets.csv");
//            if (!file.exists()) {
//                file.createNewFile();
//            }
//            FileWriter fw = new FileWriter(file.getAbsoluteFile());
//            BufferedWriter bw = new BufferedWriter(fw);
//        for(Long imei : HANDSET_TYPES.keySet()){
//            StringBuilder sb = new StringBuilder();
//            sb.append(imei);
//            sb.append(COMMA);
//            HashSet<String> types = HANDSET_TYPES.get(imei);
//            for(String type : types){
//                sb.append(type);
//                sb.append(";");
//            }
//            sb.append("\n");
//            bw.write(sb.toString());
//        }
//            bw.close();
//        }catch (Exception ex){
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

        if(notEmpty(parts[4]))
            xdrHttp.setMsisdn(Long.parseLong(parts[4]));
        if(notEmpty(parts[5]))
            xdrHttp.setImei(Long.parseLong(parts[5]));

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
