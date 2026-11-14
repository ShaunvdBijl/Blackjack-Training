package com.blackjack.trainer.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class BlackjackDatabaseService {
    private static final Logger log = LoggerFactory.getLogger(BlackjackDatabaseService.class);

    @Value("${spring.datasource.url:}")
    private String jdbcUrl;

    @Value("${spring.datasource.username:}")
    private String dbUsername;

    @Value("${spring.datasource.password:}")
    private String dbPassword;

    private boolean connected = false;
    private String lastErrorMessage = "Not connected";

    public record OverallStats(
            int totalHandsPlayed,
            int wins,
            int losses,
            int pushes,
            int netProfit,
            int totalWinnings,
            int totalLossesAmount,
            int totalStrategyErrors
    ) {}

    public record RoundRecord(
            long id,
            String playerHand,
            int playerScore,
            String dealerHand,
            int dealerScore,
            int betAmount,
            String outcome,
            int netChips,
            String playedAt
    ) {}

    public record StrategyErrorRecord(
            long id,
            String playerHand,
            int playerScore,
            String dealerUpcard,
            String chosenAction,
            String recommendedAction,
            String explanation,
            String occurredAt
    ) {}

    public record ErrorCategoryCount(
            String category,
            int errorCount
    ) {}

    @PostConstruct
    public void initDatabase() {
        connectAndCreateSchema();
    }

    public synchronized boolean connectAndCreateSchema() {
        if (jdbcUrl == null || jdbcUrl.isBlank()) {
            this.connected = false;
            this.lastErrorMessage = "No DB_URL configured in .env";
            System.out.println("[PostgreSQL Warning] No DB_URL configured in .env file.");
            return false;
        }
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS round_results (
                    id SERIAL PRIMARY KEY,
                    player_hand VARCHAR(120) NOT NULL,
                    player_score INT NOT NULL,
                    dealer_hand VARCHAR(120) NOT NULL,
                    dealer_score INT NOT NULL,
                    bet_amount INT NOT NULL,
                    outcome VARCHAR(30) NOT NULL,
                    net_chips INT NOT NULL,
                    played_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS basic_strategy_errors (
                    id SERIAL PRIMARY KEY,
                    player_hand VARCHAR(120) NOT NULL,
                    player_score INT NOT NULL,
                    dealer_upcard VARCHAR(20) NOT NULL,
                    chosen_action VARCHAR(30) NOT NULL,
                    recommended_action VARCHAR(30) NOT NULL,
                    explanation VARCHAR(255) NOT NULL,
                    occurred_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """);

            this.connected = true;
            this.lastErrorMessage = null;
            log.info("Successfully connected to PostgreSQL ({}) and verified Blackjack schema.", jdbcUrl);
            System.out.println("[PostgreSQL] Connected to " + jdbcUrl + " — Tables 'round_results' and 'basic_strategy_errors' ready.");
            return true;
        } catch (SQLException e) {
            this.connected = false;
            this.lastErrorMessage = e.getMessage();
            log.warn("Could not connect to PostgreSQL at {}: {}", jdbcUrl, e.getMessage());
            System.out.println("[PostgreSQL Warning] Database not connected (" + e.getMessage() + "). Use 'DB Stats / Config' in the UI to configure your Postgres password/URL.");
            return false;
        }
    }

    public synchronized boolean updateConnectionSettings(String url, String username, String password) {
        this.jdbcUrl = url;
        this.dbUsername = username;
        this.dbPassword = password;
        return connectAndCreateSchema();
    }

    private Connection getConnection() throws SQLException {
        DriverManager.setLoginTimeout(3);
        return DriverManager.getConnection(jdbcUrl, dbUsername, dbPassword);
    }

    public boolean isConnected() {
        return connected;
    }

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public String getDbUsername() {
        return dbUsername;
    }

    public String getLastErrorMessage() {
        return lastErrorMessage;
    }

    /**
     * Records a completed hand outcome (WIN, BLACKJACK, LOSS, PUSH) and net chip win/loss in PostgreSQL.
     */
    public void recordRoundResult(
            String playerHand,
            int playerScore,
            String dealerHand,
            int dealerScore,
            int betAmount,
            String outcome,
            int netChips
    ) {
        if (!connected) {
            return;
        }
        String sql = """
            INSERT INTO round_results (player_hand, player_score, dealer_hand, dealer_score, bet_amount, outcome, net_chips)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, playerHand);
            ps.setInt(2, playerScore);
            ps.setString(3, dealerHand);
            ps.setInt(4, dealerScore);
            ps.setInt(5, betAmount);
            ps.setString(6, outcome);
            ps.setInt(7, netChips);
            ps.executeUpdate();
            System.out.printf("[PostgreSQL] Saved hand result -> Outcome: %s | Net Chips: %+d%n", outcome, netChips);
        } catch (SQLException e) {
            this.connected = false;
            this.lastErrorMessage = e.getMessage();
            log.error("Failed to insert round result into PostgreSQL: {}", e.getMessage());
        }
    }

    /**
     * Records a Basic Strategy error made by the user into PostgreSQL.
     */
    public void recordStrategyError(
            String playerHand,
            int playerScore,
            String dealerUpcard,
            String chosenAction,
            String recommendedAction,
            String explanation
    ) {
        if (!connected) {
            return;
        }
        String sql = """
            INSERT INTO basic_strategy_errors (player_hand, player_score, dealer_upcard, chosen_action, recommended_action, explanation)
            VALUES (?, ?, ?, ?, ?, ?)
        """;
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, playerHand);
            ps.setInt(2, playerScore);
            ps.setString(3, dealerUpcard);
            ps.setString(4, chosenAction);
            ps.setString(5, recommendedAction);
            ps.setString(6, explanation);
            ps.executeUpdate();
            System.out.printf("[PostgreSQL] Logged Basic Strategy Error -> Chose %s instead of %s on %s vs %s%n",
                    chosenAction, recommendedAction, playerHand, dealerUpcard);
        } catch (SQLException e) {
            this.connected = false;
            this.lastErrorMessage = e.getMessage();
            log.error("Failed to insert strategy error into PostgreSQL: {}", e.getMessage());
        }
    }

    public OverallStats getOverallStats() {
        if (!connected) {
            return new OverallStats(0, 0, 0, 0, 0, 0, 0, 0);
        }
        int totalHands = 0, wins = 0, losses = 0, pushes = 0, netProfit = 0, totalWinnings = 0, totalLossesAmt = 0, totalErrors = 0;

        String roundStatsSql = """
            SELECT
                COUNT(*) AS total_hands,
                COALESCE(SUM(CASE WHEN outcome IN ('WIN', 'BLACKJACK') THEN 1 ELSE 0 END), 0) AS wins,
                COALESCE(SUM(CASE WHEN outcome = 'LOSS' THEN 1 ELSE 0 END), 0) AS losses,
                COALESCE(SUM(CASE WHEN outcome = 'PUSH' THEN 1 ELSE 0 END), 0) AS pushes,
                COALESCE(SUM(net_chips), 0) AS net_profit,
                COALESCE(SUM(CASE WHEN net_chips > 0 THEN net_chips ELSE 0 END), 0) AS total_winnings,
                COALESCE(SUM(CASE WHEN net_chips < 0 THEN ABS(net_chips) ELSE 0 END), 0) AS total_losses_amt
            FROM round_results
        """;

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            try (ResultSet rs = stmt.executeQuery(roundStatsSql)) {
                if (rs.next()) {
                    totalHands = rs.getInt("total_hands");
                    wins = rs.getInt("wins");
                    losses = rs.getInt("losses");
                    pushes = rs.getInt("pushes");
                    netProfit = rs.getInt("net_profit");
                    totalWinnings = rs.getInt("total_winnings");
                    totalLossesAmt = rs.getInt("total_losses_amt");
                }
            }
            try (ResultSet rs2 = stmt.executeQuery("SELECT COUNT(*) FROM basic_strategy_errors")) {
                if (rs2.next()) {
                    totalErrors = rs2.getInt(1);
                }
            }
        } catch (SQLException e) {
            log.error("Failed to query overall stats: {}", e.getMessage());
        }
        return new OverallStats(totalHands, wins, losses, pushes, netProfit, totalWinnings, totalLossesAmt, totalErrors);
    }

    public List<RoundRecord> getRecentRounds(int limit) {
        List<RoundRecord> list = new ArrayList<>();
        if (!connected) {
            return list;
        }
        String sql = """
            SELECT id, player_hand, player_score, dealer_hand, dealer_score, bet_amount, outcome, net_chips,
                   TO_CHAR(played_at, 'YYYY-MM-DD HH24:MI:SS') AS played_at_str
            FROM round_results
            ORDER BY id DESC
            LIMIT ?
        """;
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new RoundRecord(
                            rs.getLong("id"),
                            rs.getString("player_hand"),
                            rs.getInt("player_score"),
                            rs.getString("dealer_hand"),
                            rs.getInt("dealer_score"),
                            rs.getInt("bet_amount"),
                            rs.getString("outcome"),
                            rs.getInt("net_chips"),
                            rs.getString("played_at_str")
                    ));
                }
            }
        } catch (SQLException e) {
            log.error("Failed to fetch recent rounds: {}", e.getMessage());
        }
        return list;
    }

    public List<StrategyErrorRecord> getRecentStrategyErrors(int limit) {
        List<StrategyErrorRecord> list = new ArrayList<>();
        if (!connected) {
            return list;
        }
        String sql = """
            SELECT id, player_hand, player_score, dealer_upcard, chosen_action, recommended_action, explanation,
                   TO_CHAR(occurred_at, 'YYYY-MM-DD HH24:MI:SS') AS occurred_at_str
            FROM basic_strategy_errors
            ORDER BY id DESC
            LIMIT ?
        """;
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new StrategyErrorRecord(
                            rs.getLong("id"),
                            rs.getString("player_hand"),
                            rs.getInt("player_score"),
                            rs.getString("dealer_upcard"),
                            rs.getString("chosen_action"),
                            rs.getString("recommended_action"),
                            rs.getString("explanation"),
                            rs.getString("occurred_at_str")
                    ));
                }
            }
        } catch (SQLException e) {
            log.error("Failed to fetch recent strategy errors: {}", e.getMessage());
        }
        return list;
    }

    /**
     * Returns cumulative net profit progression in chronological order (oldest to newest hand).
     */
    public List<Integer> getCumulativeProfitSeries() {
        List<Integer> series = new ArrayList<>();
        series.add(0); // Starting baseline at 0
        if (!connected) {
            return series;
        }
        String sql = "SELECT net_chips FROM round_results ORDER BY id ASC";
        int runningTotal = 0;
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                runningTotal += rs.getInt("net_chips");
                series.add(runningTotal);
            }
        } catch (SQLException e) {
            log.error("Failed to fetch cumulative profit series: {}", e.getMessage());
        }
        return series;
    }

    /**
     * Groups Basic Strategy errors by hand type (Hard Total, Soft Total, Pair Splitting) and by Recommended Action.
     */
    public List<ErrorCategoryCount> getErrorBreakdownByHandType() {
        List<ErrorCategoryCount> list = new ArrayList<>();
        if (!connected) {
            return list;
        }
        String sql = """
            SELECT
                CASE
                    WHEN explanation LIKE 'Pair%' THEN 'Pair Splitting'
                    WHEN explanation LIKE 'Soft%' THEN 'Soft Totals (Ace)'
                    ELSE 'Hard Totals'
                END AS hand_type,
                COUNT(*) AS cnt
            FROM basic_strategy_errors
            GROUP BY hand_type
            ORDER BY cnt DESC
        """;
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new ErrorCategoryCount(rs.getString("hand_type"), rs.getInt("cnt")));
            }
        } catch (SQLException e) {
            log.error("Failed to fetch error breakdown by hand type: {}", e.getMessage());
        }
        return list;
    }

    /**
     * Returns the top most frequent Basic Strategy mistakes (e.g. "Hard 16 vs Dealer 10 -> HIT").
     */
    public List<ErrorCategoryCount> getTopStrategyMistakes(int limit) {
        List<ErrorCategoryCount> list = new ArrayList<>();
        if (!connected) {
            return list;
        }
        String sql = """
            SELECT explanation, COUNT(*) AS cnt
            FROM basic_strategy_errors
            GROUP BY explanation
            ORDER BY cnt DESC
            LIMIT ?
        """;
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new ErrorCategoryCount(rs.getString("explanation"), rs.getInt("cnt")));
                }
            }
        } catch (SQLException e) {
            log.error("Failed to fetch top strategy mistakes: {}", e.getMessage());
        }
        return list;
    }
}
