package com.blackjack.trainer.model;

import com.blackjack.trainer.model.Player.PlayerAction;

public class BasicStrategyAssistor {

    public record Advice(PlayerAction recommendedAction, String chartCode, String explanation) {
    }

    /**
     * Evaluates the player's current hand and dealer's upcard against the Basic Strategy chart.
     *
     * @param hand         The player's active hand
     * @param dealerUpCard The dealer's visible upcard (e.g., "06S", "01H", "12D")
     * @param canSplit     Whether splitting is currently allowed (2 matching cards + enough chips)
     * @param canDouble    Whether doubling down is currently allowed (2 cards + enough chips)
     * @return Advice containing the optimal PlayerAction, chart code, and human-readable explanation
     */
    public static Advice getBestPlay(Hand hand, String dealerUpCard, boolean canSplit, boolean canDouble) {
        if (hand == null || hand.getCardsCount() < 2 || dealerUpCard == null) {
            return new Advice(PlayerAction.HIT, "H", "Hit");
        }

        int dealerVal = Hand.getCardBjValue(dealerUpCard); // 2..10, or 11 for Ace

        // 1. Check Pair Splitting first (if hand has 2 cards of same rank or both 10-value if applicable)
        if (hand.getCardsCount() == 2 && canSplit && hand.canSplit()) {
            int pairVal = Hand.getCardBjValue(hand.getCard(0)); // 2..10, or 11 for Ace
            String pairCode = getPairSplittingCode(pairVal, dealerVal);
            // Both "Y" and "Y/N" (since Double After Split is allowed in this trainer) recommend splitting
            if ("Y".equals(pairCode) || "Y/N".equals(pairCode)) {
                String pairName = (pairVal == 11) ? "A,A" : (pairVal == 10 ? "T,T" : pairVal + "," + pairVal);
                String dealerName = (dealerVal == 11) ? "A" : String.valueOf(dealerVal);
                String reason = "Pair of " + pairName + " vs Dealer " + dealerName + " -> Split Pair (" + pairCode + ")";
                return new Advice(PlayerAction.SPLIT, pairCode, reason);
            }
        }

        int score = hand.calculateScore();
        boolean isSoft = hand.isSoft();
        String dealerName = (dealerVal == 11) ? "A" : String.valueOf(dealerVal);

        // 2. Check Soft Totals (Ace counted as 11)
        if (isSoft && score >= 13 && score <= 20) {
            int softOtherCard = score - 11; // 2 through 9 (A,2 through A,9)
            String softCode = getSoftTotalCode(softOtherCard, dealerVal);
            return resolveCode(softCode, canDouble, "Soft " + score + " (A," + softOtherCard + ") vs Dealer " + dealerName);
        }

        // 3. Check Hard Totals
        String hardCode = getHardTotalCode(score, dealerVal);
        return resolveCode(hardCode, canDouble, "Hard " + score + " vs Dealer " + dealerName);
    }

    private static Advice resolveCode(String code, boolean canDouble, String contextDesc) {
        return switch (code) {
            case "S" -> new Advice(
                    PlayerAction.STAND,
                    "S",
                    contextDesc + " -> Stand (S)"
            );
            case "D" -> {
                if (canDouble) {
                    yield new Advice(
                            PlayerAction.DOUBLE,
                            "D",
                            contextDesc + " -> Double Down (D: Double if allowed, otherwise Hit)"
                    );
                } else {
                    yield new Advice(
                            PlayerAction.HIT,
                            "D->H",
                            contextDesc + " -> Hit (D: Double not allowed on >2 cards, so Hit)"
                    );
                }
            }
            case "Ds" -> {
                if (canDouble) {
                    yield new Advice(
                            PlayerAction.DOUBLE,
                            "Ds",
                            contextDesc + " -> Double Down (Ds: Double if allowed, otherwise Stand)"
                    );
                } else {
                    yield new Advice(
                            PlayerAction.STAND,
                            "Ds->S",
                            contextDesc + " -> Stand (Ds: Double not allowed on >2 cards, so Stand)"
                    );
                }
            }
            default -> new Advice(
                    PlayerAction.HIT,
                    "H",
                    contextDesc + " -> Hit (H)"
            );
        };
    }

