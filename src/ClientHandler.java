import javax.net.ssl.SSLSocket;
import java.io.*;
import java.util.*;


public class ClientHandler implements Runnable{

    private DataInputStream Input;
    private SSLSocket s;
    private boolean myRound;
    private boolean isWinner;
    private boolean isEnd;
    boolean isReady;
    boolean NineHeart;
    LinkedList <Card> listOfCard;
    DataOutputStream Output;

    ClientHandler(DataInputStream input, DataOutputStream output, SSLSocket s) {
        this.Input = input;
        this.Output = output;
        this.s = s;
        this.isReady=false;
        this.myRound=false;
        this.NineHeart=false;
        this.isWinner=false;
        this.isEnd=false;
        this.listOfCard = new LinkedList<>();

    }

    @Override
    public void run() {
        while (true) {
            if(inThread())
                return;
        }
    }

    private void quitGame() throws IOException {
        sentToAll("game end");
        this.isEnd = true;
        System.exit(0);
        this.Output.close();
        this.Input.close();
        this.s.close();
    }
    private void waitGame() throws IOException{
        int iterator_of_stack = 0;
        while ((!Server.cardStack.peek().getValue().equals("9")
                || !Server.cardStack.peek().getSuit().equals("heart"))
                && iterator_of_stack < 3) {
            listOfCard.add(Server.cardStack.pop());
            iterator_of_stack++;
        }//while
        sendCards();
        nextRound();
    }

    private void checkIfWeHaveWinner() throws IOException{
        isWinner = true;
        Output.writeUTF("you winner");
        int how_many_winners = 0;
        int iterator_forEach_winners = 0;
        int looser = 0;
        for (ClientHandler cl : Server.vector
        ) {
            if (cl.isWinner)
                how_many_winners++;
            else {
                looser = iterator_forEach_winners;
            }
            iterator_forEach_winners++;
        }//forEachWinner
        if (how_many_winners == 3) {
            Server.vector.get(looser).Output.writeUTF("you looser");
            sentToAll("end");
            this.isEnd = true;
            System.exit(0);
            this.Output.close();
            this.Input.close();
            this.s.close();
        }//if
    }
    private static boolean checkST( StringTokenizer sT) {
        return sT.countTokens() == 0
                || sT.countTokens() == 2
                || sT.countTokens() == 4
                || sT.countTokens() > 4;
    }

    private boolean getCards(StringTokenizer sT, LinkedList<Card> cardsFromClient)  throws IOException {
        while (sT.hasMoreTokens()) {
            StringTokenizer st1 = new StringTokenizer(sT.nextToken());
            if (st1.countTokens() == 2) {
                Card tmpCard = new Card(st1.nextToken(), st1.nextToken());

                if (!Server.findInArray(Server.arrTab, tmpCard)) {
                    Output.writeUTF("this is not a card");
                    return false;
                }
                else {
                    if (findInList(listOfCard, tmpCard))
                        cardsFromClient.add(tmpCard);
                    else {
                        Output.writeUTF("You don't have this card");
                        return false;
                    }
                }
            } else {
                Output.writeUTF("Name Card Error");
                return false;
            }//else
        }//while
        return true;
    }
    private boolean checkIfClientsCardsHasGoodValue(LinkedList <Card> cardsFromClient) {
       return  Server.cardStack.empty() ||
                (Server.ValueOfCardMAP.get(cardsFromClient.getFirst().getValue())
                >=
                Server.ValueOfCardMAP.get(Server.cardStack.peek().getValue()));
    }

