import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * Created by usrc on 5/27/2016.
 */
public class DailyWriter {
    BufferedWriter bufferedWriter;
    String headLine;

    public DailyWriter(String filename) {
        try {
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(filename)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void write(String line, boolean print) {
        if (print) {
            System.out.print(line);
        }
        try {
            bufferedWriter.write(line);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setHeadLine(String headLine) {
        this.headLine = headLine;
    }
}
