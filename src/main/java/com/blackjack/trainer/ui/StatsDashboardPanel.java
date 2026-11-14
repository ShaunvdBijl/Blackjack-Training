package com.blackjack.trainer.ui;

import com.blackjack.trainer.service.BlackjackDatabaseService;
import com.blackjack.trainer.service.BlackjackDatabaseService.ErrorCategoryCount;
import com.blackjack.trainer.service.BlackjackDatabaseService.OverallStats;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.geom.Path2D;
import java.util.List;

/**
 * Interactive visual Analytics Dashboard panel rendered with Java2D.
 * Displays KPI Summary Cards, Cumulative Bankroll Profit Line Chart,
 * Win/Loss/Push Distribution Bar, and Basic Strategy Error Breakdown.
 */
public class StatsDashboardPanel extends JPanel {
    private final BlackjackDatabaseService dbService;

    private JLabel handsValueLabel;
    private JLabel winRateValueLabel;
    private JLabel netProfitValueLabel;
    private JLabel errorsValueLabel;

    private ProfitChartCanvas profitChartCanvas;
    private BreakdownCanvas breakdownCanvas;

    public StatsDashboardPanel(BlackjackDatabaseService dbService) {
        this.dbService = dbService;
        setLayout(new BorderLayout(12, 12));
        setBackground(new Color(15, 23, 42));
        setBorder(new EmptyBorder(14, 14, 14, 14));

        // 1. Top KPI Cards Row
        JPanel kpiRow = new JPanel(new GridLayout(1, 4, 12, 0));
        kpiRow.setOpaque(false);

        handsValueLabel = createValueLabel("0");
        winRateValueLabel = createValueLabel("0.0%");
        netProfitValueLabel = createValueLabel("+$0");
        errorsValueLabel = createValueLabel("0");

        kpiRow.add(createKpiCard("TOTAL HANDS PLAYED", handsValueLabel, new Color(59, 130, 246)));
        kpiRow.add(createKpiCard("WIN RATE (EXCL. PUSH)", winRateValueLabel, new Color(16, 185, 129)));
        kpiRow.add(createKpiCard("LIFETIME NET PROFIT", netProfitValueLabel, new Color(245, 158, 11)));
        kpiRow.add(createKpiCard("STRATEGY MISTAKES", errorsValueLabel, new Color(239, 68, 68)));

        add(kpiRow, BorderLayout.NORTH);

        // 2. Center Visual Charts Area (Left: Cumulative Bankroll Chart, Right: Outcomes & Error Breakdown)
        JPanel chartsGrid = new JPanel(new GridLayout(1, 2, 14, 0));
        chartsGrid.setOpaque(false);

        profitChartCanvas = new ProfitChartCanvas();
        breakdownCanvas = new BreakdownCanvas();

        chartsGrid.add(profitChartCanvas);
        chartsGrid.add(breakdownCanvas);

        add(chartsGrid, BorderLayout.CENTER);
    }

    public void refreshData() {
        OverallStats stats = dbService.getOverallStats();
        List<Integer> profitSeries = dbService.getCumulativeProfitSeries();
        List<ErrorCategoryCount> handTypeErrors = dbService.getErrorBreakdownByHandType();
        List<ErrorCategoryCount> topMistakes = dbService.getTopStrategyMistakes(5);

        handsValueLabel.setText(String.valueOf(stats.totalHandsPlayed()));

        int decisiveHands = stats.wins() + stats.losses();
        double winRate = decisiveHands > 0 ? (stats.wins() * 100.0 / decisiveHands) : 0.0;
        winRateValueLabel.setText(String.format("%.1f%% (%dW / %dL)", winRate, stats.wins(), stats.losses()));

        String netStr = stats.netProfit() >= 0 ? "+$" + stats.netProfit() : "-$" + Math.abs(stats.netProfit());
        netProfitValueLabel.setText(netStr);
        netProfitValueLabel.setForeground(stats.netProfit() >= 0 ? new Color(74, 222, 128) : new Color(248, 113, 113));

        errorsValueLabel.setText(String.valueOf(stats.totalStrategyErrors()));

        profitChartCanvas.setSeries(profitSeries);
        breakdownCanvas.setData(stats, handTypeErrors, topMistakes);

        revalidate();
        repaint();
    }

