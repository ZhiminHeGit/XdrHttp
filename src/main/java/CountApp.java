import org.apache.hadoop.hbase.*;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.*;
import java.util.*;

public class CountApp extends CountXdr {
    AppRules appRules = new AppRules();
    static public void main(String[] args) {
        CountApp countApp = new CountApp();
        countApp.processAll("app");
    }

    String getRegion(String[] parts) {
        if (parts[1].equals("460")) {
            return provinceLookup.lookupByMsisdn(Long.parseLong(parts[0]));
        } else {
            return mccLookup.lookup(Integer.parseInt(parts[1]));
        }
    }

    void processAll(String type) {
        //String[] dateList = { "20161001", "20161002", "20161003", "20161004"};
        String[] dateList = { "20161001"};
        try {
            System.out.println("Started " + type);
            for (String date : dateList) {
                if (table != null) {
                    table.close();
                }
                table = connection.getTable(TableName.valueOf(type + "_" + date));
                regionalAppMap.clear();

                Scan scan = new Scan();
                totalUser = 0;
                regionalAppMap.put(WORLD, new HashMap());
                regionalAppMap.put(MAINLAND, new HashMap());
                regionalAppMap.put(OVERSEA, new HashMap());

                ResultScanner scanner = table.getScanner(scan);
                Result result = scanner.next();
                while (result != null) {
                    result = scanner.next();
                    while (result != null) {
                        totalUser++;
                        processRow(result);
                        result = scanner.next();
                    }
                }
                generateReport("/Volumes/WDPassport/CMIData/" + type + "_" + date + ".txt");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processRow(Result result) {
        String[] parts = Bytes.toString(result.getRow()).split(":");
        String region = getRegion(parts);
        if (!regionalAppMap.containsKey(region)) {
            regionalAppMap.put(region, new HashMap());
        }
        Set<String> appSet = new HashSet();

        NavigableMap<byte[], byte[]> familyMap = result.getFamilyMap(c("data"));
        for (byte[] columnName:familyMap.keySet()) {
            String qualifier = Bytes.toString(columnName);
        //    System.out.println(qualifier);
            String host = qualifier.split(":")[0];
            String app = appRules.getUSRCApp(host);
            if (!app.equals(AppRules.NO_MATCH)) {
                appSet.add(app);
            } else {
                appSet.add(host);
            }
        }

        Map<String, Integer> appMap = regionalAppMap.get(region);
        for (String app : appSet) {
            Increment(parts[1].equals("460"), appMap, app);
        }
        Increment(parts[1].equals("460"), appMap, TOTAL_USER);
    }

  }
