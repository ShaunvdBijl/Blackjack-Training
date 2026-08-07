package BlackjackTrainer;
import java.util.*;

public class Deck {
    private ArrayList<String> deckCreation= new ArrayList<String>();
    private String[] deckSuits = {"S","C","D","H"};
    private String[]deckNumbers ={"01","02","03","04","05","06","07","08","09","10","11","12","13"};
    private Stack<String> playingDeck = new Stack<String>();
    private int count =0;
    private String drawnCard;


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
        drawnCard= this.playingDeck.pop();
        return drawnCard;

    }

    public int cardsRemaining(){
        return this.playingDeck.size();
    }

    public int getCount(){
        return this.count;
    }
    public void setCount(){
        int cardValue =Integer.parseInt(setDrawnCard());

        if(cardValue>=1 && cardValue<=6){
            this.count = this.count+1;
        }else if (cardValue>=10 && cardValue<=13){
            this.count = this.count -1;
        }
    }

    public String setDrawnCard(){
        String card = drawCard();
        return card.substring(0,2);
    }

    public String getDrawnCard(){
        return this.drawnCard;
    }
}