    /**
     * Pair Splitting Table from basicStrategy.png:
     * Dealer Upcard: 2, 3, 4, 5, 6, 7, 8, 9, 10, A(11)
     */
    private static String getPairSplittingCode(int pairVal, int dealerVal) {
        return switch (pairVal) {
            case 11 -> "Y"; // A,A: Y across all 2..A
            case 10 -> "N"; // T,T: N across all 2..A
            case 9 -> (dealerVal == 7 || dealerVal == 10 || dealerVal == 11) ? "N" : "Y"; // 9,9: Y on 2-6, 8-9; N on 7, 10, A
            case 8 -> "Y"; // 8,8: Y across all 2..A
            case 7 -> (dealerVal >= 2 && dealerVal <= 7) ? "Y" : "N"; // 7,7: Y on 2-7; N on 8-A
            case 6 -> (dealerVal == 2) ? "Y/N" : ((dealerVal >= 3 && dealerVal <= 6) ? "Y" : "N"); // 6,6: Y/N on 2; Y on 3-6; N on 7-A
            case 5 -> "N"; // 5,5: N across all 2..A (treated as hard 10)
            case 4 -> (dealerVal == 5 || dealerVal == 6) ? "Y/N" : "N"; // 4,4: Y/N on 5-6; N otherwise
            case 3, 2 -> (dealerVal == 2 || dealerVal == 3) ? "Y/N" : ((dealerVal >= 4 && dealerVal <= 7) ? "Y" : "N"); // 2,2 & 3,3: Y/N on 2-3; Y on 4-7; N on 8-A
            default -> "N";
        };
    }

    /**
     * Soft Totals Table from basicStrategy.png:
     * softOther is 2..9 corresponding to A,2 (Soft 13) through A,9 (Soft 20)
     */
    private static String getSoftTotalCode(int softOther, int dealerVal) {
        return switch (softOther) {
            case 9 -> "S"; // A,9 (20): S across all 2..A
            case 8 -> (dealerVal == 6) ? "Ds" : "S"; // A,8 (19): Ds on 6, S otherwise
            case 7 -> { // A,7 (18): Ds on 2-6; S on 7-8; H on 9, 10, A
                if (dealerVal >= 2 && dealerVal <= 6) yield "Ds";
                if (dealerVal == 7 || dealerVal == 8) yield "S";
                yield "H";
            }
            case 6 -> (dealerVal >= 3 && dealerVal <= 6) ? "D" : "H"; // A,6 (17): D on 3-6, H otherwise
            case 5, 4 -> (dealerVal >= 4 && dealerVal <= 6) ? "D" : "H"; // A,5 (16) & A,4 (15): D on 4-6, H otherwise
            case 3, 2 -> (dealerVal >= 5 && dealerVal <= 6) ? "D" : "H"; // A,3 (14) & A,2 (13): D on 5-6, H otherwise
            default -> "S";
        };
    }

    /**
     * Hard Totals Table:
     * 17+: S across all
     * 16..13: S vs 2-6, H vs 7-A
     * 12: S vs 4-6, H vs 2-3 and 7-A
     * 11: D vs 2-A (or 2-10)
     * 10: D vs 2-9, H vs 10-A
     * 9: D vs 3-6, H otherwise
     * <=8: H across all
     */
    private static String getHardTotalCode(int score, int dealerVal) {
        if (score >= 17) {
            return "S";
        }
        return switch (score) {
            case 16, 15, 14, 13 -> (dealerVal >= 2 && dealerVal <= 6) ? "S" : "H";
            case 12 -> (dealerVal >= 4 && dealerVal <= 6) ? "S" : "H";
            case 11 -> "D";
            case 10 -> (dealerVal >= 2 && dealerVal <= 9) ? "D" : "H";
            case 9 -> (dealerVal >= 3 && dealerVal <= 6) ? "D" : "H";
            default -> "H"; // 8 or less
        };
    }
}
