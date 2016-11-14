/**
 * Created by usrc on 6/21/2016.
 */
public class CellTower {
    String radio;
    int mcc;
    int mnc; // also called net
    int area;
    int cell;

    public  CellTower() {

    }

    public CellTower(XdrHttp xdrHttp) {
        setMcc(xdrHttp.getMcc());
        setMnc(xdrHttp.getMnc());

        if (xdrHttp.getCellLAC() != 0 && xdrHttp.getCellCI() != 0) {
            setArea(xdrHttp.getCellLAC());
            setCell(xdrHttp.getCellCI());
         /*      if (openCellIdMap.containsKey(cellTower.toString())) {
                   found_2G3G ++;
                   found = true;
               } else {
                   not_found_2G3G ++;
               } */

        } else if (xdrHttp.getTai() != 0 && xdrHttp.getEcgi() != 0) {
            setArea(xdrHttp.getTai());
            setCell(xdrHttp.getEcgi());
       /*        if (openCellIdMap.containsKey(cellTower.toString())) {
                   found_4G ++;
                   found = true;
               } else {
                   not_found_4G ++;
               } */
        } else {
     //       System.err.println("No base station found for record: " + xdrHttp);
        }
    }

    public String getRadio() {
        return radio;
    }

    public void setRadio(String radio) {
        this.radio = radio;
    }

    public int getMcc() {
        return mcc;
    }

    public void setMcc(int mcc) {
        this.mcc = mcc;
    }

    public int getMnc() {
        return mnc;
    }

    public void setMnc(int mnc) {
        this.mnc = mnc;
    }

    public int getArea() {
        return area;
    }

    public void setArea(int area) {
        this.area = area;
    }

    public int getCell() {
        return cell;
    }

    public void setCell(int cell) {
        this.cell = cell;
    }

    public String toString() {
        return "" + mcc + "," + mnc + "," + area + "," + cell;
    }
}
