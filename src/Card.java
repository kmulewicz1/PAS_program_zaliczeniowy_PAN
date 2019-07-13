public class Card
{

    private String suit;
    private String value;

    public String getSuit() {
        return suit;
    }

    public String getValue() {
        return value;
    }

    public void setSuit(String suit) {
        this.suit = suit;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Card( String value,String suit) {
        this.suit = suit;
        this.value = value;
    }


    public String toString()
    {
        return this.getValue()+" "+this.getSuit()+" ";
    }
}
