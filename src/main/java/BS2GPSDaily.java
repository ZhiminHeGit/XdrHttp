/**
 * Created by usrc on 5/30/2016.
 */

// java -cp XdrHttp-1.0-SNAPSHOT.jar BS2GPSDaily  /Users/jianli/Downloads/cmidata/xdr_http/raw/http0501.csv /root/all_user_bs_daily.csv
public class BS2GPSDaily {
    public static void main(String[] args) {
        Daily daily = new Daily();
        BS2GPSProcess bs2GPSProcess = new BS2GPSProcess();
        daily.process("C:\\xdr_http\\20160221\\20160221-870-HTTP.csv", ",",
                "C:\\xdr_http\\all_user_bs_daily.csv",
                args, bs2GPSProcess);
    }
}
