package com.blackjack.trainer.ui;

import com.blackjack.trainer.model.BasicStrategyAssistor;
import com.blackjack.trainer.model.BasicStrategyAssistor.Advice;
import com.blackjack.trainer.model.Deck;
import com.blackjack.trainer.model.Hand;
import com.blackjack.trainer.model.Player;
import com.blackjack.trainer.model.Player.PlayerAction;
import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.io.File;
import java.util.List;

@Component
public class MainFrame extends JFrame {
    private final Deck deck;
    private final Player player;
    private final Hand dealerHand;

    private boolean roundActive = false;
    private boolean dealerCardRevealed = false;
    private boolean showRunningCount = true;
    private boolean showCardsRemaining = true;
    private boolean showStrategyAssistor = false;
    private int currentHandIndex = 0;

    // Header labels
    private JLabel chipsLabel;
    private JLabel betLabel;
    private JLabel countLabel;
    private JLabel cardsRemainingLabel;

    // Config controls
    private JSpinner betSpinner;
    private JSpinner decksSpinner;
    private JButton buyInButton;
    private JButton shuffleButton;
    private JToggleButton toggleStrategyButton;
    private JButton viewChartButton;

    // Table Visual Components
    private JPanel dealerCardsPanel;
    private JLabel dealerScoreLabel;
    private JPanel playerHandsContainer;
    private JLabel statusBannerLabel;
    private JLabel strategyAssistorBanner;

    // Log area
    private JTextArea logArea;

    // Action buttons
    private JButton dealButton;
    private JButton hitButton;
    private JButton standButton;
    private JButton doubleButton;
    private JButton splitButton;
    private JButton toggleCountButton;
    private JButton toggleCardsLeftButton;

    public MainFrame() {
        this.deck = new Deck();
        this.player = new Player(1000);
        this.dealerHand = new Hand();

        // Initial 4-deck shoe shuffle
        this.deck.ShuffleDeck(4);

        initUI();
        refreshAllUI();
    }

