import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * Created by usrc on 5/16/2016.
 */
public class TetheringDetection {
    private static final String IPHONE = "iphone";
    private static final String ANDROID = "android";
    private static final String WINDOWS = "windows";
    public static void main(String args[]) throws IOException {
            File folder = new File("c:/xdr_http/20160221/");
            File[] listOfFiles = folder.listFiles();
            Map<Long,HashSet<String>> imisPhoneTypeMap = new HashMap<>();
            for (File file : listOfFiles) {
                if (file.isFile()) {
                    System.out.println(file.getName());
                    InputStream fileStream = new FileInputStream(file);
                    InputStream gzipStream = new GZIPInputStream(fileStream);
                    Reader decoder = new InputStreamReader(gzipStream, "UTF-8");
                    BufferedReader br = new BufferedReader(decoder);
                    String line = br.readLine();
                    while (line != null) {
                        XdrHttp xdrHttp = Main.parseXdrHttp(line);
                        if (xdrHttp.getImsi() != 0 && xdrHttp.getMsisdn() != 0) {
                            long imsi = xdrHttp.getImsi();
                            String userAgent = xdrHttp.getUserAgent();
                            if (userAgent != null &&
                                    (userAgent.contains(IPHONE) || userAgent.contains(ANDROID) ||
                                            userAgent.contains(WINDOWS))) {
                                if(!imisPhoneTypeMap.containsKey(imsi)) {
                                    imisPhoneTypeMap.put(imsi, new HashSet<>());
                                }
                                imisPhoneTypeMap.get(imsi).add(xdrHttp.getDate() + ":" + xdrHttp.getUserIP() +
                                        ":" + xdrHttp.getHost() + ":" + userAgent);
                            }
                        }
                        line = br.readLine();
                    }

                }
            }
        for (long imsi: imisPhoneTypeMap.keySet()
             ) {
            HashSet<String> phoneTypes = new HashSet<>();
            for (String userAgent:imisPhoneTypeMap.get(imsi)) {
                if (userAgent.contains(IPHONE)) {
                    phoneTypes.add(IPHONE);
                }
                if (userAgent.contains(ANDROID)) {
                    phoneTypes.add(ANDROID);
                }
                if (userAgent.contains(WINDOWS)) {
                    phoneTypes.add(WINDOWS);
                }
                if (phoneTypes.size() > 1) {
                    System.out.println(imsi);

                    for (String s:imisPhoneTypeMap.get(imsi)){

                    System.out.println(s);

                    }
                    break;
                }
            }
        }
    }
}

