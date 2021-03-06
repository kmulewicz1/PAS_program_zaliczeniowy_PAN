import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.util.Scanner;


public class Client {
    private static Scanner sc= new Scanner(System.in);
    private static final boolean[] IsMyRound = {false};
    private static final boolean[] Is9Heart = {false};
    private static final boolean[] IsEnd = {false};
    private static DataOutputStream Output;
    private static DataInputStream Input;

    private static void init() throws IOException{
        System.setProperty("javax.net.ssl.trustStore","za.store");
        SSLSocketFactory sslSocketFactory =
                (SSLSocketFactory)SSLSocketFactory.getDefault();
        SSLSocket socket = (SSLSocket) sslSocketFactory.createSocket("localhost", 9000);
        socket.startHandshake();

        Output = new DataOutputStream(socket.getOutputStream());
        Input = new DataInputStream(socket.getInputStream());
    }

    public static void main(String[] args) throws IOException {

        init();
        // sendMessage thread
        Thread sendMessage = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    // read the message to deliver.
                    String msg = sc.nextLine();
                    try
                    {
                        if(!IsMyRound[0]&&!Is9Heart[0])
                            System.out.println("It's not your round");
                        else
                        {
                            if(msg.length()<4)
                                System.out.println("too short message");
                            else
                            {
                                String msg_tmp=msg.substring(0,4);
                                switch (msg_tmp) {
                                    case "quit":
                                        Output.writeUTF(msg);

                                        break;
                                    case "send":
                                        if (msg.length() < 6)
                                            System.out.println("wrong cards in 'send'");
                                        else
                                            if(Is9Heart[0]) {
                                                if (msg.length() >= 12) {
                                                    if (msg.substring(0, 12).equals("send 9 heart"))
                                                        Output.writeUTF(msg);
                                                    else System.out.println("you must send 9 heart");
                                                }// if length >=12
                                                else System.out.println("you must send 9 heart");
                                            }
                                            else {
                                                Output.writeUTF(msg);
                                            }
                                        break;
                                    case "wait":
                                       caseWait(msg);
                                        break;
                                    default:
                                        System.out.println("invalid message");
                                        break;
                                }//switch

                            }//else
                        }//else
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        // readMessage thread
        Thread readMessage = new Thread(new Runnable()
        {
            @Override
            public void run() {
                while (true) {
                    try {
                        // read the message sent to this client
                        if(!IsEnd[0]) {
                            String msg = Input.readUTF();
                            System.out.println(msg);
                            switch (msg) {
                                case "your round":
                                    IsMyRound[0] = true;
                                    System.out.println("Write 'send' a cards what do you want to put," +
                                            " separate them with a comma or enter 'wait' ");
                                    break;
                                case "9heart":
                                    IsMyRound[0] = true;
                                    Is9Heart[0] = true;
                                    System.out.println("Write 'send' a cards what do you want to put," +
                                            " separate them with a comma, you must put the card" +
                                            "because you have 9heart");
                                    break;
                                case "it's your card: ":
                                    IsMyRound[0] = false;
                                    break;
                                case "game end":
                                    System.out.println("one player left");
                                    System.exit(0);
                                    IsEnd[0] = true;
                                case "end":
                                    System.exit(0);
                                    IsEnd[0] = true;
                                case "we have a looser":
                                    System.exit(0);
                                    IsEnd[0] = true;
                                case "Welcome in PAN game":
                                    System.out.println("You must wait for all players");
                                    break;
                                case "OK, wait for next round":
                                    Is9Heart[0] = false;
                                    IsMyRound[0] = false;
                                    break;
                            }
                        }
                        else {
                            return;
                        }
                    }
                        catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        sendMessage.start();
        readMessage.start();

    }
    private static void caseWait( String msg) throws IOException{
        if (Is9Heart[0])
            System.out.println("you must send cards");
        else
            Output.writeUTF(msg);
    }
}
