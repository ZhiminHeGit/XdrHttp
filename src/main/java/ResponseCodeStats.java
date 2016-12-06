import java.io.Serializable;

public class ResponseCodeStats implements Serializable {
    int zero = 0;
    int information = 0;
    int success = 0;
    int redirection = 0;

    int client_error = 0;
    int server_error = 0;
    int other_error = 0;

    public static void main(String[] args) {
        System.out.println("400".startsWith("4"));
        System.out.println("500".startsWith("[45]"));
        System.out.println("100".startsWith("[45]"));
    }

    public void addCode(String code) {
        switch (code.charAt(0)) {
            case '0':
                zero++;
                break;
            case '1':
                information++;
                break;
            case '2':
                success++;
                break;
            case '3':
                redirection++;
                break;
            case '4':
                client_error++;
                break;
            case '5':
                server_error++;
                break;
            default:
                other_error++;
        }
    }

    public String toString() {
        double total = zero + information + success + redirection + client_error + server_error + other_error;
        int success_all = information + success + redirection;
        return (int) total + "," + zero + "," + String.format("%.4f", zero / total) + "," +
                success_all + "," + String.format("%.4f", success_all / total) + "," +
                client_error + "," + String.format("%.4f", client_error / total) + "," +
                server_error + "," + String.format("%.4f", server_error / total);
    }
}
