package com.blackjack.trainer.model;

import java.util.ArrayList;
import java.util.List;

public class Hand {
    private List<String> playerCards = new ArrayList<>();
    private int score = 0;

    public Hand() {
    }

    public void addCard(String card) {
        if (card != null) {
            this.playerCards.add(card);
        }
    }

    public int calculateScore() {
        int tempScore = 0;
        int aceCount = 0;

        for (String card : this.playerCards) {
            int cardValue = Integer.parseInt(card.substring(0, 2));

            if (cardValue == 1) { // Ace
                aceCount++;
                tempScore += 11;
            } else if (cardValue >= 11 && cardValue <= 13) {
                tempScore += 10;
            } else {
                tempScore += cardValue;
            }
        }

        // Adjust aces from 11 down to 1 if busted
        while (tempScore > 21 && aceCount > 0) {
            tempScore -= 10;
            aceCount--;
        }

        this.score = tempScore;
        return this.score;
    }

    public int getCardsCount() {
        return this.playerCards.size();
    }

    public List<String> getPlayerCards() {
        return new ArrayList<>(this.playerCards);
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
        return calculateScore();
    }
}
