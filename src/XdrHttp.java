public class XdrHttp {
    private String type;
    private long date;
    private int duration;
    private long imsi, msisdn, imei;
    private int homeMcc, homeMnc, tac, servingMcc, servingMnc;
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

    public XdrHttp() {
        this.date = 0L;
        this.host = null;
        this.imei = 0L;
        this.msisdn = 0L;
        this.duration = 0;
        this.tai = this.ecgi = this.cellCI = this.cellLAC = 0;
        this.contentLength = 0L;
        this.userAgent = null;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public int getServingMcc() {
        return servingMcc;
    }

    public void setServingMcc(int servingMcc) {
        this.servingMcc = servingMcc;
    }

    public int getServingMnc() {
        return servingMnc;
    }

    public void setServingMnc(int servingMnc) {
        this.servingMnc = servingMnc;
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



}
