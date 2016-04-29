import java.util.HashMap;
import java.util.HashSet;

public class MobileUser {
    long imsi, msisdn, imei;
    int homeMcc, homeMnc, tac;
    String phoneBrand, phoneType;
    long lastTripDate;
    int lastTripMcc;

    HashMap<String, Integer> visitedSites, browseDurations;

    HashMap<Integer, FootPrint> footPrintHashMap;

    public HashMap<Integer, FootPrint> getFootPrintHashMap() {
        return footPrintHashMap;
    }

    public void setFootPrintHashMap(HashMap<Integer, FootPrint> footPrintHashMap) {
        this.footPrintHashMap = footPrintHashMap;
    }

    public HashMap<String, Integer> getVisitedSites() {
        return visitedSites;
    }

    public void setVisitedSites(HashMap<String, Integer> visitedSites) {
        this.visitedSites = visitedSites;
    }

    public HashMap<String, Integer> getBrowseDurations() {
        return browseDurations;
    }

    public void setBrowseDurations(HashMap<String, Integer> browseDurations) {
        this.browseDurations = browseDurations;
    }

    public int getLastTripMcc() {
        return lastTripMcc;
    }

    public void setLastTripMcc(int lastTripMcc) {
        this.lastTripMcc = lastTripMcc;
    }

    public MobileUser() {
        imei = imsi = msisdn = lastTripDate = 0L;
        homeMcc = homeMnc = tac = lastTripMcc = 0;
        footPrintHashMap = new HashMap<>();
        this.visitedSites = new HashMap<>();
        this.browseDurations = new HashMap<>();

        phoneBrand = null;
        phoneType = null;
    }

    public long getImsi() {
        return imsi;
    }

    public void setImsi(long imsi) {
        this.imsi = imsi;
    }

    public long getMsisdn() {
        return msisdn;
    }

    public void setMsisdn(long msisdn) {
        this.msisdn = msisdn;
    }

    public long getImei() {
        return imei;
    }

    public void setImei(long imei) {
        this.imei = imei;
    }

    public int getHomeMcc() {
        return homeMcc;
    }

    public void setHomeMcc(int homeMcc) {
        this.homeMcc = homeMcc;
    }

    public int getHomeMnc() {
        return homeMnc;
    }

    public void setHomeMnc(int homeMnc) {
        this.homeMnc = homeMnc;
    }

    public int getTac() {
        return tac;
    }

    public void setTac(int tac) {
        this.tac = tac;
    }

    public String getPhoneBrand() {
        return phoneBrand;
    }

    public void setPhoneBrand(String phoneBrand) {
        this.phoneBrand = phoneBrand;
    }

    public String getPhoneType() {
        return phoneType;
    }

    public void setPhoneType(String phoneType) {
        this.phoneType = phoneType;
    }

    public long getLastTripDate() {
        return lastTripDate;
    }

    public void setLastTripDate(long lastTripDate) {
        this.lastTripDate = lastTripDate;
    }
}
