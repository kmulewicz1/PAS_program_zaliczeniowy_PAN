import javax.net.ssl.SSLSocket;
import javax.sound.midi.Soundbank;
import java.io.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.StringTokenizer;


public class ClientHandler implements Runnable{


    DataInputStream Input;
    DataOutputStream Output;
    SSLSocket s;
    boolean isReady;
    boolean myRound;
    boolean isWinner;
    boolean NineHeart;
    LinkedList <Card> listOfCard;


    public ClientHandler( DataInputStream input, DataOutputStream output, SSLSocket s) {
        this.Input = input;
        this.Output = output;
        this.s = s;
        this.isReady=false;
        this.myRound=false;
        this.NineHeart=false;
        this.isWinner=false;
        this.listOfCard = new LinkedList<>();

    }

    public void inThread() {

            String buf;
            //if(!Server.cardStack.empty())
            //System.out.println(Server.cardStack.peek().toString());
            //System.out.println("heyyyy"+Server.vector.indexOf(this));
            try {
                if(!this.isReady)
                    Output.writeUTF("Welcome in PAN game");


                buf = Input.readUTF();
                //System.out.println(buf);
                String buf_switch;
                buf_switch=buf.substring(0,4);

                switch (buf_switch) {
                    case "quit":
                        sentToAll("game end");

                        break;

                    case "wait":
                        int iterator_of_stack = 0;
                        while ((!Server.cardStack.peek().getValue().equals("9")
                                || !Server.cardStack.peek().getSuit().equals("heart"))
                                && iterator_of_stack < 3) {
                            listOfCard.add(Server.cardStack.pop());
                            iterator_of_stack++;
                        }//while
                        sendCards();
                        nextRound();
                        break;
                    case "send":
                        LinkedList<Card> cards_from_client = new LinkedList<>();
                        //if(buf.endsWith(" "))
                        String CardsToSend_tmp = buf.substring(5);
                        String CardsToSend=CardsToSend_tmp.replaceAll(", ",",");
                        StringTokenizer sT = new StringTokenizer(CardsToSend, ",", false);
                        if (sT.countTokens() == 2
                                || sT.countTokens() == 0
                                || sT.countTokens() > 4
                                ||(sT.countTokens()==3&&NineHeart))
                        {
                            //System.out.println(sT.countTokens());
                            Output.writeUTF("something is wrong, try again");
                        }
                        else
                        {   while(sT.hasMoreTokens())
                        {
                            StringTokenizer st1=new StringTokenizer(sT.nextToken());
                            if(st1.countTokens()==2) {
                                Card tmpCard= new Card(st1.nextToken(),st1.nextToken());

                                if(!Server.findInArray(Server.arrTab,tmpCard))
                                {
                                    Output.writeUTF("this is no card");
                                    return;
                                }//if Server.findInArray(
                                else {
                                    if (findInList(listOfCard, tmpCard))
                                        cards_from_client.add(tmpCard);
                                    else {
                                        Output.writeUTF("You haven't this card");
                                        return;
                                    }//else find card in player's cards
                                }//else Server.findInArray
                            }
                            else {
                                Output.writeUTF("Name_Card_Error");
                                return;
                            }//else
                        }//while
                            if(!checkValueOfCards(cards_from_client))
                            {
                                Output.writeUTF("you try to send cards with another value");
                                return;
                            }
                            else {
                                if (!checkIdentical(cards_from_client))
                                {
                                    Output.writeUTF("Identical cards");
                                    return;
                                }
                                else {
                                    if (!Server.cardStack.empty()) {
                                        for (Card c : cards_from_client
                                        ) {
                                            if (Server.ValueOfCardMAP.get(c.getValue())
                                                    >=
                                                    Server.ValueOfCardMAP.get(Server.cardStack.peek().getValue())) {
                                                Server.cardStack.push(c);
                                            } else {
                                                Output.writeUTF("this cards has too small value");
                                                return;
                                            }//else

                                        }//forEach
                                    }//!Server.cardStack.empty()
                                    else {
                                        for (Card c : cards_from_client
                                        ) {
                                            Server.cardStack.push(c);
                                        }
                                    }//else stackEmpty
                                    //cards_from_client.clear();
                                    nextRound();
                                    Output.writeUTF("OK, wait for next round");
                                } //else checkIdentical()
                            } //else checkValueOfCards()
                        }//else

                        int iterator_forEach=0;
                        for (Card c: cards_from_client
                        ) {
                            listOfCard.remove(findIndexInList(c));
                            if(cards_from_client.size()-1==iterator_forEach)
                                Server.cardStack.push(c);
                            iterator_forEach++;

                        }
                        cards_from_client.clear();
                        if(listOfCard.isEmpty())
                        {
                            isWinner=true;
                            Output.writeUTF("you winner");
                            int how_many_winners=0;
                            int iterator_forEach_winners=0;
                            int looser=0;
                            for (ClientHandler cl: Server.vector
                            ) {
                                if(cl.isWinner)
                                    how_many_winners++;
                                else {looser=iterator_forEach_winners;}
                                iterator_forEach_winners++;
                            }//forEachWinner
                            if(how_many_winners==3)
                                Server.vector.get(looser).Output.writeUTF("you loose");
                        }
                        break;//send
                    default:
                    {
                        Output.writeUTF("Unvalid comand");
                    }//default
                }//switch
            } //try
            catch (IOException e) {
                e.printStackTrace();
            }//catch
    }

    //method to send msg to all player
    public void sentToAll(String msg) throws IOException {
        for (ClientHandler ch : Server.vector) {
            ch.Output.writeUTF(msg);
        }//for
    }//sentToAll
     public void nextRound() throws IOException
        {
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

    //find Card in LinkedList
    public static boolean findInList(LinkedList<Card>lc, Card card)
    {
        for(int i=0;i<lc.size();i++)
        {
            if(lc.get(i).getValue().equals(card.getValue())&&lc.get(i).getSuit().equals(card.getSuit()))
                return true;
        }
        return false;

    }//findInList

    public int findIndexInList(Card card)
    {
        for(int i=0;i<listOfCard.size();i++)
        {
            if(listOfCard.get(i).getValue().equals(card.getValue())&&listOfCard.get(i).getSuit().equals(card.getSuit()))
                return i;
        }
            return -1;
    }

    public  static boolean checkIdentical(LinkedList<Card> l)
    {
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


    public static boolean checkValueOfCards(LinkedList<Card> l)
    {
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

    public void sendCards() throws IOException {
        String cardsToSend = "";
        for (Card c : listOfCard
        ) {
            cardsToSend += c.toString();

        }
        this.Output.writeUTF("your's cards");
        this.Output.writeUTF(cardsToSend);
    }//sendCards


    @Override
    public void run() {
        while (true)
        {
            inThread();
        }//while(true)
    }//run

}//class
