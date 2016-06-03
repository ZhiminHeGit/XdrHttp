import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by usrc on 5/25/2016.
 */
public class BS2GPSProcess extends DailyProcess {
    Map<String, String> baseStationMap = new HashMap();
    int new_lookup = 0;
    int found_existing = 0;
    int no_base_station = 0;
    int found = 0;
    int not_found = 0;
    int non_460_base_station = 0;

    public BS2GPSProcess() {
        headLine = "tobeset\n";
    }

    public String getGooglePosition(BaseStation baseStation) {
        String url_string = "http://api.gpsspg.com/bs/?oid=2713" +
                "&key=226A5BA9628C309E89D2A645E0853DAA&bs=%d,%d,%d,%d" +
                "&to=2&output=json";
        return "";
    }

    public String getGpsspgPosition(BaseStation baseStation) {

        String url_string = "http://api.gpsspg.com/bs/?oid=2713" +
                "&key=226A5BA9628C309E89D2A645E0853DAA&bs=%d,%d,%d,%d" +
                "&to=2&output=json";

        url_string = String.format(url_string, baseStation.mcc, baseStation.mnc, baseStation.v1, baseStation.v2);

        //    System.out.println(url_string);
        try {
            URL url = new URL(url_string);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();

            int responseCode = con.getResponseCode();
            //System.out.println("\nSending 'GET' request to URL : " + url_string);
            // System.out.println("Response Code : " + responseCode);

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            //print result
            System.out.println(response.toString());
            return response.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "failed";
    }

    @Override
    public void process(XdrHttp xdrHttp) {
        BaseStation baseStation = null;
        if (xdrHttp.getServingMcc() == 310) {
            if (xdrHttp.getCellLAC() != 0 && xdrHttp.getCellCI() != 0) {
                baseStation = new BaseStation(0, xdrHttp.getServingMcc(), xdrHttp.getServingMnc(), xdrHttp.getCellLAC(), xdrHttp.getCellCI());
            } else if (xdrHttp.getTai() != 0 && xdrHttp.getEcgi() != 0) {
                baseStation = new BaseStation(0, xdrHttp.getServingMcc(), xdrHttp.getServingMnc(), xdrHttp.getTai(), xdrHttp.getEcgi());
            } else {
                no_base_station++;
            }
        } else {
            non_460_base_station++;
        }
        //baseStation = new BaseStation(3700, 2934274);
        if (baseStation != null) {
            if (!baseStationMap.containsKey(baseStation.toString())) {
                //System.out.print(baseStation);
                //String position = getGpsspgPosition(baseStation);
                /*if (position.contains(":200,")) {
                    System.out.println(":found");
                    ++found;
                } else {
                    System.out.println(":not found");
                    not_found++;
                }*/
                new_lookup++;
                baseStationMap.put(baseStation.toString(), "");
            } else {
                found_existing++;
            }
        }
    }

    @Override
    public void writeOut(DailyWriter dailyReaderWriter, boolean print_to_screan) {
        System.out.println("new look up :" + new_lookup);
        System.out.println("found:" + found + " not found:" + not_found);
        System.out.println("found_exsiting :" + found_existing);
        System.out.println("no_base_station :" + no_base_station);
        System.out.println("non_460_base_station :" + non_460_base_station);
    }

    class BaseStation {
        int type;
        int mcc;
        int mnc;
        int v1;
        int v2;

        public BaseStation(int type, int mcc, int mnc, int v1, int v2) {
            this.type = type;
            this.v1 = v1;
            this.v2 = v2;
            this.mcc = mcc;
            this.mnc = mnc;
        }

        public String toString() {
            return type + ":" + mcc + ";" + mnc + ";" + v1 + ";" + v2;
        }
    }
}

