/**
 * Created by zhiminhe on 1/28/17.
 */
public class Airport {
        String[] parts;
        String raw;
        GPS gps;

        public Airport(String line) {
            raw = line;
            parts = line.split(",");
            gps = new GPS(Double.parseDouble(parts[6]),
                    Double.parseDouble(parts[7]));
        }
        public String getCityAndCountry() {
            return "\"" + parts[2] + "," + parts[3] + "\"";
        }

        public GPS getGPS() {
            return gps;
        }
};

