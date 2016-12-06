import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhiminhe on 11/3/16.
 */
public class MCCLookup {
    Map<Integer, String> mccLookupMap = new HashMap<>();
    public MCCLookup(String mccLookupFile) {
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(mccLookupFile));
            String line = bufferedReader.readLine();
            while(line!=null) {
                String[] parts = line.split(",");
                mccLookupMap.put(Integer.parseInt(parts[1]), parts[0]);
                line = bufferedReader.readLine();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    String lookup(Integer mcc) {
        return mccLookupMap.get(mcc);
    }
}
