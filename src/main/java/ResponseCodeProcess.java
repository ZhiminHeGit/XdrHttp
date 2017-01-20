import java.util.HashMap;
import java.util.Map;

/**
 * Created by usrc on 5/27/2016.
 */
public class ResponseCodeProcess extends DailyProcess {
    HashMap<String, ResponseCodeStats> responseCodeStatsHashMap = new HashMap();

    public static void main(String[] args) {
        ResponseCodeProcess responseCodeProcess = new ResponseCodeProcess();
        responseCodeProcess.process(
                "/Volumes/DataDisk/GoldenWeek/201610010000-HTTP.csv",
                "/Volumes/DataDisk/Data/all_user_rc_daily.csv", args);
    }

    public ResponseCodeProcess() {
        headLine = "imsi,date,serving_mcc,host,tai,ecgi,cell_ac,cell_ci,total_requests,zero,zero_rate,success,success_rate," +
                "client_error,client_error_rate,server_error,server_error_rate\n";
    }

    @Override
    public boolean process(XdrHttp xdrHttp) {
        String responseCodeKey = xdrHttp.getImsi() + "," + xdrHttp.getReadableDate() + "," +
                xdrHttp.getMcc() + "," + xdrHttp.getHost() + "," + xdrHttp.getTai() + ","
                + xdrHttp.getEcgi() + "," + xdrHttp.getCellLAC() + "," + xdrHttp.getCellCI();
        if (!responseCodeStatsHashMap.containsKey(responseCodeKey)) {
            responseCodeStatsHashMap.put(responseCodeKey, new ResponseCodeStats());
        }
        responseCodeStatsHashMap.get(responseCodeKey).addCode(Integer.toString(xdrHttp.getResponoseCode()));
        return true;
    }

    @Override
    public void writeOut(boolean print_to_screen) {
        String output;
        for (Map.Entry entry: responseCodeStatsHashMap.entrySet()) {
            output = entry.getKey() + "," + entry.getValue() + "\n";
            writer.write(output, print_to_screen);
        }
    }
}
