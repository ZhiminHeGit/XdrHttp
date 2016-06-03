/**
 * Created by usrc on 5/23/2016.
 */
public class VipLine {
    static final int DATE = 0;
    static final int DURATION_MS = 10;
    static final int CONTENT_LENGTH = 11;
    static final int HOST = 14;
    static final int USER_AGENT = 17;
    static final int RESPONSE_CODE = 13;
    static final int CONTENT_TYPE = 16;
    static final int SERVING_MCC = 2;
    String[] data;

    public VipLine(String line) {
        data = line.split(",");
    }

    public String getContentType() {
        return data[CONTENT_TYPE].split(";")[0];
    }

    public String getResponseCode() {
        return data[RESPONSE_CODE];
    }

    public String getDate() {
        String[] dates = data[DATE].split("[/ ]");
        return dates[2] + dates[0] + dates[1];
    }

    public int getDurationMS() {
        return Integer.valueOf(data[DURATION_MS]);
    }

    public int getContentLength() {
        return Integer.valueOf(data[CONTENT_LENGTH]);
    }

    public String getHost() {
        return data[HOST].toLowerCase();
    }

    public String getFilteredHost() {
        return Main.consolidateFQDN(getHost());
    }

    public String getUserAgent() {
        return data[USER_AGENT].toLowerCase().split("[/ ]")[0];
    }

    public String getServingMCC() {
        return data[SERVING_MCC];
    }
}

