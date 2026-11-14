package com.blackjack.trainer.ui;

import com.blackjack.trainer.model.Deck;
import com.blackjack.trainer.model.Hand;
import com.blackjack.trainer.model.Player;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;

@Component
public class MainFrame extends JFrame {
    private final Deck deck;
    private final Player player;
    private final Hand dealerHand;

    private JTextArea gameStatusArea;
    private JLabel countLabel;
    private JLabel cardsRemainingLabel;
    private JLabel chipsLabel;
    private JLabel currentBetLabel;

    private JButton dealButton;
    private JButton hitButton;
    private JButton standButton;
    private JButton doubleButton;

    public MainFrame() {
        this.deck = new Deck();
        this.player = new Player(1000);
        this.dealerHand = new Hand();

        initUI();
    }

    private void initUI() {
        setTitle("Blackjack Trainer (Spring Boot + Swing)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(750, 520);
        setLocationRelativeTo(null);

        // Main layout
        setLayout(new BorderLayout(10, 10));

        // Top Header / Stats Panel
        JPanel topPanel = new JPanel(new GridLayout(1, 4, 10, 10));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        topPanel.setBackground(new Color(30, 41, 59));

        chipsLabel = createHeaderLabel("Chips: $1000");
        currentBetLabel = createHeaderLabel("Bet: $50");
        countLabel = createHeaderLabel("Running Count: 0");
        cardsRemainingLabel = createHeaderLabel("Cards Left: 0");

        topPanel.add(chipsLabel);
        topPanel.add(currentBetLabel);
        topPanel.add(countLabel);
        topPanel.add(cardsRemainingLabel);
        add(topPanel, BorderLayout.NORTH);

        // Center Game Display Area
        gameStatusArea = new JTextArea();
        gameStatusArea.setEditable(false);
        gameStatusArea.setFont(new Font("Consolas", Font.PLAIN, 15));
        gameStatusArea.setBackground(new Color(15, 23, 42));
        gameStatusArea.setForeground(new Color(241, 245, 249));
        gameStatusArea.setMargin(new Insets(15, 15, 15, 15));
        gameStatusArea.setText("Welcome to Blackjack Trainer!\nClick 'New Deal' to start a new round.\n");

        JScrollPane scrollPane = new JScrollPane(gameStatusArea);
        add(scrollPane, BorderLayout.CENTER);

        // Bottom Controls Panel
        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        controlsPanel.setBackground(new Color(241, 245, 249));

        dealButton = new JButton("New Deal");
        hitButton = new JButton("Hit");
        standButton = new JButton("Stand");
        doubleButton = new JButton("Double Down");

        styleButton(dealButton, new Color(37, 99, 235));
        styleButton(hitButton, new Color(16, 185, 129));
        styleButton(standButton, new Color(239, 68, 68));
        styleButton(doubleButton, new Color(245, 158, 11));

        hitButton.setEnabled(false);
        standButton.setEnabled(false);
        doubleButton.setEnabled(false);

        dealButton.addActionListener(e -> startNewDeal());
        hitButton.addActionListener(e -> hitPlayer());
        standButton.addActionListener(e -> standPlayer());
        doubleButton.addActionListener(e -> doubleDownPlayer());

        controlsPanel.add(dealButton);
        controlsPanel.add(hitButton);
        controlsPanel.add(standButton);
        controlsPanel.add(doubleButton);

        add(controlsPanel, BorderLayout.SOUTH);
    }

    private JLabel createHeaderLabel(String text) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setForeground(Color.WHITE);
        return label;
    }

    private void styleButton(JButton button, Color bg) {
        button.setBackground(bg);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setPreferredSize(new Dimension(130, 38));
    }

    private void startNewDeal() {
        if (deck.cardsRemaining() < 15) {
            deck.ShuffleDeck(4);
            gameStatusArea.append("\n--- Shuffled 4 fresh decks into shoe ---\n");
        }

        player.setInitialBet(50);
        player.deductChips(50);
        player.resetForNewRound();

        dealerHand.getPlayerCards().forEach(c -> {});
        dealerHand.removeCard(0); // clear any leftover cards
        while (dealerHand.getCardsCount() > 0) {
            dealerHand.removeCard(0);
        }

        // Deal initial 2 cards to player
        String p1 = deck.drawCard();
        deck.trackCount(p1.substring(0, 2));
        String p2 = deck.drawCard();
        deck.trackCount(p2.substring(0, 2));
        player.getPrimaryHand().addCard(p1);
        player.getPrimaryHand().addCard(p2);

        // Deal 2 cards to dealer (one face up, one hidden)
        String d1 = deck.drawCard();
        deck.trackCount(d1.substring(0, 2));
        String d2 = deck.drawCard();
        // second dealer card count will be tracked when revealed
        dealerHand.addCard(d1);
        dealerHand.addCard(d2);

        updateLabels();

        gameStatusArea.setText("================= NEW ROUND =================\n");
        gameStatusArea.append("Dealer shows: [" + d1 + "]\n");
        gameStatusArea.append("Your Hand   : " + player.getPrimaryHand().getPlayerCards() + " (Score: " + player.getPrimaryHand().calculateScore() + ")\n");

        if (player.getPrimaryHand().calculateScore() == 21) {
            gameStatusArea.append("-> Natural Blackjack!\n");
            endRound();
        } else {
            hitButton.setEnabled(true);
            standButton.setEnabled(true);
            doubleButton.setEnabled(player.getChips() >= 50);
            dealButton.setEnabled(false);
        }
    }

