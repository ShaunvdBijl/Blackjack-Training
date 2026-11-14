package com.blackjack.trainer;

import com.blackjack.trainer.model.BasicStrategyAssistor;
import com.blackjack.trainer.model.BasicStrategyAssistor.Advice;
import com.blackjack.trainer.model.Deck;
import com.blackjack.trainer.model.Hand;
import com.blackjack.trainer.model.Player;
import com.blackjack.trainer.model.Player.PlayerAction;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BlackjackModelTest {

    @Test
    void testDeckShuffleDrawAndCount() {
        Deck deck = new Deck();
        deck.ShuffleDeck(1);
        assertEquals(52, deck.cardsRemaining());
        assertEquals(0, deck.getCount());

        deck.trackCount("02S");
        assertEquals(1, deck.getCount());
        deck.trackCount("10H");
        assertEquals(0, deck.getCount());
        deck.trackCount("01D");
        assertEquals(-1, deck.getCount());
    }

    @Test
    void testTrueCountAndBetRecommendations() {
        Deck deck = new Deck();
        deck.ShuffleDeck(2); // 104 cards = 2.0 decks
        assertEquals(2.0, deck.getDecksRemaining(), 0.01);
        assertEquals(0.0, deck.getTrueCount(), 0.01);
        assertEquals(1, deck.getRecommendedBetUnits());

        // Simulate +6 running count with 2.0 decks remaining -> True Count = +3.0
        for (int i = 0; i < 6; i++) {
            deck.trackCount("02S");
        }
        assertEquals(6, deck.getCount());
        assertEquals(3.0, deck.getTrueCount(), 0.01);
        assertEquals(4, deck.getRecommendedBetUnits()); // TC 3 -> 4x bet spread
        assertEquals(200, deck.getRecommendedBetAmount(50));
    }

    @Test
    void testHandScoreCalculationWithAce() {
        Hand hand = new Hand();
        hand.addCard("01S"); // Ace
        hand.addCard("10D"); // 10
        assertEquals(21, hand.calculateScore());
        assertTrue(hand.isSoft());

        hand.addCard("05H"); // 5 -> 21 + 5 = 26, should adjust Ace to 1, score = 16 (Hard)
        assertEquals(16, hand.calculateScore());
        assertFalse(hand.isSoft());
        assertFalse(hand.isBusted());
    }

    @Test
    void testMultipleAcesInHand() {
        Hand hand = new Hand();
        hand.addCard("01S"); // Ace (11)
        hand.addCard("01H"); // Ace (1) -> Soft 12
        assertEquals(12, hand.calculateScore());
        assertTrue(hand.isSoft());

        hand.addCard("09D"); // +9 -> 21
        assertEquals(21, hand.calculateScore());
        assertTrue(hand.isSoft());

        hand.addCard("05C"); // +5 -> both Aces become 1 -> 1 + 1 + 9 + 5 = 16 (Hard)
        assertEquals(16, hand.calculateScore());
        assertFalse(hand.isSoft());
    }

    @Test
    void testCardFormattingAndBjValues() {
        assertEquals("A♠", Hand.formatSingleCard("01S"));
        assertEquals("10♥", Hand.formatSingleCard("10H"));
        assertEquals("J♦", Hand.formatSingleCard("11D"));
        assertEquals("Q♣", Hand.formatSingleCard("12C"));
        assertEquals("K♠", Hand.formatSingleCard("13S"));

        assertEquals(11, Hand.getCardBjValue("01S"));
        assertEquals(10, Hand.getCardBjValue("13H"));
        assertEquals(7, Hand.getCardBjValue("07D"));
    }

    @Test
    void testPlayerSplitAndDouble() {
        Deck deck = new Deck();
        deck.ShuffleDeck(2);

        Player player = new Player(1000);
        assertTrue(player.placeBet(100));
        assertEquals(900, player.getChips());

        player.getPrimaryHand().addCard("08S");
        player.getPrimaryHand().addCard("08H");

        assertTrue(player.canSplit(0));
        assertTrue(player.canDouble(0));

        // Perform split
        assertTrue(player.split(deck, 0));
        assertEquals(2, player.getHandCount());
        assertEquals(800, player.getChips());
        assertEquals(100, player.getHandBet(0));
        assertEquals(100, player.getHandBet(1));
        assertEquals(2, player.getHand(0).getCardsCount());
        assertEquals(2, player.getHand(1).getCardsCount());

        // Double down on Hand 0
        String drawn = player.doubleHand(deck, 0);
        assertNotNull(drawn);
        assertEquals(200, player.getHandBet(0));
        assertEquals(700, player.getChips());
        assertEquals(3, player.getHand(0).getCardsCount());
    }

    @Test
    void testMultiHandInitializationAndSplitInsertionOrder() {
        Deck deck = new Deck();
        deck.ShuffleDeck(4);

        Player player = new Player(1000);
        player.setInitialBet(50);
        player.resetForNewRound(3); // Start with 3 simultaneous hands
        player.deductChips(150);

        assertEquals(3, player.getHandCount());
        assertEquals(150, player.getTotalActiveBets());
        assertEquals(850, player.getChips());

        // Give Hand 0 a pair of 9s, Hand 1 a 10+7, Hand 2 a 10+10
        player.getHand(0).addCard("09S");
        player.getHand(0).addCard("09D");
        player.getHand(1).addCard("10S");
        player.getHand(1).addCard("07H");
        player.getHand(2).addCard("12C");
        player.getHand(2).addCard("13D");

        // Split Hand 0 -> New split hand should be inserted at index 1, shifting old Hand 1 to index 2
        assertTrue(player.split(deck, 0));
        assertEquals(4, player.getHandCount());
        assertEquals(200, player.getTotalActiveBets());
        assertEquals(17, player.getHand(2).calculateScore()); // Old Hand 1 is now at index 2
        assertEquals(20, player.getHand(3).calculateScore()); // Old Hand 2 is now at index 3
    }

    @Test
    void testPayoutMultipliersAndActionParsing() {
        Player player = new Player(500);
        player.placeBet(100);
        assertEquals(400, player.getChips());

        // 3:2 Blackjack payout returns 250 total (100 original bet + 150 profit) -> balance 650
        int bjPayout = player.winBet(0, 2.5);
        assertEquals(250, bjPayout);
        assertEquals(650, player.getChips());

        assertEquals(PlayerAction.HIT, PlayerAction.fromString("h"));
        assertEquals(PlayerAction.STAND, PlayerAction.fromString("2"));
        assertEquals(PlayerAction.SPLIT, PlayerAction.fromString("SP"));
        assertEquals(PlayerAction.DOUBLE, PlayerAction.fromString("double down"));
    }

    @Test
    void testBasicStrategyAssistor() {
        // 1. Pair of 8s vs Dealer 10 -> Split (Y)
        Hand pair8s = new Hand();
        pair8s.addCard("08S");
        pair8s.addCard("08D");
        Advice advice1 = BasicStrategyAssistor.getBestPlay(pair8s, "10H", true, true);
        assertEquals(PlayerAction.SPLIT, advice1.recommendedAction());

        // 2. Pair of 9s vs Dealer 7 -> Stand (N -> Hard 18 -> S)
        Hand pair9s = new Hand();
        pair9s.addCard("09S");
        pair9s.addCard("09C");
        assertEquals(PlayerAction.STAND, BasicStrategyAssistor.getBestPlay(pair9s, "07H", true, true).recommendedAction());
        assertEquals(PlayerAction.SPLIT, BasicStrategyAssistor.getBestPlay(pair9s, "08H", true, true).recommendedAction());

        // 3. Soft 18 (A,7) vs Dealer 6 -> Double if allowed (Ds), otherwise Stand
        Hand soft18 = new Hand();
        soft18.addCard("01S");
        soft18.addCard("07C");
        Advice advice2Double = BasicStrategyAssistor.getBestPlay(soft18, "06H", false, true);
        assertEquals(PlayerAction.DOUBLE, advice2Double.recommendedAction());

        Advice advice2NoDouble = BasicStrategyAssistor.getBestPlay(soft18, "06H", false, false);
        assertEquals(PlayerAction.STAND, advice2NoDouble.recommendedAction());

        // 4. Soft 18 (A,7) vs Dealer 9 -> Hit (H)
        Advice advice3 = BasicStrategyAssistor.getBestPlay(soft18, "09D", false, true);
        assertEquals(PlayerAction.HIT, advice3.recommendedAction());

        // 5. Hard 11 vs Dealer 10 -> Double if allowed (D), otherwise Hit (H)
        Hand hard11 = new Hand();
        hard11.addCard("06S");
        hard11.addCard("05D");
        assertEquals(PlayerAction.DOUBLE, BasicStrategyAssistor.getBestPlay(hard11, "10C", false, true).recommendedAction());
        assertEquals(PlayerAction.HIT, BasicStrategyAssistor.getBestPlay(hard11, "10C", false, false).recommendedAction());

        // 6. Hard 16 vs Dealer 6 -> Stand (S), vs Dealer 7 -> Hit (H)
        Hand hard16 = new Hand();
        hard16.addCard("10S");
        hard16.addCard("06D");
        assertEquals(PlayerAction.STAND, BasicStrategyAssistor.getBestPlay(hard16, "06C", false, true).recommendedAction());
        assertEquals(PlayerAction.HIT, BasicStrategyAssistor.getBestPlay(hard16, "07C", false, true).recommendedAction());
    }
}
