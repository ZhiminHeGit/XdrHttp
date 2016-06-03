/**
 * Created by usrc on 5/27/2016.
 */
public abstract class DailyProcess {
    String headLine;

    abstract public void process(XdrHttp xdrHttp);

    public abstract void writeOut(DailyWriter dailyReaderWriter, boolean print_to_screan);

    public String getHeadLine() {
        return headLine;
    }

}
