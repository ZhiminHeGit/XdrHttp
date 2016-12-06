import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhiminhe on 11/7/16.
 */
public class PhoneTypeLookup {
    Map<Long, String> phoneTypeLookupMap = new HashMap<>();
    public PhoneTypeLookup(String phoneTypeLookupFile) {
        String line = "";
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(phoneTypeLookupFile));
            line = bufferedReader.readLine();
            line = bufferedReader.readLine();
            while(line!=null) {
                String[] parts = line.split("\\|");
                if (parts.length >= 2) {
                    phoneTypeLookupMap.put(Long.parseLong(parts[0]), parts[1]);
                }
                line = bufferedReader.readLine();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    String lookup(Long prefix) {
        return phoneTypeLookupMap.get(prefix);
    }

    static public void main(String[] args) {
        PhoneTypeLookup phoneTypeLookup =
                new PhoneTypeLookup("/Volumes/DataDisk/Data/cmi_client_dim.csv");
        System.out.println(phoneTypeLookup.lookup(35279903L));


    }
}
