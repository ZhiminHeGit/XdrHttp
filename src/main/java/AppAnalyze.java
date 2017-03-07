import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class AppAnalyze {
    public  static  void main(String[] args) throws IOException {
        for(int i = 1001; i <= 1007; i++) {
            String file = "/Volumes/WDPassport/CMIData/2016" + i + "AppRank.txt";
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
            String line = bufferedReader.readLine();
            String[] titles = line.split(",");
            line = bufferedReader.readLine();
            while (line != null) {
                String[] data = line.split(",");


            }

        }

    }
}
