package BlackjackTrainer;

import java.util.ArrayList;
import java.util.List;

public class Player {
    private List<Hand> myHands = new ArrayList<>();

    // ... (Your PlayerAction Enum stays exactly the same) ...

    public Player() {
        // When a player sits down, they start with one empty Hand
        this.myHands.add(new Hand());
    }

    // We pass in the active deck, AND which hand we are hitting (0 is the first hand)
    public void hit(Deck activeDeck, int handIndex) {
        // 1. Draw a card from the active game deck
        String drawnCard = activeDeck.drawCard();

        // 2. Get the specific hand and give it the card
        Hand currentHand = this.myHands.get(handIndex);
        currentHand.addCard(drawnCard);

        // 3. Check if that specific hand busted
        if (currentHand.isBusted()) {
            System.out.println("Your score is: " + currentHand.getScore() + " so unfortunately you busted!!");
        } else {
            System.out.println("Hand score is now: " + currentHand.getScore());
            // Loop back to ask for input again...
        }
    }

    public void split() {
        // Because your Hand holds the cards now, you check it like this:
        // (Assuming you add a getCard(index) method to your Hand class)
        // if (this.myHands.get(0).getCard(0).substring(0,2).equals(this.myHands.get(0).getCard(1).substring(0,2))) { ... }
    }

    // ... (playerInput method) ...
}