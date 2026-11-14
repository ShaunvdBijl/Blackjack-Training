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
    void testBasicStrategyAssistor() {
        // 1. Pair of 8s vs Dealer 10 -> Split (Y)
        Hand pair8s = new Hand();
        pair8s.addCard("08S");
        pair8s.addCard("08D");
        Advice advice1 = BasicStrategyAssistor.getBestPlay(pair8s, "10H", true, true);
        assertEquals(PlayerAction.SPLIT, advice1.recommendedAction());

        // 2. Soft 18 (A,7) vs Dealer 6 -> Double if allowed (Ds), otherwise Stand
        Hand soft18 = new Hand();
        soft18.addCard("01S");
        soft18.addCard("07C");
        Advice advice2Double = BasicStrategyAssistor.getBestPlay(soft18, "06H", false, true);
        assertEquals(PlayerAction.DOUBLE, advice2Double.recommendedAction());

        Advice advice2NoDouble = BasicStrategyAssistor.getBestPlay(soft18, "06H", false, false);
        assertEquals(PlayerAction.STAND, advice2NoDouble.recommendedAction());

        // 3. Soft 18 (A,7) vs Dealer 9 -> Hit (H)
        Advice advice3 = BasicStrategyAssistor.getBestPlay(soft18, "09D", false, true);
        assertEquals(PlayerAction.HIT, advice3.recommendedAction());

        // 4. Hard 16 vs Dealer 6 -> Stand (S), vs Dealer 7 -> Hit (H)
        Hand hard16 = new Hand();
        hard16.addCard("10S");
        hard16.addCard("06D");
        assertEquals(PlayerAction.STAND, BasicStrategyAssistor.getBestPlay(hard16, "06C", false, true).recommendedAction());
        assertEquals(PlayerAction.HIT, BasicStrategyAssistor.getBestPlay(hard16, "07C", false, true).recommendedAction());
    }
}
