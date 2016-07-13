import org.apache.commons.lang3.tuple.Pair;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;

public class HttpUserDaily implements Serializable{
    // constants
    private static final char COMMA = ',';
    private static final char SEMICOLON = ';';
    private static final String DATE_FORMAT = "yyyyMMdd";
    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat(DATE_FORMAT);

    private long imsi;
    private long date;
    private List<Long> imeis;
    private int homeMcc, homeMnc;
    private HashSet<Pair<Integer, Integer>> visitingMccMncPairs;
    private HashSet<Pair<Integer, Integer>> baseStationPairs_3G, baseStationPairs_4G;
    private double gprs_vol;
    private long duration_3G, content_length_3G, num_request_3G;
    private long duration_4G, content_length_4G, num_request_4G;
    private long video_length, total_length;

    private HashSet<String> agentSet, androidSet;
    private List<FootPrint> footPrints;

    private boolean isHotSpot;
    private int hotspot_type, high_vol_type, video_use_type, numb_bs_cell, num_user_agents, num_network_transition;
    private List<Integer> device_type_ids;
    private int num_country, max_domain_minute, max_host_minute, max_agent_minute;
    private String agent_details, android_handsets;
    //private CarrierNetwork lastNetwork;


    public HttpUserDaily() {
        visitingMccMncPairs = new HashSet<>();
        baseStationPairs_3G = new HashSet<>();
        baseStationPairs_4G = new HashSet<>();
        imeis = new ArrayList<>();
        device_type_ids = new ArrayList<>();
     //   lastNetwork = CarrierNetwork.UNKNOWN;
        androidSet = new HashSet<>();
        agentSet = new HashSet<>();
        footPrints = new ArrayList<>();
    }

    public List<FootPrint> getFootPrints() {
        return footPrints;
    }

    public void setFootPrints(List<FootPrint> footPrints) {
        this.footPrints = footPrints;
    }

    public long getVideo_length() {
        return video_length;
    }

    public void setVideo_length(long video_length) {
        this.video_length = video_length;
    }

    public long getTotal_length() {
        return total_length;
    }

    public void setTotal_length(long total_length) {
        this.total_length = total_length;
    }

    public HashSet<String> getAgentSet() {
        return agentSet;
    }

    public void setAgentSet(HashSet<String> agentSet) {
        this.agentSet = agentSet;
    }

    public HashSet<String> getAndroidSet() {
        return androidSet;
    }

    public void setAndroidSet(HashSet<String> androidSet) {
        this.androidSet = androidSet;
    }

    public static String getSchema() {
        StringBuilder sb = new StringBuilder();

        sb.append("imsi" + COMMA);
        sb.append("visit_date" + COMMA);
        sb.append("imei" + COMMA);
        sb.append("home_mcc" + COMMA);
        sb.append("home_mnc" + COMMA);
        sb.append("num_visit_mcc" + COMMA);
        sb.append("num_visit_mnc" + COMMA);
        sb.append("num_network_transition" + COMMA);
        sb.append("num_visit_operator" + COMMA);
        sb.append("gprs_vol" + COMMA);
        sb.append("duration_3G" + COMMA);
        sb.append("content_length_3G" + COMMA);
        sb.append("num_request_3G" + COMMA);
        sb.append("duration_4G" + COMMA);
        sb.append("content_length_4G" + COMMA);
        sb.append("num_request_4G" + COMMA);
        sb.append("hotspot" + COMMA);
        sb.append("hotspot_type" + COMMA);
        sb.append("high_vol_type" + COMMA);
        sb.append("video_use_type" + COMMA);
        sb.append("num_bs_cell" + COMMA);
        sb.append("num_user_agents" + COMMA);
        sb.append("device_type_id" + COMMA);
        sb.append("num_country" + COMMA);
        sb.append("max_domain_minu" + COMMA);
        sb.append("max_host_minu" + COMMA);
        sb.append("max_agent_minu" + COMMA);
        sb.append("agent_details" + COMMA);
        sb.append("android_handsets");

        return sb.toString();
    }

    public static <T> String listToString(List<T> mylist) {
        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (T item : mylist) {
            if (count++ != 0)
                sb.append(SEMICOLON);
            sb.append(item);
        }

        return sb.toString();
    }

    public static <T> String setToString(Set<T> mySet) {
        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (T item : mySet) {
            if (count++ != 0)
                sb.append(SEMICOLON);
            sb.append(item);
        }

        return sb.toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

//        sb.append(imsi);
//        sb.append(COMMA);
        sb.append(SIMPLE_DATE_FORMAT.format(new Date(date)));
        sb.append(COMMA);
        sb.append(listToString(imeis));
        sb.append(COMMA);
        sb.append(homeMcc);
        sb.append(COMMA);
        sb.append(homeMnc);
        sb.append(COMMA);
        HashSet<Integer> mccSet = new HashSet<>();
        List<Integer> mncList = new ArrayList<>();
        for (Pair<Integer, Integer> pair : visitingMccMncPairs) {
            mccSet.add(pair.getKey());
            mncList.add(pair.getValue());
        }

        sb.append(mccSet.size());
        sb.append(COMMA);
        sb.append(mncList.size());
        sb.append(COMMA);
        sb.append(visitingMccMncPairs.size());
        sb.append(COMMA);
        sb.append(gprs_vol);
        sb.append(COMMA);
        sb.append(duration_3G);
        sb.append(COMMA);
        sb.append(content_length_3G);
        sb.append(COMMA);
        sb.append(num_request_3G);
        sb.append(COMMA);
        sb.append(duration_4G);
        sb.append(COMMA);
        sb.append(content_length_4G);
        sb.append(COMMA);
        sb.append(num_request_4G);
        sb.append(COMMA);
        sb.append((isHotSpot() ? 'y' : 'n'));
        sb.append(COMMA);
        sb.append(getHotspot_type());
        sb.append(COMMA);
        sb.append(high_vol_type);
        sb.append(COMMA);
        sb.append(getVideo_use_type());
        sb.append(COMMA);
        sb.append(getNumb_bs_cell());
        sb.append(COMMA);
        sb.append(getNum_user_agents());
        sb.append(COMMA);
        sb.append(listToString(device_type_ids));
        sb.append(COMMA);
        sb.append(mccSet.size());
        sb.append(COMMA);
        sb.append(max_domain_minute);
        sb.append(COMMA);
        sb.append(max_host_minute);
        sb.append(COMMA);
        sb.append(max_agent_minute);
        sb.append(COMMA);
        sb.append(setToString(agentSet));
        sb.append(COMMA);
        sb.append(setToString(androidSet));

        return sb.toString();
    }



