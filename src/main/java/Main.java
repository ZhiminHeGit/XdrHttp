import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    private static final boolean DEBUG = false;
    private static final int THRESHOLD = 200;
    private static final String IPHONE = "iphone";
    private static final String ANDROID = "android";
    private static final String WINDOWS = "windows";
    private static final String OTHER = "other";
    private static final String LINE_BREAK = "\n";
    private static final int VIP_USER_CRETERIA = 10;
    private static final int UNDERUSED_USER_CRETERIA = 10;
    // regular expression to validate IP address
    private static final String IPADDRESS_PATTERN =
            "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                    "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                    "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                    "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";
    // some static HashMaps to store the data
    private static HashMap<Long, Long> IMSI_MSISDN_MAPPINGS, IMSI_IMEI_MAPPINGS, IMSI_COUNTS, IMSI_DURATIONS;
    private static HashMap<String, Long> FQDN_COUNTS, CONSOLIDATED_HOST_COUNT, IMSI_4G_LOCATIONS, IMSI_23G_LOCATIONS, FILTERED_CONSOLIDATED_HOST_COUNT;
    private static HashSet<Long> MOST_FREQUENT_USERS;
    private static HashMap<Long, HashMap<Long, String>> FREQUENT_USER_ACTIVITIES;
    private static HashMap<Long, Long> IMSI_4G, IMSI_23G;
    private static HashMap<Long, HashSet<String>> HANDSET_TYPES;
    private static HashMap<Long, MobileUser> MOBILE_USERS;
    private static HashSet<Integer> UNIQUE_HOME_MCCS, UNIQUE_SERVING_MCCS;
    private static HashSet<Roaming> ROAMING_SET;
    private static HashMap<Integer, Integer> HOME_MCC_COUNT_MAP;
    private static HashMap<Roaming, HashSet<Long>> ROAMING_USER_MAP;
    private static HashMap<Roaming, HashMap<String, Long>> HOST_MAP_BY_ROAMING, FQDN_MAP_BY_ROAMING, FILTERED_HOST_MAP_BY_ROAMING;
    private static HashMap<Integer, HashMap<String, Long>> HOST_MAP_BY_HOME_MCC, FQDN_MAP_BY_HOME_MCC, FILTERED_HOST_MAP_BY_HOME_MCC;
    private static HashMap<String, String> LABELLED_DOMAINS;
    private static HashSet<String> DOMAIN_LABELS;
    private static HashMap<String, Integer> DOMAIN_LABEL_COUNTS;
    private static HashMap<String, HashSet<Long>> LABEL_IMSI_MAPS;
    private static HashMap<String, String> MCC_MAP_BY_COUNTRY;
    private static HashMap<Integer, String> MCC_MAP;
    private static HashSet<Long> IMSIS;
    private static HashSet<String> EXCLULDED_DOMAINS;
    private static HashMap<Roaming, Long> ROAMING_DURATION_MAP, ROAMING_2G_DURATION_MAP, ROAMING_4G_DURATION_MAP;
    private static HashMap<Roaming, Long> ROAMING_CONTENT_LENGTH_MAP, ROAMING_2G_CONTENT_LENGTH_MAP, ROAMING_4G_CONTENT_LENGTH_MAP;
    private static HashMap<Roaming, HashMap<Long, Long>> ROAMING_VIP_USER_MAP, ROAMING_UNDERUSED_USER_MAP;
    private static HashMap<Roaming, HashMap<String, Long>> ROAMING_HOST_CONTENT_LENGTH_MAP;
    // some constants
    private static String rootPath = "point to the root directory of the Xdr Http files";   // needs to be updated by user
    //    private static String rootPath = "/Users/jianli/Downloads/cmidata/xdr_http/";   // needs to be updated by user
    private static String filePath = "create a file that contains all the file names to be processed under the rootPath defined above"; // needs to be updated by user
    //    private static String filePath = "/Users/jianli/Downloads/cmidata/xdr_http/files.txt";
    private static String suffix = ".csv";
    private static String COMMA = ",";
    private static String DOT = "\\.";
    private static Pattern pattern = Pattern.compile(IPADDRESS_PATTERN);

    public static void main(String[] args) {

        // initialization
        long total_4g = 0L, total_2g = 0L, total_4g_size = 0L, total_2g_size = 0L;
        initializeStaticParameters();

        populateDomainLabels();
        readMccList();
        populateExcludedDomains();

        // this is kind of additional after the first run to get the most frequent users
        // the IMSIs below represent the ones that consumes the most durations in connection
        // or with the most number of entries in the Xdr Http records
        MOST_FREQUENT_USERS = new HashSet();
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
        FREQUENT_USER_ACTIVITIES = new HashMap();

        System.out.println(new Date() + " start processing");

        try {
            // the below file has all the file names to be processed
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            String line = br.readLine();
            while (line != null) {
                // process each comma separated csv files
                BufferedReader br2 = new BufferedReader(new FileReader(rootPath + line + suffix));
                String line2 = br2.readLine();
                while (line2 != null) {
                    XdrHttp xdrHttp = XdrHttp.parse(line2);
                    if (xdrHttp.getImsi() != 0 && xdrHttp.getMsisdn() != 0) {
                        // IMSI is the primary key for most of the HashMaps
                        Long imsi = xdrHttp.getImsi();

                        // keep a HashSet of IMSIs for quick lookup in 2/3/4G locations
                        IMSIS.add(imsi);

                        MobileUser mobileUser = MOBILE_USERS.containsKey(imsi) ? MOBILE_USERS.get(imsi) : new MobileUser();
                        if (mobileUser.getImsi() == 0L)
                            mobileUser.setImsi(xdrHttp.getImsi());
                        //TODO: need more granularity for parsing the user agent string
                        if (xdrHttp.getUserAgent() != null) {
                            if (HANDSET_TYPES.containsKey(imsi)) {
                                HashSet<String> types = HANDSET_TYPES.get(imsi);
                                types.add(xdrHttp.getUserAgent());
                                HANDSET_TYPES.put(imsi, types);
                            } else {
                                HashSet<String> types = new HashSet();
                                types.add(xdrHttp.getUserAgent());
                                HANDSET_TYPES.put(imsi, types);
                            }

                            //TODO: handset information to be added to the user record
                            if (mobileUser.getPhoneType() == null) {
                                String userAgent = xdrHttp.getUserAgent().toLowerCase();
                                if (userAgent.contains(IPHONE))
                                    mobileUser.setPhoneType(IPHONE);
                                else if (userAgent.contains(ANDROID))
                                    mobileUser.setPhoneType(ANDROID);
                                else if (userAgent.contains(WINDOWS))
                                    mobileUser.setPhoneType(WINDOWS);
                            }
                        }

                        // IMSI to MSISDN mapping
                        if (!IMSI_MSISDN_MAPPINGS.containsKey(imsi))
                            IMSI_MSISDN_MAPPINGS.put(imsi, xdrHttp.getMsisdn());

                        if (mobileUser.getMsisdn() == 0L)
                            mobileUser.setMsisdn(xdrHttp.getMsisdn());

                        if (!IMSI_IMEI_MAPPINGS.containsKey(imsi))
                            IMSI_IMEI_MAPPINGS.put(imsi, xdrHttp.getImei());

                        if (mobileUser.getImei() == 0L)
                            mobileUser.setImei(xdrHttp.getImei());

                        // the number of occurrences in the xdr of each IMSI
                        if (!IMSI_COUNTS.containsKey(imsi))
                            IMSI_COUNTS.put(imsi, 1L);
                        else
                            IMSI_COUNTS.put(imsi, IMSI_COUNTS.get(imsi) + 1);

                        // process mcc/mnc/tac
                        if (mobileUser.getHomeMcc() == 0)
                            mobileUser.setHomeMcc(xdrHttp.getHomeMcc());

                        if (mobileUser.getHomeMnc() == 0)
                            mobileUser.setHomeMnc(xdrHttp.getHomeMnc());

                        if (mobileUser.getTac() == 0)
                            mobileUser.setTac(xdrHttp.getTac());

                        // count the user by Home Mcc
                        if (!MOBILE_USERS.containsKey(imsi)) {
                            if (HOME_MCC_COUNT_MAP.containsKey(xdrHttp.getHomeMcc()))
                                HOME_MCC_COUNT_MAP.put(xdrHttp.getHomeMcc(), HOME_MCC_COUNT_MAP.get(xdrHttp.getHomeMcc()) + 1);
                            else
                                HOME_MCC_COUNT_MAP.put(xdrHttp.getHomeMcc(), 1);
                        }

                        UNIQUE_SERVING_MCCS.add(xdrHttp.getServingMcc());
                        UNIQUE_HOME_MCCS.add(xdrHttp.getHomeMcc());
                        Roaming roaming = new Roaming(xdrHttp.getHomeMcc(), xdrHttp.getServingMcc());
                        ROAMING_SET.add(roaming);

                        // record the IMSIs to the roaming map
                        if (ROAMING_USER_MAP.containsKey(roaming)) {
                            HashSet<Long> roamingImsis = ROAMING_USER_MAP.get(roaming);
                            roamingImsis.add(imsi);
                            ROAMING_USER_MAP.put(roaming, roamingImsis);
                        } else {
                            HashSet<Long> roamingImsis = new HashSet();
                            roamingImsis.add(imsi);
                            ROAMING_USER_MAP.put(roaming, roamingImsis);
                        }

                        // footprint
                        HashMap<Integer, FootPrint> footPrintHashMap = mobileUser.getFootPrintHashMap();
                        if (footPrintHashMap.containsKey(xdrHttp.getServingMcc())) {
                            // the serving MCC already recorded
                            FootPrint footPrint = footPrintHashMap.get(xdrHttp.getServingMcc());
                            if (footPrint.getEnterDate() > xdrHttp.getDate()) {
                                footPrint.setEnterDate(xdrHttp.getDate());
                            }
                            if (footPrint.getExitDate() < xdrHttp.getDate()) {
                                footPrint.setExitDate(xdrHttp.getDate());
                            }
                            footPrint.setDataConsumption(footPrint.getDataConsumption() + xdrHttp.getContentLength());
                            footPrintHashMap.put(xdrHttp.getServingMcc(), footPrint);
                        } else {
                            // the serving MCC is not there yet, this means that user enters a new roaming destination
                            FootPrint footPrint = new FootPrint();
                            footPrint.setExitDate(xdrHttp.getDate());
                            footPrint.setEnterDate(xdrHttp.getDate());
                            footPrint.setServingMcc(xdrHttp.getServingMcc());
                            footPrint.setDataConsumption(xdrHttp.getContentLength());
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

                            if (!CONSOLIDATED_HOST_COUNT.containsKey(shortHost))
                                CONSOLIDATED_HOST_COUNT.put(shortHost, 1L);
                            else
                                CONSOLIDATED_HOST_COUNT.put(shortHost, CONSOLIDATED_HOST_COUNT.get(shortHost) + 1);
                            if (MOST_FREQUENT_USERS.contains(imsi)) {
                                if (xdrHttp.getDate() != 0L && xdrHttp.getHost() != null) {
                                    if (FREQUENT_USER_ACTIVITIES.containsKey(imsi)) {
                                        HashMap<Long, String> contents = FREQUENT_USER_ACTIVITIES.get(imsi);
                                        contents.put(xdrHttp.getDate(), shortHost);
                                        FREQUENT_USER_ACTIVITIES.put(imsi, contents);
                                    } else {
                                        HashMap<Long, String> contents = new HashMap();
                                        contents.put(xdrHttp.getDate(), shortHost);
                                        FREQUENT_USER_ACTIVITIES.put(imsi, contents);
                                    }
                                }
                            }

                            HashMap<String, Long> hostMap;
                            // do the count by roaming and by home MCC
                            if (HOST_MAP_BY_HOME_MCC.containsKey(xdrHttp.getHomeMcc())) {
                                hostMap = HOST_MAP_BY_HOME_MCC.get(xdrHttp.getHomeMcc());
                            } else {
                                hostMap = new HashMap();
                            }
                            if (hostMap.containsKey(shortHost)) {
                                hostMap.put(shortHost, hostMap.get(shortHost) + 1);
                            } else {
                                hostMap.put(shortHost, 1L);
                            }
                            HOST_MAP_BY_HOME_MCC.put(xdrHttp.getHomeMcc(), hostMap);

                            if (FQDN_MAP_BY_HOME_MCC.containsKey(xdrHttp.getHomeMcc())) {
                                hostMap = FQDN_MAP_BY_HOME_MCC.get(xdrHttp.getHomeMcc());
                            } else {
                                hostMap = new HashMap();
                            }
                            if (hostMap.containsKey(host)) {
                                hostMap.put(host, hostMap.get(host) + 1);
                            } else {
                                hostMap.put(host, 1L);
                            }
                            FQDN_MAP_BY_HOME_MCC.put(xdrHttp.getHomeMcc(), hostMap);

                            if (HOST_MAP_BY_ROAMING.containsKey(roaming)) {
                                hostMap = HOST_MAP_BY_ROAMING.get(roaming);
                            } else {
                                hostMap = new HashMap();
                            }
                            if (hostMap.containsKey(shortHost)) {
                                hostMap.put(shortHost, hostMap.get(shortHost) + 1);
                            } else {
                                hostMap.put(shortHost, 1L);
                            }
                            HOST_MAP_BY_ROAMING.put(roaming, hostMap);

                            if (FQDN_MAP_BY_ROAMING.containsKey(roaming)) {
                                hostMap = FQDN_MAP_BY_ROAMING.get(roaming);
                            } else {
                                hostMap = new HashMap();
                            }
                            if (hostMap.containsKey(host)) {
                                hostMap.put(host, hostMap.get(host) + 1);
                            } else {
                                hostMap.put(host, 1L);
                            }
                            FQDN_MAP_BY_ROAMING.put(roaming, hostMap);

                            // filter certain domains
                            if (!EXCLULDED_DOMAINS.contains(shortHost)) {
                                if (FILTERED_HOST_MAP_BY_HOME_MCC.containsKey(xdrHttp.getHomeMcc())) {
                                    hostMap = FILTERED_HOST_MAP_BY_HOME_MCC.get(xdrHttp.getHomeMcc());
                                } else {
                                    hostMap = new HashMap();
                                }
                                if (hostMap.containsKey(shortHost)) {
                                    hostMap.put(shortHost, hostMap.get(shortHost) + 1);
                                } else {
                                    hostMap.put(shortHost, 1L);
                                }
                                FILTERED_HOST_MAP_BY_HOME_MCC.put(xdrHttp.getHomeMcc(), hostMap);

                                if (FILTERED_HOST_MAP_BY_ROAMING.containsKey(roaming)) {
                                    hostMap = FILTERED_HOST_MAP_BY_ROAMING.get(roaming);
                                } else {
                                    hostMap = new HashMap();
                                }
                                if (hostMap.containsKey(shortHost)) {
                                    hostMap.put(shortHost, hostMap.get(shortHost) + 1);
                                } else {
                                    hostMap.put(shortHost, 1L);
                                }
                                FILTERED_HOST_MAP_BY_ROAMING.put(roaming, hostMap);

                                if (!FILTERED_CONSOLIDATED_HOST_COUNT.containsKey(shortHost))
                                    FILTERED_CONSOLIDATED_HOST_COUNT.put(shortHost, 1L);
                                else
                                    FILTERED_CONSOLIDATED_HOST_COUNT.put(shortHost, FILTERED_CONSOLIDATED_HOST_COUNT.get(shortHost) + 1);
                            }

                            HashMap<String, Integer> visitedSites = mobileUser.getVisitedSites();
                            if (visitedSites.containsKey(shortHost)) {
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

                            // based on roaming direction, count the data consumed per consolidated host
                            if (ROAMING_HOST_CONTENT_LENGTH_MAP.containsKey(roaming)) {
                                hostMap = ROAMING_HOST_CONTENT_LENGTH_MAP.get(roaming);
                            } else {
                                hostMap = new HashMap();
                            }
                            if (hostMap.containsKey(shortHost)) {
                                hostMap.put(shortHost, hostMap.get(shortHost) + xdrHttp.getContentLength());
                            } else {
                                hostMap.put(shortHost, xdrHttp.getContentLength());
                            }
                            ROAMING_HOST_CONTENT_LENGTH_MAP.put(roaming, hostMap);
                        }

                        // process the durations based on the 2/3/4G locations when it happened
                        if (xdrHttp.getDuration() != 0) {
                            if (xdrHttp.getTai() != 0 && xdrHttp.getEcgi() != 0) {
                                // 4G
                                if (IMSI_4G.containsKey(imsi)) {
                                    IMSI_4G.put(imsi, IMSI_4G.get(imsi) + xdrHttp.getDuration());
                                } else {
                                    IMSI_4G.put(imsi, ((long) xdrHttp.getDuration()));
                                }

                                total_4g += ((long) xdrHttp.getDuration());
                                String location4g = xdrHttp.getTai() + "," + xdrHttp.getEcgi();
                                if (IMSI_4G_LOCATIONS.containsKey(location4g)) {
                                    IMSI_4G_LOCATIONS.put(location4g, IMSI_4G_LOCATIONS.get(location4g) + xdrHttp.getDuration());
                                } else {
                                    IMSI_4G_LOCATIONS.put(location4g, ((long) xdrHttp.getDuration()));
                                }
                                total_4g_size += (long) xdrHttp.getContentLength();

                                if (ROAMING_4G_DURATION_MAP.containsKey(roaming)) {
                                    ROAMING_4G_DURATION_MAP.put(roaming, ROAMING_4G_DURATION_MAP.get(roaming) + xdrHttp.getDuration());
                                } else {
                                    ROAMING_4G_DURATION_MAP.put(roaming, (long) xdrHttp.getDuration());
                                }
                            }
                            if (xdrHttp.getCellCI() != 0 && xdrHttp.getCellLAC() != 0) {
                                if (IMSI_23G.containsKey(imsi)) {
                                    IMSI_23G.put(imsi, IMSI_23G.get(imsi) + xdrHttp.getDuration());
                                } else {
                                    IMSI_23G.put(imsi, ((long) xdrHttp.getDuration()));
                                }

                                total_2g += ((long) xdrHttp.getDuration());
                                String location23g = xdrHttp.getCellCI() + "," + xdrHttp.getCellLAC();
                                if (IMSI_23G_LOCATIONS.containsKey(location23g)) {
                                    IMSI_23G_LOCATIONS.put(location23g, IMSI_23G_LOCATIONS.get(location23g) + xdrHttp.getDuration());
                                } else {
                                    IMSI_23G_LOCATIONS.put(location23g, ((long) xdrHttp.getDuration()));
                                }
                                total_2g_size += (long) xdrHttp.getContentLength();

                                if (ROAMING_2G_DURATION_MAP.containsKey(roaming)) {
                                    ROAMING_2G_DURATION_MAP.put(roaming, ROAMING_2G_DURATION_MAP.get(roaming) + xdrHttp.getDuration());
                                } else {
                                    ROAMING_2G_DURATION_MAP.put(roaming, (long) xdrHttp.getDuration());
                                }
                            }

                            // the durations in the xdr of each IMSI
                            if (!IMSI_DURATIONS.containsKey(imsi))
                                IMSI_DURATIONS.put(imsi, (long) xdrHttp.getDuration());
                            else
                                IMSI_DURATIONS.put(imsi, IMSI_DURATIONS.get(imsi) + xdrHttp.getDuration());

                            if (!ROAMING_DURATION_MAP.containsKey(roaming))
                                ROAMING_DURATION_MAP.put(roaming, (long) xdrHttp.getDuration());
                            else
                                ROAMING_DURATION_MAP.put(roaming, ROAMING_DURATION_MAP.get(roaming) + xdrHttp.getDuration());
                        }

                        if (xdrHttp.getContentLength() != 0) {
                            if (!ROAMING_CONTENT_LENGTH_MAP.containsKey(roaming))
                                ROAMING_CONTENT_LENGTH_MAP.put(roaming, (long) xdrHttp.getContentLength());
                            else
                                ROAMING_CONTENT_LENGTH_MAP.put(roaming, ROAMING_CONTENT_LENGTH_MAP.get(roaming) + xdrHttp.getContentLength());

                            if (xdrHttp.getCellCI() != 0 && xdrHttp.getCellLAC() != 0) {
                                if (ROAMING_2G_CONTENT_LENGTH_MAP.containsKey(roaming)) {
                                    ROAMING_2G_CONTENT_LENGTH_MAP.put(roaming, ROAMING_2G_CONTENT_LENGTH_MAP.get(roaming) + xdrHttp.getContentLength());
                                } else {
                                    ROAMING_2G_CONTENT_LENGTH_MAP.put(roaming, (long) xdrHttp.getContentLength());
                                }
                            }

                            if (xdrHttp.getTai() != 0 && xdrHttp.getEcgi() != 0) {
                                if (ROAMING_4G_CONTENT_LENGTH_MAP.containsKey(roaming)) {
                                    ROAMING_4G_CONTENT_LENGTH_MAP.put(roaming, ROAMING_4G_CONTENT_LENGTH_MAP.get(roaming) + xdrHttp.getContentLength());
                                } else {
                                    ROAMING_4G_CONTENT_LENGTH_MAP.put(roaming, (long) xdrHttp.getContentLength());
                                }
                            }
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
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        System.out.println(new Date() + " completed processing");

        if (DEBUG) {
            System.out.println("total time under 2/3G: " + total_2g + "ms");
            System.out.println("total retrieved content length under 2/3G: " + total_2g_size + "bytes");
            System.out.println("total time under 4G: " + total_4g + "ms");
            System.out.println("total retrieved content length under 4G: " + total_4g_size + "bytes");
        }

        outputIMSI4G();
        outputHomeMCCs();
        outputServingMCCs();
        outputRoamingMap();

        outputHostMapByHomeMcc();
        outputFQDNMapByHomeMcc();

        outputHostMapByRoaming();
        outputFQDNMapByRoaming();

        outputUsersByRoaming();

        outputFilteredHostMapByHomeMcc();
        outputFilteredHostMapByRoaming();

        if (DEBUG) {
            for (Long imei : IMSI_MSISDN_MAPPINGS.keySet()) {
                System.out.println("IMSI: " + imei + " maps to MSISDN: " + IMSI_MSISDN_MAPPINGS.get(imei));
            }

            for (Long imei : IMSI_COUNTS.keySet()) {
                System.out.println("IMSI: " + imei + " appears: " + IMSI_COUNTS.get(imei) + " times");
            }

            for (String host : FQDN_COUNTS.keySet()) {
                System.out.println("host: " + host + " appears: " + FQDN_COUNTS.get(host) + " times");
            }

            for (String host : CONSOLIDATED_HOST_COUNT.keySet()) {
                System.out.println("host: " + host + " appears: " + CONSOLIDATED_HOST_COUNT.get(host) + " times");
            }
        }

        // write the various results to files
        writeToFileInValueOrder("consolidate.csv", CONSOLIDATED_HOST_COUNT, true);
        writeToFileInValueOrder("filtered-consolidated-hosts.csv", FILTERED_CONSOLIDATED_HOST_COUNT, true);
        writeToFileInValueOrder("fqdn.csv", FQDN_COUNTS, true);
        writeToFileInValueOrder("imsi.csv", IMSI_COUNTS, true);
        writeToFileInValueOrder("imsi-durations.csv", IMSI_DURATIONS, true);
        writeToFileInValueOrder("imsi-msisdn-mapping.csv", IMSI_MSISDN_MAPPINGS, true);
        writeToFileInValueOrder("imsi-imei-mapping.csv", IMSI_IMEI_MAPPINGS, true);
        writeToFileInValueOrder("users-by-home-mcc.csv", HOME_MCC_COUNT_MAP, true);
        writeToFileInValueOrder("roaming-duration.csv", ROAMING_DURATION_MAP, true);
        writeToFileInValueOrder("roaming-content-length.csv", ROAMING_CONTENT_LENGTH_MAP, true);
        writeToFileInValueOrder("roaming-2g-duration.csv", ROAMING_2G_DURATION_MAP, true);
        writeToFileInValueOrder("roaming-2g-content-length.csv", ROAMING_2G_CONTENT_LENGTH_MAP, true);
        writeToFileInValueOrder("roaming-4g-duration.csv", ROAMING_4G_DURATION_MAP, true);
        writeToFileInValueOrder("roaming-4g-content-length.csv", ROAMING_4G_CONTENT_LENGTH_MAP, true);

        for (Long imei : FREQUENT_USER_ACTIVITIES.keySet()) {
            writeToFileInValueOrder(imei + ".csv", FREQUENT_USER_ACTIVITIES.get(imei), true);
        }
        writeToFileInValueOrder("imsi-4g-locations.csv", IMSI_4G_LOCATIONS, true);
        writeToFileInValueOrder("imsi-23g-locations.csv", IMSI_23G_LOCATIONS, true);

        System.out.println(new Date() + " starting write results for each mobile user");

        outputRoamingHostContentLengthMap();
        outputMobileUsers();
        outputRoamingVIPUserMap();
        outputRoamingUnderUsedUserMap();
        outputRoamingDetails();
        writeToFileInValueOrder("label-imsi-counts.csv", DOMAIN_LABEL_COUNTS, true);
        outputLabelledIMSIMap();
        outputHandsets();
        System.out.println(new Date() + " completed writing mobile user data");
    }

    private static void outputIMSI4G() {
        try {
            File file = new File(rootPath + "imsi-4g.csv");
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            for (Long imsi : IMSIS) {
                long time2g = IMSI_23G.containsKey(imsi) ? IMSI_23G.get(imsi) : 0;
                long time4g = IMSI_4G.containsKey(imsi) ? IMSI_4G.get(imsi) : 0;
                long total = time2g + time4g;
                bw.write(imsi + "," + time2g + "," + time4g + "," + total + LINE_BREAK);
            }
            bw.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static void outputHomeMCCs() {
        try {
            File file = new File(rootPath + "homeMccs.csv");
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            for (Integer mcc : UNIQUE_HOME_MCCS) {
                bw.write(mcc + COMMA + MCC_MAP.get(mcc) + LINE_BREAK);
            }
            bw.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static void outputServingMCCs() {
        try {
            File file = new File(rootPath + "servingMccs.csv");
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            for (Integer mcc : UNIQUE_SERVING_MCCS) {
                bw.write(mcc + COMMA + MCC_MAP.get(mcc) + LINE_BREAK);
            }
            bw.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static void outputRoamingMap() {
        try {
            File file = new File(rootPath + "RoamingMaps.csv");
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            for (Roaming roaming : ROAMING_SET) {
                bw.write(roaming.getHomeMcc() + COMMA + MCC_MAP.get(roaming.getHomeMcc()) + COMMA + roaming.getServingMcc() + COMMA + MCC_MAP.get(roaming.getServingMcc()) + LINE_BREAK);
            }
            bw.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static void outputRoamingDetails() {
        try {
            File file = new File(rootPath + "RoamingDetails.csv");
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write("HomeMCC,Home Country/Region,Roaming MCC,Roaming Country/Region,User Count,Hosts Visited,Browse Duration (ms),");
            bw.write("Content Length (byte),Average Data Consumption (byte),Duration under 2/3G (ms),Content Length under 2/3G (byte),Duration under 4G (ms),Content Length under 4G (byte)");
            bw.write("VIP user count,Under-used user count" + LINE_BREAK);
            for (Roaming roaming : ROAMING_SET) {
                bw.write(roaming.getHomeMcc() + COMMA + MCC_MAP.get(roaming.getHomeMcc()) + COMMA + roaming.getServingMcc() + COMMA + MCC_MAP.get(roaming.getServingMcc()) + COMMA);
                int userCount = ROAMING_USER_MAP.get(roaming).size();
                long contentLength = ROAMING_CONTENT_LENGTH_MAP.containsKey(roaming) ? ROAMING_CONTENT_LENGTH_MAP.get(roaming) : 0;
                long average = contentLength / userCount;
                bw.write(userCount + COMMA + FQDN_MAP_BY_ROAMING.get(roaming).size() + COMMA + ROAMING_DURATION_MAP.get(roaming) + COMMA + contentLength + COMMA + average + COMMA);
                bw.write(ROAMING_2G_DURATION_MAP.get(roaming) + COMMA + ROAMING_2G_CONTENT_LENGTH_MAP.get(roaming) + COMMA + ROAMING_4G_DURATION_MAP.get(roaming) + COMMA + ROAMING_4G_CONTENT_LENGTH_MAP.get(roaming) + COMMA);
                int vipCount = ROAMING_VIP_USER_MAP.containsKey(roaming) ? ROAMING_VIP_USER_MAP.get(roaming).size() : 0;
                int underCount = ROAMING_UNDERUSED_USER_MAP.containsKey(roaming) ? ROAMING_UNDERUSED_USER_MAP.get(roaming).size() : 0;
                bw.write(vipCount + COMMA + underCount + LINE_BREAK);
            }
            bw.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static void outputUsersByRoaming() {
        try {
            for (Map.Entry<Roaming, HashSet<Long>> entry : ROAMING_USER_MAP.entrySet()) {
                String roamingDirection = entry.getKey().getHomeMcc() + MCC_MAP.get(entry.getKey().getHomeMcc()) + "-To-" + entry.getKey().getServingMcc() + MCC_MAP.get(entry.getKey().getServingMcc());
                File file = new File(rootPath + roamingDirection + "-users.csv");
                if (!file.exists()) {
                    file.createNewFile();
                }
                FileWriter fw = new FileWriter(file.getAbsoluteFile());
                BufferedWriter bw = new BufferedWriter(fw);
                for (Long valueEntry : entry.getValue()) {
                    bw.write(valueEntry + LINE_BREAK);
                }
                System.out.println(roamingDirection + ":" + entry.getValue().size() + " users");
                bw.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static HashMap<Roaming, Long> getRoamingAverageDataConsumption() {
        HashMap<Roaming, Long> results = new HashMap();
        for (Roaming roaming : ROAMING_SET) {
            int userCount = ROAMING_USER_MAP.get(roaming).size();
            long contentLength = ROAMING_CONTENT_LENGTH_MAP.containsKey(roaming) ? ROAMING_CONTENT_LENGTH_MAP.get(roaming) : 0;
            long average = contentLength / userCount;
            results.put(roaming, average);
        }

        return results;
    }

    private static void outputMobileUsers() {
        HashMap<Roaming, Long> roamingAverage = getRoamingAverageDataConsumption();
        for (Map.Entry<Long, MobileUser> entry : MOBILE_USERS.entrySet()) {
            try {
                File file = new File(rootPath + "imsis/" + entry.getKey() + ".csv");
                if (!file.exists()) {
                    file.createNewFile();
                }
                FileWriter fw = new FileWriter(file.getAbsoluteFile());
                BufferedWriter bw = new BufferedWriter(fw);
                MobileUser mobileUser = entry.getValue();
                bw.write("IMSI: " + mobileUser.getImsi() + LINE_BREAK);
                bw.write("MSISDN: " + mobileUser.getMsisdn() + LINE_BREAK);
                bw.write("IMEI: " + mobileUser.getImei() + LINE_BREAK);
                bw.write("home country/region:" + MCC_MAP.get(mobileUser.getHomeMcc()) + LINE_BREAK);
                bw.write("home MCC:" + mobileUser.getHomeMcc() + LINE_BREAK);
                bw.write("home MNC:" + mobileUser.getHomeMnc() + LINE_BREAK);
                bw.write("phone Type:" + (mobileUser.getPhoneType() == null ? OTHER : mobileUser.getPhoneType()) + LINE_BREAK);
                bw.write("last trip date:" + new Date(mobileUser.getLastTripDate()) + LINE_BREAK);
                bw.write("last trip to country/region:" + MCC_MAP.get(mobileUser.getLastTripMcc()) + LINE_BREAK);
                bw.write("last trip serving MCC:" + mobileUser.getLastTripMcc() + LINE_BREAK);
                bw.write("Footprints: \n");
                HashMap<FootPrint, Long> footPrintLongHashMap = new HashMap();
                for (Map.Entry<Integer, FootPrint> footPrintEntry : mobileUser.getFootPrintHashMap().entrySet()) {
                    footPrintLongHashMap.put(footPrintEntry.getValue(), footPrintEntry.getValue().getEnterDate());
                }
                footPrintLongHashMap = sortByValueReversed(footPrintLongHashMap);
                for (FootPrint footPrint : footPrintLongHashMap.keySet()) {
                    bw.write("Roaming country/region: " + MCC_MAP.get(footPrint.getServingMcc()) + COMMA);
                    bw.write("Roaming MCC: " + footPrint.getServingMcc() + COMMA);
                    bw.write("Entering: " + new Date(footPrint.getEnterDate()) + COMMA);
                    bw.write("Exiting: " + new Date(footPrint.getExitDate()) + COMMA);
                    bw.write("Data consumption: " + footPrint.getDataConsumption() + " bytes" + LINE_BREAK);
                    Roaming roaming = new Roaming(mobileUser.getHomeMcc(), footPrint.getServingMcc());
                    bw.write("Average data consumption in this roaming direction is: " + roamingAverage.get(roaming) + " bytes" + LINE_BREAK);

                    HashMap<Long, Long> valueMap;
                    if (footPrint.getDataConsumption() > roamingAverage.get(roaming) * VIP_USER_CRETERIA) {
                        // VIP user
                        if (ROAMING_VIP_USER_MAP.containsKey(roaming)) {
                            valueMap = ROAMING_VIP_USER_MAP.get(roaming);
                        } else
                            valueMap = new HashMap();

                        valueMap.put(mobileUser.getImsi(), footPrint.getDataConsumption());
                        ROAMING_VIP_USER_MAP.put(roaming, valueMap);

                        bw.write("VIP User!!!" + LINE_BREAK);
                    } else if (footPrint.getDataConsumption() < (roamingAverage.get(roaming) / UNDERUSED_USER_CRETERIA)) {
                        // VIP user
                        if (ROAMING_UNDERUSED_USER_MAP.containsKey(roaming)) {
                            valueMap = ROAMING_UNDERUSED_USER_MAP.get(roaming);
                        } else
                            valueMap = new HashMap();

                        valueMap.put(mobileUser.getImsi(), footPrint.getDataConsumption());
                        ROAMING_UNDERUSED_USER_MAP.put(roaming, valueMap);

                        bw.write("Under-used User!!!" + LINE_BREAK);
                    }
                }
                bw.write("Most visited sites (filtered) by counts:\n");
                HashMap<String, Integer> myMap = sortByValueReversed(mobileUser.getVisitedSites());
                HashMap<String, Integer> labelledMap = new HashMap();
                for (Map.Entry<String, Integer> mapEntry : myMap.entrySet()) {
                    if (!EXCLULDED_DOMAINS.contains(mapEntry.getKey()))
                        bw.write(mapEntry.getKey() + COMMA + mapEntry.getValue() + LINE_BREAK);
                }
                bw.write("Most visited sites by counts:\n");
                for (Map.Entry<String, Integer> mapEntry : myMap.entrySet()) {
                    bw.write(mapEntry.getKey() + COMMA + mapEntry.getValue() + LINE_BREAK);
                    if (mapEntry.getValue() > THRESHOLD) {
                        if (LABELLED_DOMAINS.containsKey(mapEntry.getKey())) {
                            String label = LABELLED_DOMAINS.get(mapEntry.getKey());
                            if (labelledMap.containsKey(label)) {
                                labelledMap.put(label, labelledMap.get(label) + 1);
                            } else
                                labelledMap.put(label, 1);

                            if (DOMAIN_LABEL_COUNTS.containsKey(label)) {
                                DOMAIN_LABEL_COUNTS.put(label, DOMAIN_LABEL_COUNTS.get(label) + 1);
                            } else
                                DOMAIN_LABEL_COUNTS.put(label, 1);
                        }
                    }
                }
                bw.write("Most visited sites (filtered) by browsing durations:\n");
                myMap = sortByValueReversed(mobileUser.getBrowseDurations());
                for (Map.Entry<String, Integer> mapEntry : myMap.entrySet()) {
                    if (!EXCLULDED_DOMAINS.contains(mapEntry.getKey()))
                        bw.write(mapEntry.getKey() + COMMA + mapEntry.getValue() + LINE_BREAK);
                }
                bw.write("Most visited sites by browsing durations:\n");
                for (Map.Entry<String, Integer> mapEntry : myMap.entrySet()) {
                    bw.write(mapEntry.getKey() + COMMA + mapEntry.getValue() + LINE_BREAK);
                }
                if (labelledMap.size() > 0) {
                    labelledMap = sortByValueReversed(labelledMap);
                    bw.write("Labels:\n");
                    for (Map.Entry<String, Integer> entry1 : labelledMap.entrySet()) {
                        bw.write(entry1.getKey() + COMMA + entry1.getValue() + LINE_BREAK);
                        HashSet<Long> imeiSet;
                        if (LABEL_IMSI_MAPS.containsKey(entry1.getKey())) {
                            imeiSet = LABEL_IMSI_MAPS.get(entry1.getKey());
                        } else {
                            imeiSet = new HashSet();
                        }
                        imeiSet.add(entry.getKey());
                        LABEL_IMSI_MAPS.put(entry1.getKey(), imeiSet);
                    }
                }
                bw.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private static void outputRoamingHostContentLengthMap() {
        for (Map.Entry<Roaming, HashMap<String, Long>> entry : ROAMING_HOST_CONTENT_LENGTH_MAP.entrySet()) {
            String fileName = entry.getKey().getHomeMcc() + MCC_MAP.get(entry.getKey().getHomeMcc()) + "-To-" + entry.getKey().getServingMcc() + MCC_MAP.get(entry.getKey().getServingMcc()) + "-data-consumption-per-host.csv";
            writeToFileInValueOrder(fileName, entry.getValue(), true);
        }
    }

    private static void outputRoamingVIPUserMap() {
        for (Map.Entry<Roaming, HashMap<Long, Long>> entry : ROAMING_VIP_USER_MAP.entrySet()) {
            String fileName = entry.getKey().getHomeMcc() + MCC_MAP.get(entry.getKey().getHomeMcc()) + "-To-" + entry.getKey().getServingMcc() + MCC_MAP.get(entry.getKey().getServingMcc()) + "-VIP-users.csv";
            writeToFileInValueOrder(fileName, entry.getValue(), true);
        }
    }

    private static void outputRoamingUnderUsedUserMap() {
        for (Map.Entry<Roaming, HashMap<Long, Long>> entry : ROAMING_UNDERUSED_USER_MAP.entrySet()) {
            String fileName = entry.getKey().getHomeMcc() + MCC_MAP.get(entry.getKey().getHomeMcc()) + "-To-" + entry.getKey().getServingMcc() + MCC_MAP.get(entry.getKey().getServingMcc()) + "-UnderUsed-users.csv";
            writeToFileInValueOrder(fileName, entry.getValue(), true);
        }
    }

    private static void outputLabelledIMSIMap() {
        try {
            for (Map.Entry<String, HashSet<Long>> entry : LABEL_IMSI_MAPS.entrySet()) {
                File file = new File(rootPath + entry.getKey() + ".csv");
                if (!file.exists()) {
                    file.createNewFile();
                }
                FileWriter fw = new FileWriter(file.getAbsoluteFile());
                BufferedWriter bw = new BufferedWriter(fw);
                for (Long imsi : entry.getValue()) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(imsi);
                    sb.append(LINE_BREAK);
                    bw.write(sb.toString());
                }
                bw.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static void outputHandsets() {
        try {
            File file = new File(rootPath + "handsets.csv");
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            for (Long imei : HANDSET_TYPES.keySet()) {
                StringBuilder sb = new StringBuilder();
                sb.append(imei);
                sb.append(COMMA);
                HashSet<String> types = HANDSET_TYPES.get(imei);
                for (String type : types) {
                    sb.append(type);
                    sb.append(";");
                }
                sb.append(LINE_BREAK);
                bw.write(sb.toString());
            }
            bw.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static void outputHostMapByHomeMcc() {
        try {
            for (Map.Entry<Integer, HashMap<String, Long>> entry : HOST_MAP_BY_HOME_MCC.entrySet()) {
                File file = new File(rootPath + entry.getKey() + MCC_MAP.get(entry.getKey()) + "-hosts.csv");
                if (!file.exists()) {
                    file.createNewFile();
                }
                FileWriter fw = new FileWriter(file.getAbsoluteFile());
                BufferedWriter bw = new BufferedWriter(fw);
                HashMap<String, Long> valueMap = sortByValueReversed(entry.getValue());
                for (Map.Entry<String, Long> valueEntry : valueMap.entrySet()) {
                    bw.write(valueEntry.getKey() + COMMA + valueEntry.getValue() + LINE_BREAK);
                }
                bw.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static void outputFilteredHostMapByHomeMcc() {
        try {
            for (Map.Entry<Integer, HashMap<String, Long>> entry : FILTERED_HOST_MAP_BY_HOME_MCC.entrySet()) {
                File file = new File(rootPath + entry.getKey() + MCC_MAP.get(entry.getKey()) + "-filteredhosts.csv");
                if (!file.exists()) {
                    file.createNewFile();
                }
                FileWriter fw = new FileWriter(file.getAbsoluteFile());
                BufferedWriter bw = new BufferedWriter(fw);
                HashMap<String, Long> valueMap = sortByValueReversed(entry.getValue());
                for (Map.Entry<String, Long> valueEntry : valueMap.entrySet()) {
                    bw.write(valueEntry.getKey() + COMMA + valueEntry.getValue() + LINE_BREAK);
                }
                bw.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static void outputFQDNMapByHomeMcc() {
        try {
            for (Map.Entry<Integer, HashMap<String, Long>> entry : FQDN_MAP_BY_HOME_MCC.entrySet()) {
                File file = new File(rootPath + entry.getKey() + MCC_MAP.get(entry.getKey()) + "-FQDNs.csv");
                if (!file.exists()) {
                    file.createNewFile();
                }
                FileWriter fw = new FileWriter(file.getAbsoluteFile());
                BufferedWriter bw = new BufferedWriter(fw);
                HashMap<String, Long> valueMap = sortByValueReversed(entry.getValue());
                for (Map.Entry<String, Long> valueEntry : valueMap.entrySet()) {
                    bw.write(valueEntry.getKey() + COMMA + valueEntry.getValue() + LINE_BREAK);
                }
                bw.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static void outputHostMapByRoaming() {
        try {
            for (Map.Entry<Roaming, HashMap<String, Long>> entry : HOST_MAP_BY_ROAMING.entrySet()) {
                File file = new File(rootPath + entry.getKey().getHomeMcc() + MCC_MAP.get(entry.getKey().getHomeMcc()) + "-To-" + entry.getKey().getServingMcc() + MCC_MAP.get(entry.getKey().getServingMcc()) + "-hosts.csv");
                if (!file.exists()) {
                    file.createNewFile();
                }
                FileWriter fw = new FileWriter(file.getAbsoluteFile());
                BufferedWriter bw = new BufferedWriter(fw);
                HashMap<String, Long> valueMap = sortByValueReversed(entry.getValue());
                for (Map.Entry<String, Long> valueEntry : valueMap.entrySet()) {
                    bw.write(valueEntry.getKey() + COMMA + valueEntry.getValue() + LINE_BREAK);
                }
                bw.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static void outputFilteredHostMapByRoaming() {
        try {
            for (Map.Entry<Roaming, HashMap<String, Long>> entry : FILTERED_HOST_MAP_BY_ROAMING.entrySet()) {
                File file = new File(rootPath + entry.getKey().getHomeMcc() + MCC_MAP.get(entry.getKey().getHomeMcc()) + "-To-" + entry.getKey().getServingMcc() + MCC_MAP.get(entry.getKey().getServingMcc()) + "-filteredhosts.csv");
                if (!file.exists()) {
                    file.createNewFile();
                }
                FileWriter fw = new FileWriter(file.getAbsoluteFile());
                BufferedWriter bw = new BufferedWriter(fw);
                HashMap<String, Long> valueMap = sortByValueReversed(entry.getValue());
                for (Map.Entry<String, Long> valueEntry : valueMap.entrySet()) {
                    bw.write(valueEntry.getKey() + COMMA + valueEntry.getValue() + LINE_BREAK);
                }
                bw.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static void outputFQDNMapByRoaming() {
        try {
            for (Map.Entry<Roaming, HashMap<String, Long>> entry : FQDN_MAP_BY_ROAMING.entrySet()) {
                File file = new File(rootPath + entry.getKey().getHomeMcc() + MCC_MAP.get(entry.getKey().getHomeMcc()) + "-To-" + entry.getKey().getServingMcc() + MCC_MAP.get(entry.getKey().getServingMcc()) + "-FQDNs.csv");
                if (!file.exists()) {
                    file.createNewFile();
                }
                FileWriter fw = new FileWriter(file.getAbsoluteFile());
                BufferedWriter bw = new BufferedWriter(fw);
                HashMap<String, Long> valueMap = sortByValueReversed(entry.getValue());
                for (Map.Entry<String, Long> valueEntry : valueMap.entrySet()) {
                    bw.write(valueEntry.getKey() + COMMA + valueEntry.getValue() + LINE_BREAK);
                }
                bw.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // write the content of HashMap in the order by values to a file specified by filename
    public static <K, V extends Comparable<? super V>> void writeToFileInValueOrder(String filename, HashMap<K, V> hashMap, boolean isReverseOrder) {
        if (!isReverseOrder)
            hashMap = sortByValue(hashMap);
        else
            hashMap = sortByValueReversed(hashMap);
        try {
            File file = new File(filename);

            // if file doesnt exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }

            FileWriter fw = new FileWriter(file.getAbsoluteFile(), false);
            BufferedWriter bw = new BufferedWriter(fw);
            for (HashMap.Entry<K, V> entry : hashMap.entrySet()) {
                bw.write(entry.getKey() + "," + entry.getValue() + LINE_BREAK);
            }
            bw.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static void initializeStaticParameters() {
        IMSI_COUNTS = new HashMap();
        IMSI_DURATIONS = new HashMap();
        IMSI_MSISDN_MAPPINGS = new HashMap();
        IMSI_IMEI_MAPPINGS = new HashMap();
        CONSOLIDATED_HOST_COUNT = new HashMap();
        IMSI_4G = new HashMap();
        IMSI_23G = new HashMap();
        IMSI_4G_LOCATIONS = new HashMap();
        IMSI_23G_LOCATIONS = new HashMap();

        FQDN_COUNTS = new HashMap();
        IMSIS = new HashSet();
        HANDSET_TYPES = new HashMap();
        MOBILE_USERS = new HashMap();
        UNIQUE_HOME_MCCS = new HashSet();
        UNIQUE_SERVING_MCCS = new HashSet();
        ROAMING_SET = new HashSet();
        LABELLED_DOMAINS = new HashMap();
        DOMAIN_LABEL_COUNTS = new HashMap();
        DOMAIN_LABELS = new HashSet();
        LABEL_IMSI_MAPS = new HashMap();
        MCC_MAP = new HashMap();
        MCC_MAP_BY_COUNTRY = new HashMap();
        HOME_MCC_COUNT_MAP = new HashMap();
        ROAMING_USER_MAP = new HashMap();
        HOST_MAP_BY_HOME_MCC = new HashMap();
        HOST_MAP_BY_ROAMING = new HashMap();
        FQDN_MAP_BY_HOME_MCC = new HashMap();
        FQDN_MAP_BY_ROAMING = new HashMap();
        EXCLULDED_DOMAINS = new HashSet();
        FILTERED_HOST_MAP_BY_HOME_MCC = new HashMap();
        FILTERED_HOST_MAP_BY_ROAMING = new HashMap();
        FILTERED_CONSOLIDATED_HOST_COUNT = new HashMap();
        ROAMING_CONTENT_LENGTH_MAP = new HashMap();
        ROAMING_DURATION_MAP = new HashMap();
        ROAMING_2G_CONTENT_LENGTH_MAP = new HashMap();
        ROAMING_2G_DURATION_MAP = new HashMap();
        ROAMING_4G_CONTENT_LENGTH_MAP = new HashMap();
        ROAMING_4G_DURATION_MAP = new HashMap();

        ROAMING_VIP_USER_MAP = new HashMap();
        ROAMING_UNDERUSED_USER_MAP = new HashMap();
        ROAMING_HOST_CONTENT_LENGTH_MAP = new HashMap();
    }

    private static void populateDomainLabels() {
        try {
            // the below file has all the file names to be processed
            BufferedReader br = new BufferedReader(new FileReader(rootPath + "labelled-domains.csv"));
            String line = br.readLine();
            while (line != null) {
                String[] parts = line.split(COMMA);
                LABELLED_DOMAINS.put(parts[0], parts[1]);
                DOMAIN_LABELS.add(parts[1]);

                line = br.readLine();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static void populateExcludedDomains() {
        try {
            // the below file has all the file names to be processed
            BufferedReader br = new BufferedReader(new FileReader(rootPath + "excludeHosts.txt"));
            String line = br.readLine();
            while (line != null) {
                EXCLULDED_DOMAINS.add(line);

                line = br.readLine();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static void readMccList() {
        try {
            // the below file has all the file names to be processed
            BufferedReader br = new BufferedReader(new FileReader(rootPath + "MCC.csv"));
            String line = br.readLine();
            while (line != null) {
                String[] parts = line.split(COMMA);
                MCC_MAP_BY_COUNTRY.put(parts[0], parts.length == 3 ? parts[1] + COMMA + parts[2] : parts[1]);
                for (int i = 1; i < parts.length; i++) {
                    MCC_MAP.put(Integer.parseInt(parts[i]), parts[0]);
                }

                line = br.readLine();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // utility to reach to the root level of a FQDN
    public static String consolidateFQDN(String fqdn) {
        if (fqdn == null || fqdn.isEmpty())
            return null;

        if (isIPAddress(fqdn))
            return fqdn;

        String[] parts = fqdn.split(DOT);
        if (parts.length <= 2)
            return fqdn;
        else if (parts[parts.length - 2].equals("co") || parts[parts.length - 2].equals("com") || parts[parts.length - 2].equals("net") || parts[parts.length - 2].equals("org") || parts[parts.length - 2].equals("gov"))
            return parts[parts.length - 3] + "." + parts[parts.length - 2] + "." + parts[parts.length - 1];
        else
            return parts[parts.length - 2] + "." + parts[parts.length - 1];
    }

    private static boolean isIPAddress(final String ip) {
        Matcher matcher = pattern.matcher(ip);
        return matcher.matches();
    }

    private static <K, V extends Comparable<? super V>> HashMap<K, V> sortByValue(HashMap<K, V> map) {
        HashMap<K, V> result = new LinkedHashMap();
    /*    Stream<Map.Entry<K, V>> st = map.entrySet().stream();

        st.sorted( Map.Entry.comparingByValue() )
                .forEachOrdered( e -> result.put(e.getKey(), e.getValue()) );

        return result; */
        return null;
    }

    public static <K, V extends Comparable<? super V>> HashMap<K, V> sortByValueReversed(HashMap<K, V> map) {
      /*  HashMap<K, V> result = new LinkedHashMap();
        Stream<Map.Entry<K, V>> st = map.entrySet().stream();

        st.sorted( comparingByValueReversed() )
                .forEachOrdered( e -> result.put(e.getKey(), e.getValue()) );

        return result; */
        return null;
    }

    public static <K, V extends Comparable<? super V>> Comparator<Map.Entry<K, V>> comparingByValueReversed() {
     /*   return (Comparator<Map.Entry<K, V>> & Serializable)
                (c1, c2) -> c2.getValue().compareTo(c1.getValue()); */
        return null;
    }
}
