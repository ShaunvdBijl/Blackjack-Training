package Deck;
import java.util.*;

public class Deck {
    private ArrayList<String> deckCreation= new ArrayList<String>();
    private String[] deckSuits = {"S","C","D","H"};
    private String[]deckNumbers ={"01","02","03","04","05","06","07","08","09","10","11","12","13"};
    private Stack<String> playingDeck = new Stack<String>();


    public Deck() {

    }

    public Stack<String> ShuffleDeck(int numberOfDecks){
        int decksAdded=0;
        while(decksAdded<numberOfDecks){
            for (int i=0;i<deckSuits.length;i++){
                for (int j=0;j<deckNumbers.length;j++){
                    String card = deckNumbers[j]+deckSuits[i];

                    this.deckCreation.add(card);
                }
            }
            decksAdded++;
        }
        Collections.shuffle(this.deckCreation);
        for(String finaldeckCard:this.deckCreation){
            this.playingDeck.add(finaldeckCard);
        }
        return playingDeck;
    }

    public String drawCard(){
        return this.playingDeck.pop();
    }

    public int cardsRemaining(){
        return this.playingDeck.size();
    }
}
