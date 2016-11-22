import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Demo {

    public static void main(String[] args) {
        long start = System.currentTimeMillis();


        Set<Long> imsiSet = new HashSet();
        Map<String, Set<Long>> appMap = new HashMap(),
            regionMap = new HashMap(), phoneBrandMap = new HashMap();

        double lat = 22.272861, lon = 114.182056;
        double radius = 10;
        int absolute_hour = 0;
        String dataDir = "/Volumes/DataDisk/processed/";

        if (args.length >= 3) {
            dataDir = args[0];
            lat = Double.parseDouble(args[1]);
            lon = Double.parseDouble(args[2]);
            radius = Double.parseDouble(args[3]);
            absolute_hour = Integer.parseInt(args[4]);

        };
        int date = absolute_hour / 24 + 1, hour = absolute_hour % 24;
        GPS centerGPS = new GPS(lat, lon);
        String input =
                String.format(dataDir + "2016100%d%02d.csv",
                        date, hour);

        try {
            BufferedReader reader = new BufferedReader(new FileReader(input));
            String line;

            while ((line = reader.readLine()) != null) {

                DemoRecord demoRecord = new DemoRecord(line);
                Long imsi = demoRecord.getImsi();
                GPS userGPS = demoRecord.getGps();
                if (GPS.getTravelDistance(userGPS, centerGPS) <= radius) {
                    imsiSet.add(imsi);

                    String region = demoRecord.getRegion();
                    if (regionMap.containsKey(region)) {
                        regionMap.get(region).add(imsi);
                    } else {
                        regionMap.put(region, Utils.newHashSet(imsi));
                    }
                    String app = demoRecord.getApp();
                    if (!app.contains("HostIsNull") && !app.contains("NoMatch")) {
                        if (appMap.containsKey(app)) {
                            appMap.get(app).add(imsi);
                        } else {
                            appMap.put(app, Utils.newHashSet(imsi));
                        }
                    }
                    String phoneBrand = demoRecord.getPhoneBrand();
                    if (!phoneBrand.contains("NoBrand")) {
                        if (phoneBrandMap.containsKey(phoneBrand)) {
                            phoneBrandMap.get(phoneBrand).add(imsi);
                        } else {
                            phoneBrandMap.put(phoneBrand, Utils.newHashSet(imsi));
                        }
                    }
                }
            }

            String s = "用户数Subscriber Number:" + imsiSet.size();
            System.out.println(s);

            System.out.println("=");
            getTop(regionMap);

            System.out.println("=");

            getTop(phoneBrandMap);

            System.out.println("=");
            getTop(appMap);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.err.println(System.currentTimeMillis() - start);
    }

    private static void getTop( Map<String, Set<Long>>  itemMap) {
        List<Map.Entry<String, Set<Long>>> sortedItem  = sortByValue(itemMap);

        String s;
        int total = 0;
        for (Map.Entry e: sortedItem) {
            total += ((Set) e.getValue()).size();
        }
        /* [ ['Task', 'Hours per Day'],
            ['Work',     11],
            ['Eat',      2],
            ['Commute',  2],
            ['Watch TV', 2],
            ['Sleep',    7]] */
        System.out.println("[\n[\"Item\", \"Count\"],");
        for (int i = 0; i < 5 && i < sortedItem.size(); i++) {
            int count =  sortedItem.get(i).getValue().size();
            s = String.format("[\"%s\", %d],", sortedItem.get(i).getKey(), count);
            total = total - count;
            System.out.println(s);
        }
        s = String.format("[\"其它\", %d]", total);
        System.out.println(s);
        System.out.println("]");
    }

    public static  List<Map.Entry<String, Set<Long>>>
        sortByValue( Map<String, Set<Long>> map ) {
            List<Map.Entry<String, Set<Long>>> list =
                    new LinkedList<>(map.entrySet());
            Collections.sort(list, new Comparator<Map.Entry<String, Set<Long>>>() {
                public int compare(Map.Entry<String, Set<Long>> o1, Map.Entry<String, Set<Long>> o2) {
                    return o2.getValue().size() -
                            o1.getValue().size();
                }
            });

            return list;
        }

}
