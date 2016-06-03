import java.util.HashMap;

/**
 * Created by usrc on 5/27/2016.
 */
public class ResponseCodeProcess extends DailyProcess {
    HashMap<String, ResponseCodeStats> responseCodeStatsHashMap = new HashMap();

    public ResponseCodeProcess() {
        headLine = "imsi,date,serving_mcc,host,tai,ecgi,cell_ac,cell_ci,total_requests,zero,zero_rate,success,success_rate," +
                "client_error,client_error_rate,server_error,server_error_rate\n";
    }

    @Override
    public void process(XdrHttp xdrHttp) {
        String responseCodeKey = xdrHttp.getImsi() + "," + xdrHttp.getReadableDate() + "," +
                xdrHttp.getServingMcc() + "," + xdrHttp.getHost() + "," + xdrHttp.getTai() + ","
                + xdrHttp.getEcgi() + "," + xdrHttp.getCellLAC() + "," + xdrHttp.getCellCI();
        if (!responseCodeStatsHashMap.containsKey(responseCodeKey)) {
            responseCodeStatsHashMap.put(responseCodeKey, new ResponseCodeStats());
        }
        responseCodeStatsHashMap.get(responseCodeKey).addCode(Integer.toString(xdrHttp.getResponoseCode()));
    }

    @Override
    public void writeOut(DailyWriter dailyWriter, boolean print_to_screen) {
        String output;
        for (String key : responseCodeStatsHashMap.keySet()) {
            output = key + "," + responseCodeStatsHashMap.get(key) + "\n";
            dailyWriter.write(output, print_to_screen);
        }
    }
}
