package com.blackjack.trainer.model;

import java.util.ArrayList;
import java.util.List;

public class Player {
    private List<Hand> myHands = new ArrayList<>();
    private List<Integer> handBets = new ArrayList<>();
    private int chips = 1000;
    private int initialBet = 0;

    public enum PlayerAction {
        HIT(1),
        STAND(2),
        SPLIT(3),
        VIEW_COUNT(4),
        DOUBLE(5),
        VIEW_CARDS_LEFT(6);

        private final int value;

        PlayerAction(int value) {
            this.value = value;
        }

        public int getValue() {
            return this.value;
        }

        public static PlayerAction fromString(String input) {
            if (input == null || input.trim().isEmpty()) {
                return null;
            }
            String clean = input.trim().toUpperCase();

            for (PlayerAction action : values()) {
                if (String.valueOf(action.value).equals(clean)) {
                    return action;
                }
            }

            switch (clean) {
                case "H":
                case "HIT":
                    return HIT;
                case "S":
                case "STAND":
                    return STAND;
                case "SP":
                case "SPLIT":
                    return SPLIT;
                case "C":
                case "COUNT":
                case "VIEW_COUNT":
                    return VIEW_COUNT;
                case "D":
                case "DOUBLE":
                    return DOUBLE;
                case "R":
                case "REMAINING":
                case "CARDS_LEFT":
                case "VIEW_CARDS_LEFT":
                    return VIEW_CARDS_LEFT;
                default:
                    return null;
            }
        }
    }

    public Player() {
        this.myHands.add(new Hand());
        this.handBets.add(0);
    }

    public Player(int initialChips) {
        this.chips = initialChips;
        this.myHands.add(new Hand());
        this.handBets.add(0);
    }

    public void resetForNewRound() {
        this.myHands.clear();
        this.handBets.clear();
        this.myHands.add(new Hand());
        this.handBets.add(this.initialBet);
    }

    public List<Hand> getMyHands() {
        return myHands;
    }

    public Hand getPrimaryHand() {
        if (myHands.isEmpty()) {
            myHands.add(new Hand());
        }
        return myHands.get(0);
    }

    public int getChips() {
        return chips;
    }

    public void setChips(int chips) {
        this.chips = chips;
    }

    public void addChips(int amount) {
        this.chips += amount;
    }

    public void deductChips(int amount) {
        this.chips -= amount;
    }

    public int getInitialBet() {
        return initialBet;
    }

    public void setInitialBet(int initialBet) {
        this.initialBet = initialBet;
    }

    public List<Integer> getHandBets() {
        return handBets;
    }
}
