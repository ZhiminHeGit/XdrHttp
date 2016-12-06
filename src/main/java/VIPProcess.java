import java.io.*;
import java.util.HashMap;

/**
 * Created by usrc on 5/18/2016.
 */
public class VIPProcess {
    public static void main(String[] args) throws IOException {

        AppRules appRules = new AppRules();

        File folder = new File("c:/xdr_http/vips/");
        File[] listOfFiles = folder.listFiles();

        String http_user_app_daily_file = "/home/jianli/http_user_app_daily.csv";
        String http_user_ct_daily_file = "c:/xdr_http/http_user_ct_daily.csv";
        String http_user_rc_daily_file = "c:/xdr_http/http_user_daily.csv";

        BufferedWriter http_user_app_daily_bw =
                new BufferedWriter(new OutputStreamWriter(
                        new FileOutputStream(http_user_app_daily_file)));

        BufferedWriter http_user_ct_daily_bw =
                new BufferedWriter(new OutputStreamWriter(
                        new FileOutputStream(http_user_ct_daily_file)));

        BufferedWriter http_user_rc_daily_bw =
                new BufferedWriter(new OutputStreamWriter(
                        new FileOutputStream(http_user_rc_daily_file)));


        String output = "imsi,use_date,seving_mcc,fqdn_host,filtered_host,cmi_app_id,cmti_app,app_use_freq,content_length,duration,app_user_agent\n";
        http_user_app_daily_bw.write(output);
        System.out.print(output);
        output = "imsi,use_date,content_type,num_request,durationMS,content_length,num_user_agent,number_filtered_host\n";
        http_user_ct_daily_bw.write(output);
        output = "imsi,use_date,success,failure,success_rate\n";
        http_user_rc_daily_bw.write(output);


        for (File file : listOfFiles) {
            if (file.isFile()) {
                String imsi = file.getName().substring(0, file.getName().lastIndexOf('.'));
                System.out.println(file.getName());
                BufferedReader br = new BufferedReader(new FileReader(file));
                // skip headLine
                String line = br.readLine();

                line = br.readLine();
                HashMap<String, AppStats> appStatsMap = new HashMap();
                HashMap<String, ContentTypeStats> ctStatsMap = new HashMap();
                HashMap<String, ResponseCodeStats> rcStatsMap = new HashMap();
                while (line != null) {
                    VipLine vipLine = new VipLine(line);
                    String appKey = imsi + "," + vipLine.getDate() + "," + vipLine.getServingMCC() + "," + vipLine.getHost() + "," +
                            vipLine.getFilteredHost() + "," + appRules.getCMIApp(vipLine.getHost()) + "," + appRules.getUSRCApp(line);
                    if (!appStatsMap.containsKey(appKey)) {
                        appStatsMap.put(appKey, new AppStats());
                    }
                    appStatsMap.get(appKey).addAppUsage(vipLine);

                    String ctKey = imsi + "," + vipLine.getDate() + "," + vipLine.getContentType();
                    if (!ctStatsMap.containsKey(ctKey)) {
                        ctStatsMap.put(ctKey, new ContentTypeStats());
                    }
                    ctStatsMap.get(ctKey).addCtUsage(vipLine);

                    String rcKey = imsi + "," + vipLine.getDate();
                    if (!rcStatsMap.containsKey(rcKey)) {
                        rcStatsMap.put(rcKey, new ResponseCodeStats());

                    }
                    rcStatsMap.get(rcKey).addCode(vipLine.getResponseCode());
                    // http_user_rc_daily_bw.write(imsi + ","  + "\n");
                    line = br.readLine();
                }
                writeAppStats(http_user_app_daily_bw, appStatsMap);

                writeCtStats(http_user_ct_daily_bw, ctStatsMap);

                writeRcStats(http_user_rc_daily_bw, rcStatsMap);

            }
            break;
        }
        http_user_app_daily_bw.close();
        http_user_ct_daily_bw.close();
        http_user_rc_daily_bw.close();
    }

    public static void writeRcStats(BufferedWriter http_user_rc_daily_bw, HashMap<String, ResponseCodeStats> rcStatsMap) throws IOException {
        String output;
        for (String key : rcStatsMap.keySet()) {
            output = key + "," + rcStatsMap.get(key) + "\n";
            //    System.out.print(output);
            http_user_rc_daily_bw.write(output);
        }
    }

    public static void writeCtStats(BufferedWriter http_user_ct_daily_bw, HashMap<String, ContentTypeStats> ctStatsMap) throws IOException {
        String output;
        for (String key : ctStatsMap.keySet()) {
            output = key + "," + ctStatsMap.get(key) + "\n";
            //    System.out.print(output);
            http_user_ct_daily_bw.write(output);
        }
    }

    public static void writeAppStats(BufferedWriter http_user_app_daily_bw, HashMap<String, AppStats> appStatsMap) throws IOException {
        String output;
        for (String key : appStatsMap.keySet()
                ) {
            output = key + "," +
                    appStatsMap.get(key) + "\n";
            System.out.print(output);
            http_user_app_daily_bw.write(output);
        }
    }


    public static String outputStatsMap(HashMap<String, ItemStats> statsMap) {

        int count = 1;
        String output = "";
        for (ItemStats itemStats
                : Main.sortByValueReversed(statsMap).values()
                ) {

            if (count > 3) {
                break;
            }
            output = output + "," + itemStats;
            count++;
        }
        return output;
    }

    public static void updateStatsMap(HashMap<String, ItemStats> statsMap, VipLine vipLine, String item) {
        if (!statsMap.containsKey(item)) {
            statsMap.put(item, new ItemStats(item));
        }
        statsMap.get(item).addDurationAndLength(vipLine.getDurationMS(), vipLine.getContentLength());
    }
}
