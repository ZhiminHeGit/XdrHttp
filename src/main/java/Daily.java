import java.io.BufferedReader;
import java.io.IOException;
import java.util.Date;

/**
 * Created by usrc on 5/26/2016.
 */

public class Daily {
    public void process(String input, String separator, String output, String[] args, DailyProcess process) {
        System.out.println(new Date() + " start processing");
        DailyReaderWriter dailyReaderWriter = new DailyReaderWriter(
                input, output,
                args);
        try {
            DailyWriter dailyWriter = dailyReaderWriter.getWriter();
            dailyWriter.write(process.getHeadLine(), false);
            BufferedReader bufferedReader = dailyReaderWriter.getReader();
            String line = bufferedReader.readLine();
            int count = 0;
            while (line != null) {
                count++;
                if (count % 100000 == 0) {
                    System.out.println(new Date() + " " + count);
                }
                XdrHttp xdrHttp = XdrHttp.parse(line, separator);
                if (xdrHttp != null) {
                    process.process(xdrHttp);
                }
                line = bufferedReader.readLine();
            }
            process.writeOut(dailyWriter, false);
            dailyWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(new Date() + " finish processing");
    }
}
