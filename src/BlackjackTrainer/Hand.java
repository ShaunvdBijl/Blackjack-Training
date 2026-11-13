package BlackjackTrainer;

import java.util.ArrayList;
import java.util.List;

public class Hand {
    private List<String> playerCards = new ArrayList<>();
    private int score = 0;

    public Hand() {
    }

    public void addCard(String card) {
        this.playerCards.add(card);
    }

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

    public int getCardsCount() {
        return this.playerCards.size();
    }

    public String getCard(int index) {
        return this.playerCards.get(index);
    }

    public void removeCard(int index) {
        this.playerCards.remove(index);
    }

    public boolean isBusted() {
        return calculateScore() > 21;
    }

    public int getScore() {
        return this.score;
    }
}