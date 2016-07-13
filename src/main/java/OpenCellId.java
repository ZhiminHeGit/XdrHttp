import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by usrc on 6/22/2016.
 */
public class OpenCellId {
    Map<String, GPS> openCellIdMap = new HashMap();

    // 452 vietnam
    // 454 Hong Kong
    // 455 Macau
    // 466 TW  No
    // 502 Malasiya
    // 525 Singapore No
    // csv format available at http://wiki.opencellid.org/wiki/Menu_map_view#database
    public OpenCellId(String cellTowerFile) {
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new FileReader(cellTowerFile));
            String line = bufferedReader.readLine();
            int count = 0;
            int included_count = 0;
            System.out.println(line);
            line = bufferedReader.readLine();
            while (line != null) {
                count++;
                CellTowerGPS cellTowerGps = new CellTowerGPS(line);
                included_count++;
                if (included_count % 10000 == 0) {
                    System.out.println("included: " + included_count);
                }
                openCellIdMap.put(cellTowerGps.getCellTower().toString(), cellTowerGps.getGps());

                line = bufferedReader.readLine();
            }
            System.out.println("total loaded:" + count);
            System.out.println("total included:" + included_count);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public GPS lookup(CellTower cellTower) {
        return openCellIdMap.get(cellTower.toString());
    }
}
