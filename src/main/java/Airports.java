import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**


 0 Airport ID	Unique OpenFlights identifier for this airport.
 1 Name	Name of airport. May or may not contain the City name.
 2 City	Main city served by airport. May be spelled differently from Name.
 3 Country	Country or territory where airport is located. See countries.dat to cross-reference to ISO 3166-1 codes.
 4 IATA	3-letter IATA code. Null if not assigned/unknown.
 5 ICAO	4-letter ICAO code. Null if not assigned.
 6 Latitude	Decimal degrees, usually to six significant digits. Negative is South, positive is North.
 7 Longitude	Decimal degrees, usually to six significant digits. Negative is West, positive is East.
 8 Altitude	In feet.
 9 Timezone	Hours offset from UTC. Fractional hours are expressed as decimals, eg. India is 5.5.
 10 DST	Daylight savings time. One of E (Europe), A (US/Canada), S (South America), O (Australia), Z (New Zealand), N (None) or U (Unknown). See also: Help: Time
 11 Tz database time zone	Timezone in "tz" (Olson) format, eg. "America/Los_Angeles".
 12 Type	Type of the airport. Value "airport" for air terminals, "station" for train stations, "port" for ferry terminals and "unknown" if not known. In airports.csv, only type=airport is included.
 13 Source	Source of this data. "OurAirports" for data sourced from OurAirports, "Legacy" for old data not matched to OurAirports (mostly DAFIF), "User" for unverified user contributions. In airports.csv, only source=OurAirports is included.
*/

 public class Airports {
    List<Airport> airportList = new ArrayList<Airport>();

    public static void main(String[] args) {

    }
    public Airport lookup(GPS gps) {
        double min_distance = 100;
        Airport closet_airport = null;
        for(Airport airport: airportList) {
            double distance = GPS.getTravelDistance(gps, airport.getGPS());
            if (distance < min_distance ) {
                closet_airport = airport;
                min_distance = distance;
            }
        }
        return closet_airport;
    }


    public Airports() {
        String airportFile = "/Volumes/DataDisk/Data/airports.csv";
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new FileReader(airportFile));
            // remove headline
            String line = bufferedReader.readLine();
            while (line != null) {
                if (! line.toLowerCase().contains("station")) {
                    Airport airport = new Airport(line);
                    airportList.add(airport);
                }
                line = bufferedReader.readLine();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
 }