    public void setMax_agent_minute(int max_agent_minute) {
        this.max_agent_minute = max_agent_minute;
    }

    public long getImsi() {
        return imsi;
    }

    public void setImsi(long imsi) {
        this.imsi = imsi;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public List<Long> getImeis() {
        return imeis;
    }

    public void setImeis(List<Long> imeis) {
        this.imeis = imeis;
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

    public HashSet<Pair<Integer, Integer>> getVisitingMccMncPairs() {
        return visitingMccMncPairs;
    }

    public void setVisitingMccMncPairs(HashSet<Pair<Integer, Integer>> visitingMccMncPairs) {
        this.visitingMccMncPairs = visitingMccMncPairs;
    }

    public HashSet<Pair<Integer, Integer>> getBaseStationPairs_3G() {
        return baseStationPairs_3G;
    }

    public void setBaseStationPairs_3G(HashSet<Pair<Integer, Integer>> baseStationPairs_3G) {
        this.baseStationPairs_3G = baseStationPairs_3G;
    }

    public HashSet<Pair<Integer, Integer>> getBaseStationPairs_4G() {
        return baseStationPairs_4G;
    }

    public void setBaseStationPairs_4G(HashSet<Pair<Integer, Integer>> baseStationPairs_4G) {
        this.baseStationPairs_4G = baseStationPairs_4G;
    }

    public double getGprs_vol() {
        return gprs_vol;
    }

    public void setGprs_vol(double gprs_vol) {
        this.gprs_vol = gprs_vol;
    }

    public long getDuration_3G() {
        return duration_3G;
    }

    public void setDuration_3G(long duration_3G) {
        this.duration_3G = duration_3G;
    }

    public long getContent_length_3G() {
        return content_length_3G;
    }

    public void setContent_length_3G(long content_length_3G) {
        this.content_length_3G = content_length_3G;
    }

    public long getNum_request_3G() {
        return num_request_3G;
    }

    public void setNum_request_3G(long num_request_3G) {
        this.num_request_3G = num_request_3G;
    }

    public long getDuration_4G() {
        return duration_4G;
    }

    public void setDuration_4G(long duration_4G) {
        this.duration_4G = duration_4G;
    }

    public long getContent_length_4G() {
        return content_length_4G;
    }

    public void setContent_length_4G(long content_length_4G) {
        this.content_length_4G = content_length_4G;
    }

    public long getNum_request_4G() {
        return num_request_4G;
    }

    public void setNum_request_4G(long num_request_4G) {
        this.num_request_4G = num_request_4G;
    }

    public boolean isHotSpot() {
        return agentSet.size() >= 4;
    }

    public void setHotSpot(boolean hotSpot) {
        isHotSpot = hotSpot;
    }

    public int getHotspot_type() {
        return agentSet.size() >=6 ? 2 : (agentSet.size() >= 4 ? 1 : 0);
    }

    public void setHotspot_type(int hotspot_type) {
        this.hotspot_type = hotspot_type;
    }

    public int getHigh_vol_type() {
        return high_vol_type;
    }

    public void setHigh_vol_type(int high_vol_type) {
        this.high_vol_type = high_vol_type;
    }

    public int getVideo_use_type() {
        return ((double)video_length)/total_length > 0.7d ? 1 : 0;
    }

    public int getNumb_bs_cell() {
        return baseStationPairs_3G.size() + baseStationPairs_4G.size();
    }

    public int getNum_user_agents() {
        return agentSet.size();
    }

    public List<Integer> getDevice_type_ids() {
        return device_type_ids;
    }

    public void setDevice_type_ids(List<Integer> device_type_ids) {
        this.device_type_ids = device_type_ids;
    }

    public int getNum_country() {
        return num_country;
    }

    public void setNum_country(int num_country) {
        this.num_country = num_country;
    }

    public int getMax_domain_minute() {
        return max_domain_minute;
    }

    public void setMax_domain_minute(int max_domain_minute) {
        this.max_domain_minute = max_domain_minute;
    }

    public int getMax_host_minute() {
        return max_host_minute;
    }

    public void setMax_host_minute(int max_host_minute) {
        this.max_host_minute = max_host_minute;
    }

    public String getAgent_details() {
        return agent_details;
    }

    public void setAgent_details(String agent_details) {
        this.agent_details = agent_details;
    }

    public String getAndroid_handsets() {
        return android_handsets;
    }

    public void setAndroid_handsets(String android_handsets) {
        this.android_handsets = android_handsets;
    }
}