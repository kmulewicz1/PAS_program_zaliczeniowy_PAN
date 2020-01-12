import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import java.io.*;
import java.net.ServerSocket;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Server {
    static Vector<ClientHandler> vector = new Vector<>();
    static Stack<Card> cardStack = new Stack<>();
    static Card [] arrTab = new Card[24];
    static Map<String,Integer> ValueOfCardMAP = new HashMap<>();

    static boolean findInArray(Card[] arrTab, Card card) {
        for(int i=0;i<24;i++) {
            if(arrTab[i].getValue().equals(card.getValue())&&arrTab[i].getSuit().equals(card.getSuit()))
                return true;
        }
        return false;
    }

    private static void initMap()
    {
        ValueOfCardMAP.put("9",9);
        ValueOfCardMAP.put("10",10);
        ValueOfCardMAP.put("jack",11);
        ValueOfCardMAP.put("queen",12);
        ValueOfCardMAP.put("king",13);
        ValueOfCardMAP.put("ace",14);
    }

    private static void initTabOfCard()
    {
        for(int i=0;i<6;i++)
        {
            String value="";
            if(i==0) value="9";
            else if(i==1) value="10";
            else if(i==2) value="jack";
            else if(i==3) value="queen";
            else if(i==4) value="king";
            else  value="ace";

            arrTab[4*i]=new Card(value,"heart");
            arrTab[4*i+1]=new Card(value,"diamond");
            arrTab[4*i+2]=new Card(value,"club");
            arrTab[4*i+3]=new Card(value,"spade");
        }

    }
    private static void sendCards() throws Exception
    {
        int iterator=0;
        for (ClientHandler ch: Server.vector)
        {
            ch.Output.writeUTF("game start");
            ch.isReady=true;
            String cards="";
            for(int i=0;i<6;i++) {
                ch.listOfCard.add(arrTab[iterator + i]);
                cards=cards + arrTab[iterator + i].toString();

            }
            ch.Output.writeUTF("your cards: "+cards);

            //find who start
            Pattern pattern = Pattern.compile("9 heart");
            Matcher matcher = pattern.matcher(cards);
            if(matcher.find()) {
                ch.NineHeart = true;
                ch.Output.writeUTF("9heart");
            }
            iterator+=6;

        }
    }
    private static void shuffleCard()
    {
        Random rgen = new Random();

        for (int i=0; i<arrTab.length; i++) {
            int randomPosition = rgen.nextInt(arrTab.length);
            Card temp = arrTab[i];
            arrTab[i] = arrTab[randomPosition];
            arrTab[randomPosition] = temp;
        }
    }

    public static void main(String[] args) throws IOException {

        System.setProperty("javax.net.ssl.keyStore","za.store");
        System.setProperty("javax.net.ssl.keyStorePassword","qwerty123");
        SSLServerSocketFactory ssf = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
        ServerSocket sslServerSocket = ssf.createServerSocket(9000);

        boolean notification_start_game_send=false;

        initMap();
        initTabOfCard();
        shuffleCard();

        while(true) {
            SSLSocket clientSocket = (SSLSocket) sslServerSocket.accept();
            DataInputStream Input = new DataInputStream(clientSocket.getInputStream());
            DataOutputStream Output = new DataOutputStream(clientSocket.getOutputStream());
            ClientHandler clientHandler = new ClientHandler(Input, Output, clientSocket);

            if (vector.size() < 4) {
                Thread t = new Thread(clientHandler);
                vector.add(clientHandler);
                t.start();
            }
            if(vector.size()==4&&!notification_start_game_send) {
                notification_start_game_send=true;
                try {
                    sendCards();
                }
                catch ( Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }
}
