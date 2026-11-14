package com.blackjack.trainer.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

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
                case "D":
                case "DOUBLE":
                case "DOUBLE DOWN":
                case "DOUBLEDOWN":
                    return DOUBLE;
                case "C":
                case "COUNT":
                case "VIEW_COUNT":
                case "VIEW COUNT":
                    return VIEW_COUNT;
                case "R":
                case "L":
                case "LEFT":
                case "CARDS":
                case "REMAINING":
                case "CARDS_LEFT":
                case "VIEW_CARDS_LEFT":
                case "VIEW CARDS LEFT":
                    return VIEW_CARDS_LEFT;
                default:
                    try {
                        return PlayerAction.valueOf(clean);
                    } catch (IllegalArgumentException e) {
                        return null;
                    }
            }
        }
    }

    public Player() {
        this(1000);
    }

    public Player(int startAmount) {
        this.chips = startAmount;
        resetHands();
    }

    public void resetHands() {
        this.myHands.clear();
        this.handBets.clear();
        this.myHands.add(new Hand());
        this.handBets.add(0);
    }

    public void resetForNewRound() {
        this.myHands.clear();
        this.handBets.clear();
        this.myHands.add(new Hand());
        this.handBets.add(this.initialBet);
    }

    public int BuyIn(int startAmount) {
        this.chips = startAmount;
        return this.chips;
    }

    public int buyIn(int startAmount) {
        return BuyIn(startAmount);
    }

    public int getChips() {
        return this.chips;
    }

    public void setChips(int chips) {
        this.chips = chips;
    }

    public void addChips(int amount) {
        this.chips += amount;
    }

    public boolean deductChips(int amount) {
        if (amount <= this.chips) {
            this.chips -= amount;
            return true;
        }
        return false;
    }

    public int InitialBet() {
        Scanner sc = new Scanner(System.in);
        return InitialBet(sc);
    }

    public int InitialBet(Scanner sc) {
        int betAmount = 0;
        while (true) {
            System.out.printf("Please enter your initial bet: ");
            if (sc.hasNextInt()) {
                betAmount = sc.nextInt();
                if (betAmount <= 0) {
                    System.out.println("Bet amount must be greater than 0.");
                } else if (this.chips > 0 && betAmount > this.chips) {
                    System.out.printf("You cannot bet more chips than you have! (Current chips: %d)%n", this.chips);
                } else {
                    break;
                }
            } else {
                System.out.println("Invalid input. Please enter a valid number.");
                sc.next();
            }
        }
        placeBet(betAmount);
        return betAmount;
    }

    public boolean placeBet(int amount) {
        return placeBet(0, amount);
    }

    public boolean placeBet(int handIndex, int amount) {
        if (amount <= 0 || amount > this.chips) {
            return false;
        }
        this.chips -= amount;
        this.initialBet = amount;
        if (handIndex >= 0 && handIndex < this.handBets.size()) {
            this.handBets.set(handIndex, amount);
        } else if (handIndex == this.handBets.size()) {
            this.handBets.add(amount);
        }
        return true;
    }

    public int getInitialBet() {
        return this.initialBet;
    }

    public void setInitialBet(int initialBet) {
        this.initialBet = initialBet;
    }

    public int getHandBet(int handIndex) {
        if (handIndex >= 0 && handIndex < this.handBets.size()) {
            return this.handBets.get(handIndex);
        }
        return 0;
    }

    public void setHandBet(int handIndex, int bet) {
        if (handIndex >= 0 && handIndex < this.handBets.size()) {
            this.handBets.set(handIndex, bet);
        }
    }

    public int getTotalActiveBets() {
        int sum = 0;
        for (int bet : this.handBets) {
            sum += bet;
        }
        return sum;
    }

    public List<Integer> getHandBets() {
        return this.handBets;
    }

    public int winBet(int handIndex, double multiplier) {
        int bet = getHandBet(handIndex);
        int payout = (int) Math.round(bet * multiplier);
        this.chips += payout;
        return payout;
    }

    public int pushBet(int handIndex) {
        int bet = getHandBet(handIndex);
        this.chips += bet;
        return bet;
    }

    public String hit(Deck activeDeck, int handIndex) {
        if (handIndex < 0 || handIndex >= this.myHands.size()) {
            return null;
        }
        String drawnCard = activeDeck.drawCardAndTrack();
        Hand currentHand = this.myHands.get(handIndex);
        currentHand.addCard(drawnCard);
        return drawnCard;
    }

    public void stand() {
        stand(0);
    }

    public void stand(int handIndex) {
        // Player stands on handIndex
    }

    public boolean canDouble(int handIndex) {
        Hand hand = getHand(handIndex);
        if (hand == null || hand.getCardsCount() != 2) {
            return false;
        }
        int currentBet = getHandBet(handIndex);
        return this.chips >= currentBet && currentBet > 0;
    }

    public String doubleHand(Deck activeDeck, int handIndex) {
        if (!canDouble(handIndex)) {
            return null;
        }
        Hand currentHand = this.myHands.get(handIndex);
        int currentBet = getHandBet(handIndex);

        this.chips -= currentBet;
        this.handBets.set(handIndex, currentBet * 2);

        String drawnCard = activeDeck.drawCardAndTrack();
        currentHand.addCard(drawnCard);
        return drawnCard;
    }

    public boolean canSplit(int handIndex) {
        Hand hand = getHand(handIndex);
        if (hand == null || !hand.canSplit()) {
            return false;
        }
        int currentBet = getHandBet(handIndex);
        return this.chips >= currentBet && currentBet > 0;
    }

    public boolean split(Deck activeDeck, int handIndex) {
        if (!canSplit(handIndex)) {
            return false;
        }

        Hand currentHand = this.myHands.get(handIndex);
        String card2 = currentHand.getCard(1);
        int currentBet = getHandBet(handIndex);

        // Deduct matching bet for the new split hand
        this.chips -= currentBet;
        this.handBets.add(currentBet);

        Hand splitHand = new Hand();
        splitHand.addCard(card2);
        currentHand.removeCard(1);

        if (activeDeck != null) {
            String deal1 = activeDeck.drawCardAndTrack();
            currentHand.addCard(deal1);

            String deal2 = activeDeck.drawCardAndTrack();
            splitHand.addCard(deal2);
        }

        this.myHands.add(splitHand);
        return true;
    }

    public void playerInput(String input, Deck activeDeck, int handIndex) {
        PlayerAction playerAction = PlayerAction.fromString(input);
        if (playerAction == null) {
            return;
        }

        switch (playerAction) {
            case HIT -> hit(activeDeck, handIndex);
            case STAND -> stand(handIndex);
            case SPLIT -> split(activeDeck, handIndex);
            case DOUBLE -> doubleHand(activeDeck, handIndex);
            case VIEW_COUNT -> System.out.println("The current card count is: " + activeDeck.getCount());
            case VIEW_CARDS_LEFT -> System.out.println("Cards left in deck: " + activeDeck.cardsRemaining());
        }
    }

    public List<Hand> getMyHands() {
        return this.myHands;
    }

    public Hand getPrimaryHand() {
        if (myHands.isEmpty()) {
            myHands.add(new Hand());
            handBets.add(0);
        }
        return myHands.get(0);
    }

    public Hand getHand(int index) {
        if (index >= 0 && index < this.myHands.size()) {
            return this.myHands.get(index);
        }
        return null;
    }

    public int getHandCount() {
        return this.myHands.size();
    }

    public boolean isBusted(int handIndex) {
        Hand hand = getHand(handIndex);
        return hand != null && hand.isBusted();
    }

    public boolean allHandsBusted() {
        if (this.myHands.isEmpty()) {
            return false;
        }
        for (Hand hand : this.myHands) {
            if (!hand.isBusted()) {
                return false;
            }
        }
        return true;
    }

    public boolean hasBlackjack(int handIndex) {
        Hand hand = getHand(handIndex);
        return hand != null && hand.getCardsCount() == 2 && hand.calculateScore() == 21;
    }
}
