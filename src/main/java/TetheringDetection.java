import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

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
        Map<Long, HashSet<String>> imisPhoneTypeMap = new HashMap();
        Map<Long, HashSet<Long>> imisImeiMap = new HashMap();
        HashMap<String, Long> agentCount = new HashMap();
        int n_records = 0;
        for (File file : listOfFiles) {

            if (file.isFile()) {
                System.out.println(file.getName());
                BufferedReader br = new BufferedReader(new FileReader(file));
                String line = br.readLine();
                while (line != null) {
                    XdrHttp xdrHttp = XdrHttp.parse(line);
                    if (xdrHttp.getImsi() != 0 && xdrHttp.getMsisdn() != 0) {
                        long imsi = xdrHttp.getImsi();
                        if (!imisImeiMap.containsKey(imsi)) {
                            imisImeiMap.put(imsi, new HashSet());
                        }
                        imisImeiMap.get(imsi).add(xdrHttp.getImei());
                        String userAgent = xdrHttp.getUserAgent();
                        if (userAgent != null) {
                            String appAndHost = userAgent.split("[/, ;]")[0];
                            String phoneType = "other";
                            if (userAgent.contains(IPHONE)) {
                                phoneType = IPHONE;
                            }
                            if (userAgent.contains(ANDROID)) {
                                phoneType = ANDROID;
                            }
                            appAndHost = appAndHost + ":" + xdrHttp.getHost();
                            if (!agentCount.containsKey(appAndHost)) {
                                agentCount.put(appAndHost, 0L + xdrHttp.getDuration());
                            }
                            agentCount.put(appAndHost, agentCount.get(appAndHost) + xdrHttp.getDuration());
                            if (!imisPhoneTypeMap.containsKey(imsi)) {
                                imisPhoneTypeMap.put(imsi, new HashSet());
                            }
                            imisPhoneTypeMap.get(imsi).add(userAgent);
                        }
                    }
                    line = br.readLine();
                    n_records++;
                }
            }
            System.out.println(n_records);

        }
        for (long imsi : imisImeiMap.keySet()) {
            if (imisImeiMap.get(imsi).size() > 1) {
                System.out.println("imsi:" + imsi);
                System.out.println("imei:" + imisImeiMap.get(imsi));
            }
        }
        Main.writeToFileInValueOrder("C:/xdr_http/ranked_agents_time.txt", agentCount, true);
        // PrintWriter out = new PrintWriter("C:/xdr_http/ranked_agents.txt");
        // for (long imsi: imisPhoneTypeMap.keySet()
        //      ) {
            /*HashSet<String> phoneTypes = new HashSet<>();
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
                    out.println("imsi:" + imsi);
                    for (String s:imisPhoneTypeMap.get(imsi)){
                        out.println(s);
                    }
                    break;
                }
            }*/
        //   out.println("imsi:" + imsi);
        //   for (String s:imisPhoneTypeMap.get(imsi)){
        //       out.println("agent:" + s);
        // }
        //}
    }
}
