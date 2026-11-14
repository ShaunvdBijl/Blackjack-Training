package com.blackjack.trainer.model;

import java.util.*;

public class Deck {
    private ArrayList<String> deckCreation = new ArrayList<>();
    private String[] deckSuits = {"S", "C", "D", "H"};
    private String[] deckNumbers = {"01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13"};
    private Stack<String> playingDeck = new Stack<>();
    private int count = 0;
    private String drawnCard;

    public Deck() {
    }

    public Stack<String> ShuffleDeck(int numberOfDecks) {
        this.deckCreation.clear();
        this.playingDeck.clear();
        this.count = 0;

        int decksAdded = 0;
        while (decksAdded < numberOfDecks) {
            for (int i = 0; i < deckSuits.length; i++) {
                for (int j = 0; j < deckNumbers.length; j++) {
                    String card = deckNumbers[j] + deckSuits[i];
                    this.deckCreation.add(card);
                }
            }
            decksAdded++;
        }
        Collections.shuffle(this.deckCreation);
        for (String finaldeckCard : this.deckCreation) {
            this.playingDeck.add(finaldeckCard);
        }
        return playingDeck;
    }

    public void trackCount(String cardOrRank) {
        if (cardOrRank == null || cardOrRank.length() < 2) {
            return;
        }
        String rank = cardOrRank.substring(0, 2);
        if ("01".equals(rank) || "10".equals(rank) || "11".equals(rank) || "12".equals(rank) || "13".equals(rank)) {
            this.count -= 1;
        } else if ("02".equals(rank) || "03".equals(rank) || "04".equals(rank) || "05".equals(rank) || "06".equals(rank)) {
            this.count += 1;
        }
    }

    public String drawCard() {
        if (this.playingDeck.isEmpty()) {
            return null;
        }
        drawnCard = this.playingDeck.pop();
        return drawnCard;
    }

    public String drawCardAndTrack() {
        String card = drawCard();
        if (card != null) {
            trackCount(card);
        }
        return card;
    }

    public int cardsRemaining() {
        return this.playingDeck.size();
    }

    public int getCount() {
        return this.count;
    }

    public void setCount() {
        String rank = setDrawnCard();
        if (rank != null) {
            int cardValue = Integer.parseInt(rank);
            if (cardValue >= 2 && cardValue <= 6) {
                this.count = this.count + 1;
            } else if (cardValue == 1 || (cardValue >= 10 && cardValue <= 13)) {
                this.count = this.count - 1;
            }
        }
    }

    public String setDrawnCard() {
        String card = drawCard();
        return (card != null && card.length() >= 2) ? card.substring(0, 2) : null;
    }

    public String getDrawnCard() {
        return this.drawnCard;
    }
}
