import java.util.HashMap;
import java.util.Map;

/**
 * Created by usrc on 5/27/2016.
 */
public class AppProcess extends DailyProcess {
    AppRules appRules = new AppRules();
    HashMap<String, AppStats> appStatsMap = new HashMap();

    public static void main(String[] args) {
        AppProcess appProcess = new AppProcess();
        appProcess.process("/Volumes/DataDisk/GoldenWeek/201610010000-HTTP.csv",
                "/Volumes/DataDisk/Data/all_user_app_daily.csv",
                args);
    }

    public AppProcess() {
        headLine = "imsi,use_date,seving_mcc,fqdn_host,filtered_host,cmi_app_id,cmti_app,app_use_freq,content_length,duration,app_user_agent\n";

    }

    public boolean process(XdrHttp xdrHttp) {
        String appKey = xdrHttp.getImsi() + "," + xdrHttp.getReadableDate() + "," + xdrHttp.getMcc() + "," + xdrHttp.getHost() + "," +
                xdrHttp.getFilteredHost() + "," + appRules.getCMIApp(xdrHttp.getHost()) + "," +
                appRules.getUSRCApp(xdrHttp.getHost() + xdrHttp.getUserAgent());
        if (!appStatsMap.containsKey(appKey)) {
            appStatsMap.put(appKey, new AppStats());
        }
        appStatsMap.get(appKey).addAppUsage(xdrHttp);
        return true;
    }

    @Override
    public void writeOut(boolean print_to_screan) {
        String output;
        for (Map.Entry entry : appStatsMap.entrySet()
                ) {
            output = entry.getKey() + "," +
                    entry.getValue() + "\n";
            writer.write(output, print_to_screan);
        }
    }
}
