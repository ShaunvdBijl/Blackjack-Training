package BlackjackTrainer;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Player {
    private List<Hand> myHands = new ArrayList<>();
    private List<Integer> handBets = new ArrayList<>();
    private int chips = 0;
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

            // Match numeric action values: "1" -> HIT, "2" -> STAND, etc.
            for (PlayerAction action : values()) {
                if (String.valueOf(action.value).equals(clean)) {
                    return action;
                }
            }

            // Match names and common shorthand abbreviations
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
                case "L":
                case "LEFT":
                case "CARDS":
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

    // Constructors
    public Player() {
        this(0);
    }

    public Player(int startAmount) {
        this.chips = startAmount;
        resetHands();
    }

    // Resets hands and bets for a new round
    public void resetHands() {
        this.myHands.clear();
        this.handBets.clear();
        this.myHands.add(new Hand());
        this.handBets.add(0);
    }

    // Buy-in
    public int BuyIn(int startAmount) {
        this.chips = startAmount;
        System.out.printf("Your starting amount: %d%n", startAmount);
        return this.chips;
    }

    public int buyIn(int startAmount) {
        return BuyIn(startAmount);
    }

    // Chip management
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

    // Initial bet methods
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
        if (this.chips > 0 && amount > this.chips) {
            System.out.println("Not enough chips to place this bet.");
            return false;
        }
        if (this.chips >= amount) {
            this.chips -= amount;
        }
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

    public List<Integer> getHandBets() {
        return this.handBets;
    }

    // Payout helpers
    public void winBet(int handIndex, double multiplier) {
        int bet = getHandBet(handIndex);
        int payout = (int) (bet * multiplier);
        this.chips += payout;
        System.out.printf("Hand %d won! Payout: %d chips. Current balance: %d chips%n", handIndex + 1, payout, this.chips);
    }

    public void pushBet(int handIndex) {
        int bet = getHandBet(handIndex);
        this.chips += bet;
        System.out.printf("Hand %d pushed. Returned bet: %d chips. Current balance: %d chips%n", handIndex + 1, bet, this.chips);
    }

    // Hand actions
    public void hit(Deck activeDeck, int handIndex) {
        if (handIndex < 0 || handIndex >= this.myHands.size()) {
            System.out.println("Invalid hand index: " + handIndex);
            return;
        }
        String drawnCard = activeDeck.drawCard();
        Hand currentHand = this.myHands.get(handIndex);
        currentHand.addCard(drawnCard);

        System.out.println("Drew card: " + drawnCard);
        if (currentHand.isBusted()) {
            System.out.println("Your score is: " + currentHand.getScore() + " so unfortunately you busted!!");
        } else {
            System.out.println("Hand score is now: " + currentHand.getScore());
        }
    }

    public void stand() {
        stand(0);
    }

    public void stand(int handIndex) {
        if (handIndex >= 0 && handIndex < this.myHands.size()) {
            Hand currentHand = this.myHands.get(handIndex);
            System.out.println("Standing on Hand " + (handIndex + 1) + " with a score of: " + currentHand.calculateScore());
        } else {
            System.out.println("Standing.");
        }
    }

    public void doubleHand(int handIndex) {
        System.out.println("Doubling down on a hand requires passing the active deck.");
    }

    public void doubleHand(Deck activeDeck, int handIndex) {
        if (handIndex < 0 || handIndex >= this.myHands.size()) {
            System.out.println("Invalid hand index: " + handIndex);
            return;
        }

        Hand currentHand = this.myHands.get(handIndex);

        // In Blackjack, doubling down is only allowed on the initial two cards
        if (currentHand.getCardsCount() != 2) {
            System.out.println("Invalid move: You can only double down on your initial two cards.");
            return;
        }

        int currentBet = getHandBet(handIndex);
        if (this.chips < currentBet) {
            System.out.printf("Invalid move: Not enough chips to double down! (Requires: %d, Available: %d)%n",
                    currentBet, this.chips);
            return;
        }

        // Deduct extra bet from chips and double the bet for this hand
        this.chips -= currentBet;
        this.handBets.set(handIndex, currentBet * 2);
        System.out.printf("Doubled down! Bet increased to %d chips (Remaining chips: %d)%n",
                currentBet * 2, this.chips);

        // Player receives exactly ONE more card and stands
        String drawnCard = activeDeck.drawCard();
        currentHand.addCard(drawnCard);
        System.out.println("Drew final card: " + drawnCard);

        if (currentHand.isBusted()) {
            System.out.println("Your score is: " + currentHand.getScore() + " so unfortunately you busted!!");
        } else {
            System.out.println("Final score for Hand " + (handIndex + 1) + " is: " + currentHand.getScore() + ". Hand stands.");
        }
    }

    public void split(int handIndex) {
        split(null, handIndex);
    }

    public void split(Deck activeDeck, int handIndex) {
        if (handIndex < 0 || handIndex >= this.myHands.size()) {
            System.out.println("Invalid hand index: " + handIndex);
            return;
        }

        Hand currentHand = this.myHands.get(handIndex);

        // 1. You can only split if you have exactly 2 cards
        if (currentHand.getCardsCount() == 2) {
            String card1 = currentHand.getCard(0);
            String card2 = currentHand.getCard(1);

            // 2. Check if the card numbers match (comparing the "05" to "05")
            if (card1.substring(0, 2).equals(card2.substring(0, 2))) {
                int currentBet = getHandBet(handIndex);
                if (this.chips < currentBet) {
                    System.out.printf("Invalid move: Not enough chips to split! (Requires: %d, Available: %d)%n",
                            currentBet, this.chips);
                    return;
                }

                // Deduct matching bet for the new hand
                this.chips -= currentBet;
                this.handBets.add(currentBet);

                System.out.println("Splitting the hand!");

                // 3. Create a brand new hand
                Hand splitHand = new Hand();

                // 4. Move the second card from the old hand to the new hand
                splitHand.addCard(card2);
                currentHand.removeCard(1);

                // If active deck is present, deal a second card to each split hand
                if (activeDeck != null) {
                    String deal1 = activeDeck.drawCard();
                    currentHand.addCard(deal1);
                    System.out.println("Hand " + (handIndex + 1) + " receives: " + deal1);

                    String deal2 = activeDeck.drawCard();
                    splitHand.addCard(deal2);
                    System.out.println("New Hand " + (this.myHands.size() + 1) + " receives: " + deal2);
                }

                // 5. Add the new hand to the player's collection
                this.myHands.add(splitHand);

                System.out.println("Split successful! You now have " + myHands.size() + " hands.");
                System.out.println("Hand " + (handIndex + 1) + " score: " + currentHand.calculateScore());
                System.out.println("Hand " + myHands.size() + " score: " + splitHand.calculateScore());
            } else {
                System.out.println("Invalid move: Your two cards do not match.");
            }
        } else {
            System.out.println("Invalid move: You can only split on your initial two cards.");
        }
    }

    public void playerInput(String input, Deck activeDeck, int handIndex) {
        PlayerAction playerAction = PlayerAction.fromString(input);

        if (playerAction == null) {
            System.out.println("Invalid command. Please try again.");
            return;
        }

        switch (playerAction) {
            case HIT:
                hit(activeDeck, handIndex);
                break;
            case STAND:
                stand(handIndex);
                break;
            case SPLIT:
                split(activeDeck, handIndex);
                break;
            case VIEW_COUNT:
                System.out.println("The current card count is: " + activeDeck.getCount());
                break;
            case VIEW_CARDS_LEFT:
                System.out.println("Cards left in deck: " + activeDeck.cardsRemaining());
                break;
            case DOUBLE:
                doubleHand(activeDeck, handIndex);
                break;
        }
    }

    // Hand getters and utility methods
    public List<Hand> getMyHands() {
        return this.myHands;
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

    public void showHand(int handIndex) {
        Hand hand = getHand(handIndex);
        if (hand == null) {
            System.out.println("Hand " + (handIndex + 1) + " does not exist.");
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Hand ").append(handIndex + 1).append(" (Bet: ").append(getHandBet(handIndex)).append("): [");
        for (int i = 0; i < hand.getCardsCount(); i++) {
            sb.append(hand.getCard(i));
            if (i < hand.getCardsCount() - 1) {
                sb.append(", ");
            }
        }
        sb.append("] Score: ").append(hand.calculateScore());
        System.out.println(sb.toString());
    }

    public void showAllHands() {
        System.out.println("--- Current Hands (Chips: " + this.chips + ") ---");
        for (int i = 0; i < this.myHands.size(); i++) {
            showHand(i);
        }
    }
}
