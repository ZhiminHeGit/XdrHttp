import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class XdrHttp {
    private String type;
    private long date;
    private int duration;
    private long imsi, msisdn, imei;
    private int homeMcc, homeMnc, tac, mcc, mnc;
    private int ratType;
    private String apn;
    private int tai, ecgi, cellLAC, cellCI;
    private String userIP, serverIP;
    private int userPort, serverPort;
    private String httpMethod;
    private int responoseCode;
    private String host;
    private String uriData, contentType;
    private long contentLength;
    private String userAgent;
    private String sgsnIP, sgwIP;
    private String formattedDateTime;
    static private final SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    {
        format.setTimeZone(TimeZone.getTimeZone("Asia/Hong_Kong"));
    }
    private XdrHttp() {
    }

    public static boolean notEmpty(String string) {
        return string != null && !string.isEmpty() && !string.equalsIgnoreCase("NULL");
    }

    public static XdrHttp parse(String line) {
        return parse(line, ",");
    }

    public static XdrHttp parse(String line, String separator) {
        XdrHttp xdrHttp = new XdrHttp();
        if (!notEmpty(line)) {
            throw new RuntimeException("line is empty");
        }
        try {
            String[] parts = line.split(separator);

            if (notEmpty(parts[1])) {
                // the date format is "start time in UTC with msec, such as 1456088339.964
                xdrHttp.setDate((long) (Float.parseFloat(parts[1]) * 1000));
                xdrHttp.formattedDateTime = format.format(new Date(xdrHttp.getDate()));
            }
            if (notEmpty(parts[2])) {
//                xdrHttp.setDuration(Integer.parseInt(parts[2]));
            }

            if (notEmpty(parts[3]))
                xdrHttp.setImsi(Long.parseLong(parts[3]));
            if (notEmpty(parts[4]))
                xdrHttp.setMsisdn(Long.parseLong(parts[4]));
            if (notEmpty(parts[5]))
                xdrHttp.setImei(Long.parseLong(parts[5]));

            if (notEmpty(parts[6])) {
                xdrHttp.setHomeMcc(Integer.parseInt(parts[6]));
            }
            if (notEmpty(parts[7])) {
                xdrHttp.setHomeMnc(Integer.parseInt(parts[7]));
            }
            if (notEmpty(parts[8])) {
                xdrHttp.setTac(Integer.parseInt(parts[8]));
            }
            if (notEmpty(parts[9])) {
                xdrHttp.setMcc(Integer.parseInt(parts[9]));
            }
            if (notEmpty(parts[10])) {
                xdrHttp.setMnc(Integer.parseInt(parts[10]));
            }
            if (notEmpty(parts[13]))
                xdrHttp.setTai(Integer.parseInt(parts[13]));
            if (notEmpty(parts[14]))
                xdrHttp.setEcgi(Integer.parseInt(parts[14]));
            if (notEmpty(parts[15]))
                xdrHttp.setCellLAC(Integer.parseInt(parts[15]));
            if (notEmpty(parts[16]))
                xdrHttp.setCellCI(Integer.parseInt(parts[16]));
            if (notEmpty(parts[22]))
                xdrHttp.setResponoseCode(Integer.parseInt(parts[22]));

            if (notEmpty(parts[23]))
                xdrHttp.setHost(parts[23].toLowerCase());

            if (notEmpty(parts[26]))
                xdrHttp.setContentLength(Long.parseLong(parts[26]));

            if (notEmpty(parts[27])) {
                xdrHttp.setUserAgent(parts[27].toLowerCase());
            }
            ;
        } catch (Exception e) {
            System.out.println(line);
            e.printStackTrace();
            System.exit(0);
            return null;
        }
        return xdrHttp;
    }


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getReadableDate() {
        Date date = new Date(getDate());

        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
        return formatter.format(date);
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
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

    public int getRatType() {
        return ratType;
    }

    public void setRatType(int ratType) {
        this.ratType = ratType;
    }

    public String getApn() {
        return apn;
    }

    public void setApn(String apn) {
        this.apn = apn;
    }

    public int getTai() {
        return tai;
    }

    public void setTai(int tai) {
        this.tai = tai;
    }

    public int getEcgi() {
        return ecgi;
    }

    public void setEcgi(int ecgi) {
        this.ecgi = ecgi;
    }

    public int getCellLAC() {
        return cellLAC;
    }

    public void setCellLAC(int cellLAC) {
        this.cellLAC = cellLAC;
    }

    public int getCellCI() {
        return cellCI;
    }

    public void setCellCI(int cellCI) {
        this.cellCI = cellCI;
    }

    public String getUserIP() {
        return userIP;
    }

    public void setUserIP(String userIP) {
        this.userIP = userIP;
    }

    public String getServerIP() {
        return serverIP;
    }

    public void setServerIP(String serverIP) {
        this.serverIP = serverIP;
    }

    public int getUserPort() {
        return userPort;
    }

    public void setUserPort(int userPort) {
        this.userPort = userPort;
    }

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public int getResponoseCode() {
        return responoseCode;
    }

    public void setResponoseCode(int responoseCode) {
        this.responoseCode = responoseCode;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getUriData() {
        return uriData;
    }

    public void setUriData(String uriData) {
        this.uriData = uriData;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public long getContentLength() {
        return contentLength;
    }

    public void setContentLength(long contentLength) {
        this.contentLength = contentLength;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getSgsnIP() {
        return sgsnIP;
    }

    public void setSgsnIP(String sgsnIP) {
        this.sgsnIP = sgsnIP;
    }

    public String getSgwIP() {
        return sgwIP;
    }

    public void setSgwIP(String sgwIP) {
        this.sgwIP = sgwIP;
    }

    public String getFilteredHost() {
        return Main.consolidateFQDN(getHost());
    }

    public String getFormattedDateTime() {
        return formattedDateTime;
    }
}
