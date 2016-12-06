/**
 * Created by usrc on 5/23/2016.
 */
public class ItemStats implements Comparable<ItemStats> {
    String item;
    int count = 0;
    int duration = 0;
    int content_length = 0;

    public ItemStats(String the_app) {
        item = the_app;
    }

    public int getCount() {
        return count;
    }

    public int getDuration() {
        return duration;
    }

    public int getContent_length() {
        return content_length;
    }

    public String getItem() {
        return item;
    }

    public void addDurationAndLength(int d, int cl) {
        count++;
        duration += d;
        content_length += cl;
    }

    public String toString() {
        return item + ";" + count + ";" + duration + ";" + content_length;
    }

    @Override
    public int compareTo(ItemStats o) {
        return content_length - o.getContent_length();
    }

}
