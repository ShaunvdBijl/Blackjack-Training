package BlackjackTrainer;

import java.util.ArrayList;
import java.util.List;

public class Hand {
    private List<String> playerCards = new ArrayList<>();
    private int score = 0;

    public Hand() {
    }

    // 1. Just accept a String. The Player will hand this to you.
    public void addCard(String card) {
        this.playerCards.add(card);
    }

    // 2. Calculate the score based ONLY on the cards currently in this hand
    public int calculateScore() {
        int tempScore = 0;

        for (String card : this.playerCards) {
            int cardValue = Integer.parseInt(card.substring(0, 2));

            if (cardValue >= 11 && cardValue <= 13) {
                cardValue = 10;
            }
            tempScore += cardValue;
        }

        this.score = tempScore; // Update the class variable
        return this.score;
    }

    // 3. Check if THIS specific hand has busted
    public boolean isBusted() {
        return calculateScore() > 21;
    }

    public int getScore() {
        return this.score;
    }
}