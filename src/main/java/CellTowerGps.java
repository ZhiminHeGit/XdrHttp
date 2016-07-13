/**
 * Created by usrc on 6/21/2016.
 */
public class CellTowerGPS {
    static final int RADIO = 0;
    static final int MCC = 1;
    static final int MNC = 2;
    static final int AREA = 3;
    static final int CELL = 4;
    static final int LON = 6;
    static final int LAT = 7;
    CellTower cellTower = new CellTower();
    GPS gps = new GPS();

    CellTowerGPS(String input) {
        String[] strs = input.split(",");
        cellTower.setRadio(strs[RADIO]);
        if (strs[MCC].isEmpty()) {
            cellTower.setMcc(460);
        } else {
            cellTower.setMcc(Integer.parseInt(strs[MCC]));
        }
        if (strs[MNC].isEmpty()) {
            cellTower.setMnc(0);
        } else {
            cellTower.setMnc(Integer.parseInt(strs[MNC]));
        }

        cellTower.setArea(Integer.parseInt(strs[AREA]));
        cellTower.setCell(Integer.parseInt(strs[CELL]));
        gps.setLon(Double.parseDouble(strs[LON]));
        gps.setLat(Double.parseDouble(strs[LAT]));

    }

    public CellTower getCellTower() {
        return cellTower;
    }

    public GPS getGps() {
        return gps;
    }
}

