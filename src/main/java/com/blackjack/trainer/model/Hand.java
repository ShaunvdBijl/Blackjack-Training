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

    public void clear() {
        this.playerCards.clear();
        this.score = 0;
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

    public boolean isSoft() {
        int tempScore = 0;
        int aceCount = 0;

        for (String card : this.playerCards) {
            int cardValue = Integer.parseInt(card.substring(0, 2));
            if (cardValue == 1) {
                aceCount++;
                tempScore += 11;
            } else if (cardValue >= 11 && cardValue <= 13) {
                tempScore += 10;
            } else {
                tempScore += cardValue;
            }
        }

        while (tempScore > 21 && aceCount > 0) {
            tempScore -= 10;
            aceCount--;
        }

        return aceCount > 0 && tempScore <= 21;
    }

    public static int getCardBjValue(String card) {
        if (card == null || card.length() < 2) {
            return 0;
        }
        int val = Integer.parseInt(card.substring(0, 2));
        if (val == 1) {
            return 11; // Ace
        }
        return Math.min(val, 10);
    }

    public boolean canSplit() {
        if (this.playerCards.size() != 2) {
            return false;
        }
        String c1 = this.playerCards.get(0);
        String c2 = this.playerCards.get(1);
        return c1.substring(0, 2).equals(c2.substring(0, 2));
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
        if (index >= 0 && index < this.playerCards.size()) {
            this.playerCards.remove(index);
        }
    }

    public boolean isBusted() {
        return calculateScore() > 21;
    }

    public int getScore() {
        return calculateScore();
    }

    public String formatCards() {
        List<String> formatted = new ArrayList<>();
        for (String card : playerCards) {
            formatted.add(formatSingleCard(card));
        }
        return formatted.toString();
    }

    public static String formatSingleCard(String card) {
        if (card == null || card.length() < 3) {
            return String.valueOf(card);
        }
        String rankCode = card.substring(0, 2);
        String suitCode = card.substring(2, 3);

        String rank = switch (rankCode) {
            case "01" -> "A";
            case "11" -> "J";
            case "12" -> "Q";
            case "13" -> "K";
            default -> String.valueOf(Integer.parseInt(rankCode));
        };

        String suit = switch (suitCode) {
            case "S" -> "♠";
            case "H" -> "♥";
            case "D" -> "♦";
            case "C" -> "♣";
            default -> suitCode;
        };

        return rank + suit;
    }
}
