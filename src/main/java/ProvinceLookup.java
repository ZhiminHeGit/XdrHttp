import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhiminhe on 11/3/16.
 */
public class ProvinceLookup {
    Map<Long, String> provinceLookupMap = new HashMap<>();
    public ProvinceLookup(String proviceLookupFile) {
        String line = "";
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(proviceLookupFile));
            line = bufferedReader.readLine();
            line = bufferedReader.readLine();
            while(line!=null) {
                String[] parts = line.split(",");
                if (parts.length >= 2) {
                    provinceLookupMap.put(Long.parseLong(parts[0]), parts[1]);
                }
                line = bufferedReader.readLine();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    String lookup(Long prefix) {
        return provinceLookupMap.get(prefix);
    }
}
