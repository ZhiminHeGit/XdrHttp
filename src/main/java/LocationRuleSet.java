import java.io.*;
import java.util.HashSet;
import java.util.Set;

public class LocationRuleSet {
    public Set<LocationRule> getLocationRuleSet() {
        return locationRuleSet;
    }

    Set<LocationRule> locationRuleSet = new HashSet();
    static public LocationRuleSet loadFromResource() {
        LocationRuleSet locationRuleSet = new LocationRuleSet();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream("location_rules");
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line = reader.readLine();
            // skip head
            line = reader.readLine();
            while (line != null) {
                locationRuleSet.locationRuleSet.add(new LocationRule(line));
                line = reader.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return locationRuleSet;
    }

    static public LocationRuleSet loadFromFile(String ruleFile) {
        LocationRuleSet locationRuleSet = new LocationRuleSet();
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(ruleFile));
            String line = bufferedReader.readLine();
            line = bufferedReader.readLine();
            while (line != null) {
                locationRuleSet.locationRuleSet.add(new LocationRule(line));
                line = bufferedReader.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return locationRuleSet;
    }
}
