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
    GPS gps;

    CellTowerGPS(String input) {
        String[] strs = input.split(",");
        cellTower.setRadio(strs[RADIO]);
        cellTower.setMcc(Integer.parseInt(strs[MCC]));
        cellTower.setMnc(Integer.parseInt(strs[MNC]));

        cellTower.setArea(Integer.parseInt(strs[AREA]));
        cellTower.setCell(Integer.parseInt(strs[CELL]));
        gps = new GPS(Double.parseDouble(strs[LAT]), Double.parseDouble(strs[LON]));
    }

    public CellTower getCellTower() {
        return cellTower;
    }

    public GPS getGps() {
        return gps;
    }
}

