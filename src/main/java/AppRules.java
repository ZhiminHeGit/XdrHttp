import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.Map;

public class AppRules {
    Map<String, String> cmiAppRules;
    Map<String, String> usrcAppRules;

    public AppRules() {
        cmiAppRules = getCMIAppRules();
        usrcAppRules = getUSRCAppRules();
    }

    public static void main(String[] args) {
        AppRules appRules = new AppRules();
        System.out.println(appRules.getCMIApp("365rili.com"));
        System.out.println(appRules.getUSRCApp("163.com"));
    }

    static Map<String, String> getCMIAppRules() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream("cmi_app.csv");
        Map<String, String> appRules = new LinkedHashMap();
        try {
            BufferedReader appRulesReader = new BufferedReader(new InputStreamReader(inputStream));
            String appListLine = appRulesReader.readLine();
            while (appListLine != null) {
                String[] matches = appListLine.split(",");
                appRules.put(matches[3], matches[0]);
                appListLine = appRulesReader.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return appRules;
    }

    static Map<String, String> getUSRCAppRules() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream("usrc_app_rules.txt");
        Map<String, String> appList = new LinkedHashMap();
        try {
            BufferedReader appRulesReader = new BufferedReader(new InputStreamReader(inputStream));
            String appListLine = appRulesReader.readLine();
            while (appListLine != null) {
                //  System.out.println(appListLine);
                String[] matches = appListLine.split(",");
                appList.put(matches[0], matches[1]);
                appListLine = appRulesReader.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return appList;
    }

    public String getCMIApp(String host) {
        if (host == null) return "HostIsNull";
        for (String key : cmiAppRules.keySet()) {
            if (host.contains(key)) {
                return cmiAppRules.get(key);
            }
        }
        return "NoMatch";
    }

    public String getUSRCApp(String line) {
        if (line == null) return "null";
        for (String key : usrcAppRules.keySet()) {
            if (line.contains(key)) {
                return usrcAppRules.get(key);
            }
        }
        return "unidentified";
    }
}
