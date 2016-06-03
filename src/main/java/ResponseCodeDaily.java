/**
 * Created by usrc on 5/27/2016.
 */

// java -cp XdrHttp-1.0-SNAPSHOT.jar ResponseCodeDaily  /Users/jianli/Downloads/cmidata/xdr_http/raw/http0501.csv /root/all_user_rc_daily.csv
public class ResponseCodeDaily {
    public static void main(String[] args) {
        Daily daily = new Daily();
        ResponseCodeProcess responseCodeProcess = new ResponseCodeProcess();
        daily.process("C:\\xdr_http\\51sample.csv", "\b",
                "C:\\xdr_http\\all_user_rc_daily.csv",
                args, responseCodeProcess);
    }
}