    private JPanel createKpiCard(String title, JLabel valueLabel, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(4, 4));
        card.setBackground(new Color(30, 41, 59));
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(accentColor, 2, true),
                new EmptyBorder(10, 14, 10, 14)
        ));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        titleLbl.setForeground(new Color(148, 163, 184));

        card.add(titleLbl, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    private JLabel createValueLabel(String initial) {
        JLabel lbl = new JLabel(initial);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lbl.setForeground(Color.WHITE);
        return lbl;
    }

    // ==================== CUMULATIVE PROFIT LINE CHART ====================

    private static class ProfitChartCanvas extends JPanel {
        private List<Integer> series = List.of(0);

        public ProfitChartCanvas() {
            setBackground(new Color(30, 41, 59));
            setBorder(new LineBorder(new Color(51, 65, 85), 1, true));
        }

        public void setSeries(List<Integer> series) {
            this.series = (series == null || series.isEmpty()) ? List.of(0) : series;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int padLeft = 58, padRight = 24, padTop = 44, padBottom = 38;

            // Title
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
            g2.drawString("Cumulative Profit / Loss Progression ($)", 16, 25);

            int plotW = w - padLeft - padRight;
            int plotH = h - padTop - padBottom;
            if (plotW <= 20 || plotH <= 20) {
                g2.dispose();
                return;
            }

            int minVal = 0;
            int maxVal = 100;
            for (int v : series) {
                if (v < minVal) minVal = v;
                if (v > maxVal) maxVal = v;
            }
            if (maxVal == minVal) {
                maxVal += 100;
                minVal -= 100;
            } else {
                int margin = Math.max(20, (int) ((maxVal - minVal) * 0.12));
                maxVal += margin;
                minVal -= margin;
            }

            // Draw grid lines & Y axis labels
            g2.setFont(new Font("Consolas", Font.PLAIN, 11));
            int gridSteps = 4;
            for (int i = 0; i <= gridSteps; i++) {
                int y = padTop + (i * plotH / gridSteps);
                int val = maxVal - (i * (maxVal - minVal) / gridSteps);

                g2.setColor(new Color(51, 65, 85));
                g2.drawLine(padLeft, y, padLeft + plotW, y);

                g2.setColor(new Color(148, 163, 184));
                String label = (val >= 0 ? "+$" : "-$") + Math.abs(val);
                g2.drawString(label, 8, y + 4);
            }

            // Draw $0 baseline if within range
            int zeroY = padTop + (int) ((double) (maxVal - 0) / (maxVal - minVal) * plotH);
            g2.setColor(new Color(148, 163, 184, 160));
            g2.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{5, 5}, 0));
            g2.drawLine(padLeft, zeroY, padLeft + plotW, zeroY);

            // Plot series line
            int n = series.size();
            if (n > 1) {
                Path2D path = new Path2D.Double();
                for (int i = 0; i < n; i++) {
                    double x = padLeft + (i * (double) plotW / (n - 1));
                    double y = padTop + ((double) (maxVal - series.get(i)) / (maxVal - minVal) * plotH);
                    if (i == 0) {
                        path.moveTo(x, y);
                    } else {
                        path.lineTo(x, y);
                    }
                }

                int lastVal = series.get(n - 1);
                Color lineColor = lastVal >= 0 ? new Color(16, 185, 129) : new Color(239, 68, 68);
                g2.setColor(lineColor);
                g2.setStroke(new BasicStroke(2.6f));
                g2.draw(path);

                // Draw endpoint dot
                int endX = padLeft + plotW;
                int endY = padTop + (int) ((double) (maxVal - lastVal) / (maxVal - minVal) * plotH);
                g2.fillOval(endX - 4, endY - 4, 8, 8);
            } else {
                g2.setColor(new Color(148, 163, 184));
                g2.setFont(new Font("Segoe UI", Font.ITALIC, 13));
                g2.drawString("Play hands to see your bankroll curve!", padLeft + 30, padTop + plotH / 2);
            }

            // X-axis label
            g2.setColor(new Color(148, 163, 184));
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            g2.drawString("Hands Played (0 -> " + (n - 1) + ")", padLeft + plotW / 2 - 55, h - 12);

            g2.dispose();
        }
    }

    // ==================== OUTCOMES & STRATEGY ERRORS BREAKDOWN ====================

    private static class BreakdownCanvas extends JPanel {
        private OverallStats stats = new OverallStats(0, 0, 0, 0, 0, 0, 0, 0);
        private List<ErrorCategoryCount> handTypeErrors = List.of();
        private List<ErrorCategoryCount> topMistakes = List.of();

        public BreakdownCanvas() {
            setBackground(new Color(30, 41, 59));
            setBorder(new LineBorder(new Color(51, 65, 85), 1, true));
        }

        public void setData(OverallStats stats, List<ErrorCategoryCount> handTypeErrors, List<ErrorCategoryCount> topMistakes) {
            this.stats = stats;
            this.handTypeErrors = handTypeErrors;
            this.topMistakes = topMistakes;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int pad = 18;
            int y = 25;

            // Section 1: Hand Outcomes Stacked Bar
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
            g2.drawString("Hand Outcomes Distribution", pad, y);
            y += 12;

            int barW = w - pad * 2;
            int barH = 24;
            int total = Math.max(1, stats.totalHandsPlayed());
            int winW = (int) Math.round(stats.wins() * (double) barW / total);
            int pushW = (int) Math.round(stats.pushes() * (double) barW / total);
            int lossW = Math.max(0, barW - winW - pushW);

            if (stats.totalHandsPlayed() == 0) {
                g2.setColor(new Color(51, 65, 85));
                g2.fillRoundRect(pad, y, barW, barH, 6, 6);
            } else {
                g2.setColor(new Color(16, 185, 129));
                g2.fillRect(pad, y, winW, barH);
                g2.setColor(new Color(245, 158, 11));
                g2.fillRect(pad + winW, y, pushW, barH);
                g2.setColor(new Color(239, 68, 68));
                g2.fillRect(pad + winW + pushW, y, lossW, barH);
            }

            y += barH + 16;
            g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
            g2.setColor(new Color(74, 222, 128));
            g2.drawString("● Wins: " + stats.wins(), pad, y);
            g2.setColor(new Color(251, 191, 36));
            g2.drawString("● Pushes: " + stats.pushes(), pad + 110, y);
            g2.setColor(new Color(248, 113, 113));
            g2.drawString("● Losses: " + stats.losses(), pad + 230, y);

            // Section 2: Errors by Hand Category
            y += 28;
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
            g2.drawString("Basic Strategy Errors by Hand Type", pad, y);
            y += 16;

            if (handTypeErrors.isEmpty()) {
                g2.setColor(new Color(148, 163, 184));
                g2.setFont(new Font("Segoe UI", Font.ITALIC, 12));
                g2.drawString("No Basic Strategy errors recorded yet!", pad, y);
                y += 22;
            } else {
                int maxErr = Math.max(1, handTypeErrors.get(0).errorCount());
                for (ErrorCategoryCount item : handTypeErrors) {
                    g2.setColor(new Color(226, 232, 240));
                    g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                    g2.drawString(item.category() + " (" + item.errorCount() + ")", pad, y);

                    int fillW = (int) ((item.errorCount() / (double) maxErr) * (barW - 170));
                    g2.setColor(new Color(139, 92, 246));
                    g2.fillRoundRect(pad + 155, y - 11, Math.max(8, fillW), 13, 4, 4);
                    y += 20;
                }
            }

            // Section 3: Top Weak Spots (Most Frequent Mistakes)
            y += 14;
            g2.setColor(new Color(254, 240, 138));
            g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
            g2.drawString("Top Weak Spots (Most Frequent Mistakes)", pad, y);
            y += 18;

            if (topMistakes.isEmpty()) {
                g2.setColor(new Color(148, 163, 184));
                g2.setFont(new Font("Segoe UI", Font.ITALIC, 12));
                g2.drawString("Keep playing — any recurring mistakes will be highlighted here.", pad, y);
            } else {
                g2.setFont(new Font("Consolas", Font.PLAIN, 11));
                for (ErrorCategoryCount mistake : topMistakes) {
                    g2.setColor(new Color(248, 113, 113));
                    String badge = "[" + mistake.errorCount() + "x] ";
                    g2.drawString(badge, pad, y);

                    g2.setColor(new Color(226, 232, 240));
                    String text = mistake.category();
                    if (text.length() > 54) {
                        text = text.substring(0, 51) + "...";
                    }
                    g2.drawString(text, pad + 36, y);
                    y += 18;
                }
            }

            g2.dispose();
        }
    }
}