    private boolean sent(String buf) throws IOException{
        LinkedList<Card> cardsFromClient = new LinkedList<>();
        String CardsToSend_tmp = buf.substring(5);
        String CardsToSend = CardsToSend_tmp.replaceAll(", ", ",");
        StringTokenizer sT = new StringTokenizer(CardsToSend, ",", false);
        if (checkST(sT)) {
            Output.writeUTF("something is wrong, try again");
        } else {
            if(!this.getCards(sT, cardsFromClient)) {
                return false;
            }
            if (!checkValueOfCards(cardsFromClient)) {
                Output.writeUTF("you try to send cards with another value");
                return false;
            } else {
                if (!checkIdentical(cardsFromClient)) {
                    Output.writeUTF("Identical cards");
                    return false;
                } else {
                    if (checkIfClientsCardsHasGoodValue(cardsFromClient)) {
                        for (Card c : cardsFromClient
                        ) {
                            Server.cardStack.push(c);
                            listOfCard.remove(findIndexInList(c));
                        }
                    }
                    else {
                        Output.writeUTF("this cards has too small value");
                        return false;
                    }
                    nextRound();
                    Output.writeUTF("OK, wait for next round");
                }
            }
        }
        cardsFromClient.clear();
        if (listOfCard.isEmpty()) {
            this.checkIfWeHaveWinner();
        }
        return true;
    }
    private boolean play() throws IOException{
        String buf;
        buf = Input.readUTF();
        String buf_switch;
        buf_switch = buf.substring(0, 4);
        switch (buf_switch) {
            case "quit":
                this.quitGame();
                break;
            case "wait":
                this.waitGame();
                break;
            case "send":
              if(!sent(buf)){
               return false;
              }
                break;
            default:
                Output.writeUTF("Invalid command");
        }
        return true;
    }

    private boolean inThread() {

            try {
                if(!this.isReady) {
                    Output.writeUTF("Welcome in PAN game");
                }
                if(!this.isEnd) {
                    if(!play()){
                        return false;
                    }
                }//if
                else  {
                    this.Input.close();
                    this.Output.close();
                    this.s.close();
                    return true;
                }
            } //try
            catch (IOException e) {
                e.printStackTrace();
            }
        return false;
    }

    private void sentToAll(String msg) throws IOException {
        for (ClientHandler ch : Server.vector) {
            ch.Output.writeUTF(msg);
        }
    }


    private void nextRound() throws IOException {
            this.NineHeart=false;
            this.myRound=false;
            int index= Server.vector.indexOf(this)+1;
            for(int i=0; i<3;i++) {
                if (index == 4) index = 0;
                if (!Server.vector.get(index).isWinner) {
                    Server.vector.get(index).Output.writeUTF("your round");
                    Server.vector.get(index).Output.writeUTF("Actual card is: "+Server.cardStack.peek().toString());
                    Server.vector.get(index).myRound = true;
                    Server.vector.get(index).sendCards();
                    break;
                }//if
                index++;
            }//for
        }//nextRound

    private static boolean findInList(LinkedList<Card> lc, Card card)
    {
        for(int i=0;i<lc.size();i++)
        {
            if(lc.get(i).getValue().equals(card.getValue())&&lc.get(i).getSuit().equals(card.getSuit()))
                return true;
        }
        return false;

    }//findInList

    private int findIndexInList(Card card)
    {
        for(int i=0;i<listOfCard.size();i++)
        {
            if(listOfCard.get(i).getValue().equals(card.getValue())&&listOfCard.get(i).getSuit().equals(card.getSuit()))
                return i;
        }
            return -1;
    }

    private static boolean checkIdentical(LinkedList<Card> l) {
         Map<String,Boolean> IdenticalCheckMap = new HashMap<>();
        IdenticalCheckMap.put("diamond",false);
        IdenticalCheckMap.put("heart",false);
        IdenticalCheckMap.put("club",false);
        IdenticalCheckMap.put("spade",false);
        for (Card c: l
             ) {
            if(!IdenticalCheckMap.get(c.getSuit()))
                IdenticalCheckMap.replace(c.getSuit(),false,true);
            else return false;
        }//forEach
        return true;
    }//checkIdentical


    private static boolean checkValueOfCards(LinkedList<Card> l) {
        if(l.size()==1)
            return true;
        else
        {
            String tmp=l.getFirst().getValue();
            for(int i=1;i<l.size();i++)
            {
                if(!l.get(i).getValue().equals(tmp))
                    return false;
            }//for
        }
        return true;
    }//checkValueOfCard

    private void sendCards() throws IOException {
        String cardsToSend = "";
        for (Card c : listOfCard
        ) {
            cardsToSend += c.toString();

        }
        this.Output.writeUTF("your's cards");
        this.Output.writeUTF(cardsToSend);
    }

}
