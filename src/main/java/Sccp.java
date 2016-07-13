import java.util.HashSet;
import java.util.Set;

/**
 * Created by usrc on 6/15/2016.
 */
public class Sccp {
    static final int END_TIME= 0;
    static final int BEGIN_TIME = 1;
    static final int OPERATION_CODE = 51;
    static final int MCC = 62;
    static final int MNC = 63;
    static final int LAC = 64;
    static final int CELL_ID = 65;
    static Set<String> operationCodeSet = new HashSet();
    public static int noLac = 0;
    public static int noMcc = 0;
    public static int noMnc = 0;
    public static int noCell = 0;
    int mcc;
    int mnc;
    int lac;
    int cellId;
    String endTime;
    String beginTime;
    String operationCode;
    static {
        operationCodeSet.add("Send authentication info");
        operationCodeSet.add("Update location");
    }
    protected Sccp() {};

    static Sccp parse(String line) {
        Sccp sccp = new Sccp();
        String[] strs = line.split(",");
        try {
            if (strs[MCC].contains("-")) {
                // System.out.println("MCC:"+ strs[MCC]);
                noMcc++;
            } else {
                sccp.mcc = Integer.parseInt(strs[MCC]);
            }
            if (strs[MNC].contains("-")) {
                // System.out.println("MNC:"+ strs[MNC]);
                noMnc++;
            } else {
                sccp.mnc = Integer.parseInt(strs[MNC]);
            }
            if (strs[LAC].contains("$FF FF")) {
                //System.out.println("LAC:"+ strs[LAC]);
                noLac++;
            } else {
                sccp.lac = Integer.parseInt(strs[LAC]);
            }

            if (strs[CELL_ID].contains("$FF FF")) {
                //System.out.println("CELL_ID:"+ strs[CELL_ID]);
                noCell++;
            } else {
                sccp.cellId = Integer.parseInt(strs[CELL_ID]);
            }
            sccp.operationCode = strs[OPERATION_CODE];
            sccp.endTime = strs[END_TIME];
            sccp.beginTime = strs[BEGIN_TIME];

        } catch (Exception e) {
           // e.printStackTrace();
            return null;
        }
    return sccp;
    }

}
