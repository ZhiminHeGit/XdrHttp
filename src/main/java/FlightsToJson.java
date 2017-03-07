import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by zhiminhe on 1/29/17.
 */
public class FlightsToJson {
    public static void main(String[] args) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader("/Volumes/DataDisk/Data/1001flights.txt"));
            String line = reader.readLine();
            System.out.println("[\\");
            while(line != null) {
                String[] parts = line.split(":");
                System.out.print(
                        String.format("[%s, %s, %s]" ,parts[0], parts[1], parts[2]));
                line = reader.readLine();
                if (line != null) {
                    System.out.println(",\\");
                } else {
                    System.out.println("]");
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
