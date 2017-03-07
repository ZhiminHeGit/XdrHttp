import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class OpenCellId {
    Map<String, GPS> openCellIdMap = new HashMap();
    static OpenCellId instance = null;
    // 460 China
    // 452 vietnam
    // 454 Hong Kong
    // 455 Macau
    // 466 TW  No
    // 502 Malasiya
    // 525 Singapore No
    // 262 Germany
    // csv format available at http://wiki.opencellid.org/wiki/Menu_map_view#database
    protected OpenCellId(String cellTowerFile) {
    System.out.println("Initializing OpenCellId");
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(cellTowerFile));
            String line = bufferedReader.readLine();
            while (line != null) {
                CellTowerGPS cellTowerGps = new CellTowerGPS(line);
                openCellIdMap.put(cellTowerGps.getCellTower().toString(), cellTowerGps.getGps());
                line = bufferedReader.readLine();
            }
            bufferedReader.close();
            System.out.println("OpenCellId Initialized");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void createInstance() {
        instance = new OpenCellId("/Volumes/DataDisk/Data/cell_towers.csv");
    }

    public static OpenCellId getInstance() {
        if (instance == null) {
            createInstance();
        }
        return  instance;
    }

    public GPS lookup(CellTower cellTower) {
        return openCellIdMap.get(cellTower.toString());
    }

    public static void main(String[] args) {
    }
};