    private void initUI() {
        setTitle("Blackjack Trainer — Spring Boot & Swing");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1040, 760);
        setMinimumSize(new Dimension(920, 680));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(0, 0));

        // 1. TOP BAR: Stats & Shoe / Bet Configuration
        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.setBackground(new Color(15, 23, 42));
        topContainer.setBorder(new EmptyBorder(10, 16, 10, 16));

        JPanel statsPanel = new JPanel(new GridLayout(1, 4, 12, 0));
        statsPanel.setOpaque(false);

        chipsLabel = createStatBadge("Chips: $1000", new Color(16, 185, 129));
        betLabel = createStatBadge("Active Bet: $0", new Color(59, 130, 246));
        countLabel = createStatBadge("Running Count: 0", new Color(245, 158, 11));
        cardsRemainingLabel = createStatBadge("Cards Left: 208", new Color(168, 85, 247));

        statsPanel.add(chipsLabel);
        statsPanel.add(betLabel);
        statsPanel.add(countLabel);
        statsPanel.add(cardsRemainingLabel);

        // Settings bar (Buy-in, Bet amount, Deck count, Shuffle, Strategy Assistor Toggle)
        JPanel configPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        configPanel.setBackground(new Color(30, 41, 59));
        configPanel.setBorder(new EmptyBorder(4, 10, 4, 10));

        JLabel betPrompt = new JLabel("Bet ($):");
        betPrompt.setForeground(Color.WHITE);
        betPrompt.setFont(new Font("Segoe UI", Font.BOLD, 13));
        betSpinner = new JSpinner(new SpinnerNumberModel(50, 10, 10000, 10));
        betSpinner.setPreferredSize(new Dimension(75, 28));

        JLabel deckPrompt = new JLabel("Decks:");
        deckPrompt.setForeground(Color.WHITE);
        deckPrompt.setFont(new Font("Segoe UI", Font.BOLD, 13));
        decksSpinner = new JSpinner(new SpinnerNumberModel(4, 1, 8, 1));
        decksSpinner.setPreferredSize(new Dimension(55, 28));

        shuffleButton = createSmallButton("Shuffle Shoe", new Color(71, 85, 105));
        buyInButton = createSmallButton("Buy-In (+$500)", new Color(14, 116, 144));
        toggleCountButton = createSmallButton("Hide Count", new Color(100, 116, 139));
        toggleCardsLeftButton = createSmallButton("Hide Cards Left", new Color(100, 116, 139));

        toggleStrategyButton = new JToggleButton("Strategy Assistor: OFF");
        toggleStrategyButton.setBackground(new Color(71, 85, 105));
        toggleStrategyButton.setForeground(Color.WHITE);
        toggleStrategyButton.setFocusPainted(false);
        toggleStrategyButton.setFont(new Font("Segoe UI", Font.BOLD, 12));

        viewChartButton = createSmallButton("View Chart", new Color(99, 102, 241));

        shuffleButton.addActionListener(e -> manualShuffle());
        buyInButton.addActionListener(e -> promptBuyIn());
        toggleCountButton.addActionListener(e -> {
            showRunningCount = !showRunningCount;
            toggleCountButton.setText(showRunningCount ? "Hide Count" : "View Count");
            log("Running Count is now " + (showRunningCount ? "VISIBLE (" + deck.getCount() + ")" : "HIDDEN"));
            refreshAllUI();
        });
        toggleCardsLeftButton.addActionListener(e -> {
            showCardsRemaining = !showCardsRemaining;
            toggleCardsLeftButton.setText(showCardsRemaining ? "Hide Cards Left" : "View Cards Left");
            log("Cards Left is now " + (showCardsRemaining ? "VISIBLE (" + deck.cardsRemaining() + ")" : "HIDDEN"));
            refreshAllUI();
        });
        toggleStrategyButton.addActionListener(e -> {
            showStrategyAssistor = toggleStrategyButton.isSelected();
            toggleStrategyButton.setText(showStrategyAssistor ? "Strategy Assistor: ON" : "Strategy Assistor: OFF");
            toggleStrategyButton.setBackground(showStrategyAssistor ? new Color(16, 185, 129) : new Color(71, 85, 105));
            String stateMsg = showStrategyAssistor ? "ENABLED" : "DISABLED";
            log("Basic Strategy Assistor UI is now " + stateMsg + " (Console play evaluation is always active).");
            System.out.println("[Basic Strategy Assistor] Visual Assistor toggled " + stateMsg);
            refreshAllUI();
        });
        viewChartButton.addActionListener(e -> showStrategyChartDialog());

        configPanel.add(betPrompt);
        configPanel.add(betSpinner);
        configPanel.add(Box.createHorizontalStrut(4));
        configPanel.add(deckPrompt);
        configPanel.add(decksSpinner);
        configPanel.add(shuffleButton);
        configPanel.add(buyInButton);
        configPanel.add(Box.createHorizontalStrut(4));
        configPanel.add(toggleCountButton);
        configPanel.add(toggleCardsLeftButton);
        configPanel.add(Box.createHorizontalStrut(4));
        configPanel.add(toggleStrategyButton);
        configPanel.add(viewChartButton);

        topContainer.add(statsPanel, BorderLayout.NORTH);
        topContainer.add(configPanel, BorderLayout.SOUTH);
        add(topContainer, BorderLayout.NORTH);

        // 2. CENTER: Green Casino Felt Table + Activity Log
        JSplitPane centerSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        centerSplit.setResizeWeight(0.72);
        centerSplit.setDividerSize(6);
        centerSplit.setBorder(null);

        JPanel tableFelt = new JPanel(new BorderLayout(10, 10));
        tableFelt.setBackground(new Color(20, 83, 45)); // Deep casino green
        tableFelt.setBorder(new EmptyBorder(16, 20, 16, 20));

        // Dealer section
        JPanel dealerSection = new JPanel(new BorderLayout(6, 6));
        dealerSection.setOpaque(false);
        dealerScoreLabel = new JLabel("DEALER HAND", SwingConstants.CENTER);
        dealerScoreLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        dealerScoreLabel.setForeground(new Color(254, 240, 138));

        dealerCardsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 8));
        dealerCardsPanel.setOpaque(false);
        dealerCardsPanel.setPreferredSize(new Dimension(600, 125));

        dealerSection.add(dealerScoreLabel, BorderLayout.NORTH);
        dealerSection.add(dealerCardsPanel, BorderLayout.CENTER);

        // Status Banner + Strategy Assistor Banner in middle of table
        JPanel bannersPanel = new JPanel(new GridLayout(0, 1, 0, 6));
        bannersPanel.setOpaque(false);

        statusBannerLabel = new JLabel("Set your bet and click 'Deal Hand' to begin!", SwingConstants.CENTER);
        statusBannerLabel.setFont(new Font("Segoe UI", Font.BOLD, 17));
        statusBannerLabel.setForeground(Color.WHITE);
        statusBannerLabel.setOpaque(true);
        statusBannerLabel.setBackground(new Color(15, 23, 42));
        statusBannerLabel.setBorder(new EmptyBorder(8, 16, 8, 16));

        strategyAssistorBanner = new JLabel("Basic Strategy Assistor: Waiting for hand...", SwingConstants.CENTER);
        strategyAssistorBanner.setFont(new Font("Segoe UI", Font.BOLD, 15));
        strategyAssistorBanner.setForeground(new Color(254, 240, 138));
        strategyAssistorBanner.setOpaque(true);
        strategyAssistorBanner.setBackground(new Color(30, 58, 138));
        strategyAssistorBanner.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(250, 204, 21), 2, true),
                new EmptyBorder(6, 14, 6, 14)
        ));
        strategyAssistorBanner.setVisible(false);

        bannersPanel.add(statusBannerLabel);
        bannersPanel.add(strategyAssistorBanner);

        // Player Hands Section (supports multiple split hands)
        playerHandsContainer = new JPanel(new GridLayout(1, 0, 15, 0));
        playerHandsContainer.setOpaque(false);

        JPanel centerTableBody = new JPanel(new BorderLayout(8, 12));
        centerTableBody.setOpaque(false);
        centerTableBody.add(bannersPanel, BorderLayout.NORTH);
        centerTableBody.add(playerHandsContainer, BorderLayout.CENTER);

        tableFelt.add(dealerSection, BorderLayout.NORTH);
        tableFelt.add(centerTableBody, BorderLayout.CENTER);

        // Log Scroll Pane
        logArea = new JTextArea(6, 60);
        logArea.setEditable(false);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 13));
        logArea.setBackground(new Color(15, 23, 42));
        logArea.setForeground(new Color(226, 232, 240));
        logArea.setMargin(new Insets(8, 12, 8, 12));
        log("Welcome to Blackjack Trainer! Shoe initialized with 4 decks (208 cards).");

        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setBorder(BorderFactory.createTitledBorder(
                new LineBorder(new Color(51, 65, 85)),
                "Game Log & Action History",
                0, 0, new Font("Segoe UI", Font.BOLD, 12), new Color(148, 163, 184)
        ));
        logScroll.setBackground(new Color(15, 23, 42));

        centerSplit.setTopComponent(tableFelt);
        centerSplit.setBottomComponent(logScroll);
        add(centerSplit, BorderLayout.CENTER);

        // 3. BOTTOM BAR: Player Action Buttons
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 12));
        actionPanel.setBackground(new Color(15, 23, 42));
        actionPanel.setBorder(new EmptyBorder(4, 12, 8, 12));

        dealButton = createActionButton("Deal Hand", new Color(37, 99, 235));
        hitButton = createActionButton("Hit (H)", new Color(16, 185, 129));
        standButton = createActionButton("Stand (S)", new Color(239, 68, 68));
        doubleButton = createActionButton("Double Down (D)", new Color(245, 158, 11));
        splitButton = createActionButton("Split Pair (SP)", new Color(139, 92, 246));

        dealButton.addActionListener(e -> startNewRound());
        hitButton.addActionListener(e -> handleHit());
        standButton.addActionListener(e -> handleStand());
        doubleButton.addActionListener(e -> handleDoubleDown());
        splitButton.addActionListener(e -> handleSplit());

        actionPanel.add(dealButton);
        actionPanel.add(hitButton);
        actionPanel.add(standButton);
        actionPanel.add(doubleButton);
        actionPanel.add(splitButton);

        add(actionPanel, BorderLayout.SOUTH);
    }

    // ==================== BASIC STRATEGY EVALUATION & LOGGING ====================

    /**
     * Evaluates the player's chosen action against Basic Strategy BEFORE the hand is mutated,
     * and ALWAYS logs to the terminal console (System.out) whether the play was correct,
     * even when the Basic Strategy Assistor toggle is switched OFF.
     */
    private void evaluateAndLogPlayToConsole(PlayerAction chosenAction) {
        if (!roundActive || dealerHand.getCardsCount() == 0) {
            return;
        }
        Hand activeHand = player.getHand(currentHandIndex);
        if (activeHand == null) {
            return;
        }

        String dealerUpCard = dealerHand.getCard(0);
        boolean canSplit = player.canSplit(currentHandIndex);
        boolean canDouble = player.canDouble(currentHandIndex);

        Advice advice = BasicStrategyAssistor.getBestPlay(activeHand, dealerUpCard, canSplit, canDouble);
        boolean isCorrect = (chosenAction == advice.recommendedAction());

        String consoleMessage;
        if (isCorrect) {
            consoleMessage = String.format(
                    "[Basic Strategy Check] CORRECT PLAY! You chose %s on %s (Score: %d) vs Dealer %s. (%s)",
                    chosenAction,
                    activeHand.formatCards(),
                    activeHand.calculateScore(),
                    Hand.formatSingleCard(dealerUpCard),
                    advice.explanation()
            );
        } else {
            consoleMessage = String.format(
                    "[Basic Strategy Check] INCORRECT PLAY! You chose %s, but Basic Strategy recommends %s on %s (Score: %d) vs Dealer %s. (%s)",
                    chosenAction,
                    advice.recommendedAction(),
                    activeHand.formatCards(),
                    activeHand.calculateScore(),
                    Hand.formatSingleCard(dealerUpCard),
                    advice.explanation()
            );
        }

        // Always print to standard console output regardless of toggle state
        System.out.println(consoleMessage);

        // If the assistor toggle is ON, also record feedback in the UI log
        if (showStrategyAssistor) {
            log(consoleMessage);
        }
    }

    private void showStrategyChartDialog() {
        File chartFile = new File("basicStrategy.png");
        if (!chartFile.exists()) {
            chartFile = new File("Blackjack-Practise/basicStrategy.png");
        }
        if (chartFile.exists()) {
            ImageIcon icon = new ImageIcon(chartFile.getAbsolutePath());
            JLabel imgLabel = new JLabel(icon);
            JScrollPane scroll = new JScrollPane(imgLabel);
            scroll.setPreferredSize(new Dimension(450, 620));
            JOptionPane.showMessageDialog(this, scroll, "Basic Strategy Reference Chart", JOptionPane.PLAIN_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Could not locate basicStrategy.png in project root.", "Chart Not Found", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // ==================== GAMEPLAY ACTIONS ====================

    private void manualShuffle() {
        if (roundActive) {
            JOptionPane.showMessageDialog(this, "Finish the current hand before shuffling!", "Round in Progress", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int numDecks = (int) decksSpinner.getValue();
        deck.ShuffleDeck(numDecks);
        log("--- Manually shuffled " + numDecks + " deck(s) (" + deck.cardsRemaining() + " cards). Running count reset to 0. ---");
        statusBannerLabel.setText("Shoe shuffled with " + numDecks + " deck(s)! Ready to deal.");
        refreshAllUI();
    }

    private void promptBuyIn() {
        String input = JOptionPane.showInputDialog(this, "Enter chip amount to add to your balance:", "500");
        if (input != null && !input.trim().isEmpty()) {
            try {
                int amount = Integer.parseInt(input.trim());
                if (amount > 0) {
                    player.addChips(amount);
                    log("Added $" + amount + " chips. New balance: $" + player.getChips());
                    refreshAllUI();
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter a valid positive number.", "Invalid Amount", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void startNewRound() {
        int betAmount = (int) betSpinner.getValue();
        if (player.getChips() < betAmount) {
            JOptionPane.showMessageDialog(this,
                    "Not enough chips for a $" + betAmount + " bet! Use 'Buy-In' or lower your bet.",
                    "Insufficient Chips", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Auto-shuffle if shoe has fewer than 15 cards
        if (deck.cardsRemaining() < 15) {
            int numDecks = (int) decksSpinner.getValue();
            deck.ShuffleDeck(numDecks);
            log("--- Shoe low on cards! Auto-shuffled " + numDecks + " deck(s). Count reset to 0. ---");
        }

        roundActive = true;
        dealerCardRevealed = false;
        currentHandIndex = 0;

        player.setInitialBet(betAmount);
        player.resetForNewRound();
        player.deductChips(betAmount);

        dealerHand.clear();

        // Deal 2 cards to Player (both face up & tracked in count)
        String p1 = deck.drawCardAndTrack();
        String p2 = deck.drawCardAndTrack();
        player.getPrimaryHand().addCard(p1);
        player.getPrimaryHand().addCard(p2);

        // Deal 2 cards to Dealer (1st face up & tracked; 2nd face down, tracked only when revealed)
        String d1 = deck.drawCardAndTrack();
        String d2 = deck.drawCard(); // hole card
        dealerHand.addCard(d1);
        dealerHand.addCard(d2);

        log("==========================================");
        log("New Round Started | Bet: $" + betAmount + " | Dealer shows: " + Hand.formatSingleCard(d1));
        log("Your Hand: " + player.getPrimaryHand().formatCards() + " (Score: " + player.getPrimaryHand().calculateScore() + ")");

        // Check for immediate Natural Blackjack
        if (player.hasBlackjack(0)) {
            statusBannerLabel.setText("NATURAL BLACKJACK! Checking Dealer's hand...");
            revealDealerAndFinishRound();
            return;
        }

        statusBannerLabel.setText("Playing Hand 1 — Choose Hit, Stand, Double Down, or Split");
        refreshAllUI();
    }

    private void handleHit() {
        if (!roundActive) return;

        evaluateAndLogPlayToConsole(PlayerAction.HIT);

        String drawn = player.hit(deck, currentHandIndex);
        Hand activeHand = player.getHand(currentHandIndex);
        int score = activeHand.calculateScore();

        log("Hand " + (currentHandIndex + 1) + " HIT -> Drew " + Hand.formatSingleCard(drawn) + " | Score: " + score);

        if (activeHand.isBusted()) {
            log("Hand " + (currentHandIndex + 1) + " BUSTED with " + score + "!");
            statusBannerLabel.setText("Hand " + (currentHandIndex + 1) + " Busted (" + score + ")!");
            advanceToNextHandOrDealer();
        } else if (score == 21) {
            log("Hand " + (currentHandIndex + 1) + " reached 21! Auto-standing.");
            advanceToNextHandOrDealer();
        } else {
            statusBannerLabel.setText("Playing Hand " + (currentHandIndex + 1) + " (Score: " + score + ")");
            refreshAllUI();
        }
    }

    private void handleStand() {
        if (!roundActive) return;

        evaluateAndLogPlayToConsole(PlayerAction.STAND);

        Hand activeHand = player.getHand(currentHandIndex);
        player.stand(currentHandIndex);
        log("Hand " + (currentHandIndex + 1) + " STANDS at " + activeHand.calculateScore() + ".");
        advanceToNextHandOrDealer();
    }

    private void handleDoubleDown() {
        if (!roundActive || !player.canDouble(currentHandIndex)) return;

        evaluateAndLogPlayToConsole(PlayerAction.DOUBLE);

        String drawn = player.doubleHand(deck, currentHandIndex);
        Hand activeHand = player.getHand(currentHandIndex);
        int score = activeHand.calculateScore();
        int newBet = player.getHandBet(currentHandIndex);

        log("Hand " + (currentHandIndex + 1) + " DOUBLED DOWN (Bet: $" + newBet + ") -> Drew " + Hand.formatSingleCard(drawn) + " | Final Score: " + score);

        if (activeHand.isBusted()) {
            log("Hand " + (currentHandIndex + 1) + " BUSTED on Double Down (" + score + ")!");
        }
        advanceToNextHandOrDealer();
    }

    private void handleSplit() {
        if (!roundActive || !player.canSplit(currentHandIndex)) return;

        evaluateAndLogPlayToConsole(PlayerAction.SPLIT);

        boolean ok = player.split(deck, currentHandIndex);
        if (ok) {
            log("Hand " + (currentHandIndex + 1) + " SPLIT into " + player.getHandCount() + " hands!");
            for (int i = 0; i < player.getHandCount(); i++) {
                Hand h = player.getHand(i);
                log("  -> Hand " + (i + 1) + ": " + h.formatCards() + " (Score: " + h.calculateScore() + ", Bet: $" + player.getHandBet(i) + ")");
            }
            statusBannerLabel.setText("Split successful! Playing Hand " + (currentHandIndex + 1));
            refreshAllUI();
        }
    }

    private void advanceToNextHandOrDealer() {
        if (currentHandIndex + 1 < player.getHandCount()) {
            currentHandIndex++;
            Hand nextHand = player.getHand(currentHandIndex);
            statusBannerLabel.setText("Now playing Hand " + (currentHandIndex + 1) + " (Score: " + nextHand.calculateScore() + ")");
            refreshAllUI();
        } else {
            revealDealerAndFinishRound();
        }
    }

    private void revealDealerAndFinishRound() {
        dealerCardRevealed = true;
        // Track the dealer's hole card now that it is turned face-up
        if (dealerHand.getCardsCount() >= 2) {
            String holeCard = dealerHand.getCard(1);
            deck.trackCount(holeCard);
            log("Dealer reveals hole card: " + Hand.formatSingleCard(holeCard)
                    + " -> Dealer Hand: " + dealerHand.formatCards() + " (Score: " + dealerHand.calculateScore() + ")");
        }

        // Dealer only draws if at least one player hand is not busted and not a natural blackjack
        boolean anyHandNeedsDealerPlay = false;
        for (int i = 0; i < player.getHandCount(); i++) {
            if (!player.isBusted(i) && !(player.hasBlackjack(i) && player.getHandCount() == 1)) {
                anyHandNeedsDealerPlay = true;
                break;
            }
        }

        if (anyHandNeedsDealerPlay) {
            while (dealerHand.calculateScore() < 17) {
                String drawn = deck.drawCardAndTrack();
                dealerHand.addCard(drawn);
                log("Dealer draws " + Hand.formatSingleCard(drawn) + " -> Dealer Score: " + dealerHand.calculateScore());
            }
        }

        int dScore = dealerHand.calculateScore();
        boolean dealerBlackjack = (dealerHand.getCardsCount() == 2 && dScore == 21);
        StringBuilder summary = new StringBuilder();

        for (int i = 0; i < player.getHandCount(); i++) {
            Hand pHand = player.getHand(i);
            int pScore = pHand.calculateScore();
            int bet = player.getHandBet(i);
            String handLabel = player.getHandCount() > 1 ? "Hand " + (i + 1) : "Player";

            if (pHand.isBusted()) {
                String msg = handLabel + " Busted (-$" + bet + ")";
                log("Result: " + msg);
                summary.append(msg).append("   ");
            } else if (player.hasBlackjack(i) && player.getHandCount() == 1) {
                if (dealerBlackjack) {
                    player.pushBet(i);
                    String msg = "Push (Both have Blackjack)";
                    log("Result: " + msg);
                    summary.append(msg).append("   ");
                } else {
                    int payout = player.winBet(i, 2.5); // 3:2 Blackjack payout
                    String msg = "BLACKJACK! Won +$" + (payout - bet);
                    log("Result: " + msg + " (Total payout: $" + payout + ")");
                    summary.append(msg).append("   ");
                }
            } else if (dScore > 21) {
                player.winBet(i, 2.0);
                String msg = handLabel + " Wins! Dealer Busted (+$" + bet + ")";
                log("Result: " + msg);
                summary.append(msg).append("   ");
            } else if (pScore > dScore) {
                player.winBet(i, 2.0);
                String msg = handLabel + " Wins " + pScore + " vs " + dScore + " (+$" + bet + ")";
                log("Result: " + msg);
                summary.append(msg).append("   ");
            } else if (pScore < dScore) {
                String msg = "Dealer Wins " + dScore + " vs " + pScore + " (-$" + bet + ")";
                log("Result: " + msg);
                summary.append(msg).append("   ");
            } else {
                player.pushBet(i);
                String msg = handLabel + " Push at " + pScore + " (Bet Returned)";
                log("Result: " + msg);
                summary.append(msg).append("   ");
            }
        }

        roundActive = false;
        statusBannerLabel.setText(summary.toString().trim());
        refreshAllUI();
    }

    // ==================== UI RENDERING ====================

    private void refreshAllUI() {
        // Update top badges
        chipsLabel.setText("Chips: $" + player.getChips());
        betLabel.setText("Active Bet: $" + (roundActive ? player.getTotalActiveBets() : betSpinner.getValue()));
        countLabel.setText("Running Count: " + (showRunningCount ? deck.getCount() : "HIDDEN"));
        cardsRemainingLabel.setText("Cards Left: " + (showCardsRemaining ? deck.cardsRemaining() : "HIDDEN"));

        // Update Basic Strategy Assistor Banner
        strategyAssistorBanner.setVisible(showStrategyAssistor);
        if (showStrategyAssistor) {
            if (roundActive && dealerHand.getCardsCount() > 0) {
                Hand activeHand = player.getHand(currentHandIndex);
                String dealerUpCard = dealerHand.getCard(0);
                Advice advice = BasicStrategyAssistor.getBestPlay(
                        activeHand,
                        dealerUpCard,
                        player.canSplit(currentHandIndex),
                        player.canDouble(currentHandIndex)
                );
                strategyAssistorBanner.setText("★ Basic Strategy Assistor: Best Play is "
                        + advice.recommendedAction() + " — [" + advice.explanation() + "]");
            } else {
                strategyAssistorBanner.setText("★ Basic Strategy Assistor: Deal a hand to see real-time optimal play advice.");
            }
        }

        // Update Dealer Cards
        dealerCardsPanel.removeAll();
        if (dealerHand.getCardsCount() == 0) {
            dealerScoreLabel.setText("DEALER HAND");
        } else {
            for (int i = 0; i < dealerHand.getCardsCount(); i++) {
                if (i == 1 && !dealerCardRevealed) {
                    dealerCardsPanel.add(createCardVisual("??", true));
                } else {
                    dealerCardsPanel.add(createCardVisual(dealerHand.getCard(i), false));
                }
            }
            if (dealerCardRevealed) {
                dealerScoreLabel.setText("DEALER HAND — Score: " + dealerHand.calculateScore());
            } else {
                String upCard = dealerHand.getCard(0);
                dealerScoreLabel.setText("DEALER SHOWS: " + Hand.formatSingleCard(upCard));
            }
        }

        // Update Player Hands
        playerHandsContainer.removeAll();
        List<Hand> hands = player.getMyHands();
        for (int i = 0; i < hands.size(); i++) {
            Hand h = hands.get(i);
            boolean isCurrentTurn = roundActive && (i == currentHandIndex);
            playerHandsContainer.add(createPlayerHandBox(h, i, isCurrentTurn));
        }

        // Update Buttons state
        dealButton.setEnabled(!roundActive);
        betSpinner.setEnabled(!roundActive);
        decksSpinner.setEnabled(!roundActive);
        shuffleButton.setEnabled(!roundActive);

        hitButton.setEnabled(roundActive);
        standButton.setEnabled(roundActive);
        doubleButton.setEnabled(roundActive && player.canDouble(currentHandIndex));
        splitButton.setEnabled(roundActive && player.canSplit(currentHandIndex));

        revalidate();
        repaint();
    }

    private JPanel createPlayerHandBox(Hand hand, int handIndex, boolean isActiveTurn) {
        JPanel box = new JPanel(new BorderLayout(6, 6));
        box.setOpaque(true);
        box.setBackground(isActiveTurn ? new Color(22, 101, 52) : new Color(20, 83, 45));

        Color borderColor = isActiveTurn ? new Color(250, 204, 21) : new Color(74, 222, 128, 100);
        int borderThickness = isActiveTurn ? 3 : 1;
        box.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(borderColor, borderThickness, true),
                new EmptyBorder(10, 12, 10, 12)
        ));

        int score = hand.getCardsCount() > 0 ? hand.calculateScore() : 0;
        int bet = player.getHandBet(handIndex);
        String headerText = "HAND " + (handIndex + 1)
                + (hand.getCardsCount() > 0 ? "  |  Score: " + score + "  |  Bet: $" + bet : "");
        if (hand.isBusted()) {
            headerText += " (BUSTED)";
        }

        JLabel title = new JLabel(headerText, SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        title.setForeground(isActiveTurn ? new Color(254, 240, 138) : Color.WHITE);

        JPanel cardsFlow = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));
        cardsFlow.setOpaque(false);

        for (String card : hand.getPlayerCards()) {
            cardsFlow.add(createCardVisual(card, false));
        }

        box.add(title, BorderLayout.NORTH);
        box.add(cardsFlow, BorderLayout.CENTER);
        return box;
    }

    private JPanel createCardVisual(String rawCard, boolean faceDown) {
        JPanel cardPanel = new JPanel(new BorderLayout());
        cardPanel.setPreferredSize(new Dimension(78, 108));
        cardPanel.setBorder(new LineBorder(new Color(30, 41, 59), 2, true));

        if (faceDown) {
            cardPanel.setBackground(new Color(30, 58, 138));
            JLabel backLabel = new JLabel("♠ ♥ ♦ ♣", SwingConstants.CENTER);
            backLabel.setForeground(new Color(147, 197, 253));
            backLabel.setFont(new Font("Segoe UI Symbol", Font.BOLD, 14));
            cardPanel.add(backLabel, BorderLayout.CENTER);
            return cardPanel;
        }

        cardPanel.setBackground(Color.WHITE);
        String formatted = Hand.formatSingleCard(rawCard);
        boolean isRed = rawCard != null && (rawCard.endsWith("H") || rawCard.endsWith("D"));
        Color textColor = isRed ? new Color(220, 38, 38) : new Color(15, 23, 42);

        JLabel topRank = new JLabel(" " + formatted);
        topRank.setFont(new Font("Segoe UI Symbol", Font.BOLD, 14));
        topRank.setForeground(textColor);

        JLabel centerSuit = new JLabel(formatted.substring(formatted.length() - 1), SwingConstants.CENTER);
        centerSuit.setFont(new Font("Segoe UI Symbol", Font.BOLD, 30));
        centerSuit.setForeground(textColor);

        JLabel bottomRank = new JLabel(formatted + " ", SwingConstants.RIGHT);
        bottomRank.setFont(new Font("Segoe UI Symbol", Font.BOLD, 14));
        bottomRank.setForeground(textColor);

        cardPanel.add(topRank, BorderLayout.NORTH);
        cardPanel.add(centerSuit, BorderLayout.CENTER);
        cardPanel.add(bottomRank, BorderLayout.SOUTH);
        return cardPanel;
    }

    private JLabel createStatBadge(String text, Color accentColor) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setForeground(Color.WHITE);
        label.setOpaque(true);
        label.setBackground(new Color(30, 41, 59));
        label.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(accentColor, 2, true),
                new EmptyBorder(6, 10, 6, 10)
        ));
        return label;
    }

    private JButton createActionButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setPreferredSize(new Dimension(160, 42));
        return btn;
    }

    private JButton createSmallButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        return btn;
    }

    private void log(String message) {
        logArea.append(message + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }
}
