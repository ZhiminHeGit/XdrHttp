import com.google.common.collect.Sets;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class OpenCellId {
    Map<String, GPS> openCellIdMap = new HashMap();
    // 460 China
    // 452 vietnam
    // 454 Hong Kong
    // 455 Macau
    // 466 TW  No
    // 502 Malasiya
    // 525 Singapore No
    // 262 Germany
    // csv format available at http://wiki.opencellid.org/wiki/Menu_map_view#database
    public OpenCellId(String cellTowerFile) {

       // Set<Integer> mccSet = Sets.newHashSet(460, 452, 454, 455, 466, 502, 525, 262);
        System.out.println("Initializing OpenCellId");
        //String output = "/Volumes/DataDisk/Data/cell_towers_7.csv";
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(cellTowerFile));
          //  BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(
          //          new FileOutputStream(output)));
            String line = bufferedReader.readLine();
         //   System.out.println(line);
         //   line = bufferedReader.readLine();
         //   int count = 0;
            while (line != null) {
                CellTowerGPS cellTowerGps = new CellTowerGPS(line);
            //    if (mccSet.contains(cellTowerGps.getCellTower().getMcc())) {
            //        count++;
            //        if (count % 10000 == 0) {
            //            System.out.println("included: " + count);
            //        }
                    openCellIdMap.put(cellTowerGps.getCellTower().toString(), cellTowerGps.getGps());
            //        bufferedWriter.write(line + "\n");
            //    }
                line = bufferedReader.readLine();
            }
            bufferedReader.close();
            // bufferedWriter.close();
            System.out.println("OpenCellId Initialized");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public GPS lookup(CellTower cellTower) {
        return openCellIdMap.get(cellTower.toString());
    }

    public static void main(String[] args) {
        OpenCellId openCellId = new OpenCellId("/Volumes/DataDisk/Data/cell_towers_7.csv");
    }
};
