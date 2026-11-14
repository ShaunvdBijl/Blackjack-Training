-- Initial schema for Blackjack Trainer PostgreSQL container
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
);

CREATE TABLE IF NOT EXISTS basic_strategy_errors (
    id SERIAL PRIMARY KEY,
    player_hand VARCHAR(120) NOT NULL,
    player_score INT NOT NULL,
    dealer_upcard VARCHAR(20) NOT NULL,
    chosen_action VARCHAR(30) NOT NULL,
    recommended_action VARCHAR(30) NOT NULL,
    explanation VARCHAR(255) NOT NULL,
    occurred_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
