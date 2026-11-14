package com.blackjack.trainer;

import com.blackjack.trainer.model.Deck;
import com.blackjack.trainer.model.Hand;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BlackjackModelTest {

    @Test
    void testDeckShuffleAndDraw() {
        Deck deck = new Deck();
        deck.ShuffleDeck(1);
        assertEquals(52, deck.cardsRemaining());

        String card = deck.drawCard();
        assertNotNull(card);
        assertEquals(51, deck.cardsRemaining());
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
}