    private void hitPlayer() {
        String card = deck.drawCard();
        deck.trackCount(card.substring(0, 2));
        player.getPrimaryHand().addCard(card);

        updateLabels();
        gameStatusArea.append("You hit: [" + card + "] -> Hand: " + player.getPrimaryHand().getPlayerCards() + " (Score: " + player.getPrimaryHand().calculateScore() + ")\n");

        doubleButton.setEnabled(false);

        if (player.getPrimaryHand().isBusted()) {
            gameStatusArea.append("-> BUSTED! You went over 21.\n");
            endRound();
        }
    }

    private void doubleDownPlayer() {
        if (player.getChips() >= 50) {
            player.deductChips(50);
            player.setInitialBet(100);
            updateLabels();

            String card = deck.drawCard();
            deck.trackCount(card.substring(0, 2));
            player.getPrimaryHand().addCard(card);

            gameStatusArea.append("You Doubled Down! Received: [" + card + "] -> Hand: " + player.getPrimaryHand().getPlayerCards() + " (Score: " + player.getPrimaryHand().calculateScore() + ")\n");

            if (player.getPrimaryHand().isBusted()) {
                gameStatusArea.append("-> BUSTED! You went over 21.\n");
            }
            standPlayer();
        }
    }

    private void standPlayer() {
        hitButton.setEnabled(false);
        standButton.setEnabled(false);
        doubleButton.setEnabled(false);

        // Dealer reveals hidden card
        String hiddenCard = dealerHand.getCard(1);
        deck.trackCount(hiddenCard.substring(0, 2));
        gameStatusArea.append("\nDealer reveals hidden card: [" + hiddenCard + "] -> Total Dealer Hand: " + dealerHand.getPlayerCards() + " (Score: " + dealerHand.calculateScore() + ")\n");

        // Dealer hits on soft 17 or under
        while (dealerHand.calculateScore() < 17) {
            String c = deck.drawCard();
            deck.trackCount(c.substring(0, 2));
            dealerHand.addCard(c);
            gameStatusArea.append("Dealer draws: [" + c + "] (Score: " + dealerHand.calculateScore() + ")\n");
        }

        evaluateWinners();
        endRound();
    }

    private void evaluateWinners() {
        int pScore = player.getPrimaryHand().calculateScore();
        int dScore = dealerHand.calculateScore();
        int bet = player.getInitialBet();

        if (pScore > 21) {
            gameStatusArea.append("\nResult: House wins (Player Busted).\n");
        } else if (dScore > 21) {
            gameStatusArea.append("\nResult: YOU WIN! (Dealer Busted) +$" + (bet * 2) + "\n");
            player.addChips(bet * 2);
        } else if (pScore > dScore) {
            gameStatusArea.append("\nResult: YOU WIN! (" + pScore + " vs " + dScore + ") +$" + (bet * 2) + "\n");
            player.addChips(bet * 2);
        } else if (pScore < dScore) {
            gameStatusArea.append("\nResult: Dealer wins (" + dScore + " vs " + pScore + ").\n");
        } else {
            gameStatusArea.append("\nResult: Push (Tie). Bet returned +$" + bet + "\n");
            player.addChips(bet);
        }
    }

    private void endRound() {
        hitButton.setEnabled(false);
        standButton.setEnabled(false);
        doubleButton.setEnabled(false);
        dealButton.setEnabled(true);
        updateLabels();
    }

    private void updateLabels() {
        chipsLabel.setText("Chips: $" + player.getChips());
        currentBetLabel.setText("Bet: $" + player.getInitialBet());
        countLabel.setText("Running Count: " + deck.getCount());
        cardsRemainingLabel.setText("Cards Left: " + deck.cardsRemaining());
    }
}
