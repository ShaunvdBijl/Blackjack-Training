package com.blackjack.trainer;

import com.blackjack.trainer.model.Deck;
import com.blackjack.trainer.model.Hand;
import com.blackjack.trainer.model.Player;
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
    void testHandScoreCalculationWithAce() {
        Hand hand = new Hand();
        hand.addCard("01S"); // Ace
        hand.addCard("10D"); // 10
        assertEquals(21, hand.calculateScore());

        hand.addCard("05H"); // 5 -> 21 + 5 = 26, should adjust Ace to 1, score = 16
        assertEquals(16, hand.calculateScore());
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
}
