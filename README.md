# Blackjack & Card Counting Trainer (Spring Boot + Swing + PostgreSQL)

An interactive desktop **Blackjack & Hi-Lo Card Counting Trainer** built with **Java 17**, **Spring Boot 3**, **Java Swing / Java2D**, and **PostgreSQL 16**, fully containerized with **Docker & Docker Compose**.

Designed to help players master **Basic Strategy**, **Running & True Count conversion**, and **optimal bet sizing** while tracking every hand and mistake in a PostgreSQL analytics database.

---

## Key Features

### 1. Full Casino Blackjack & Multi-Hand Play
- **1 to 5 Simultaneous Starting Hands**: Choose how many hands to play per round (`Hands: 1–5`) before dealing, and play each hand in left-to-right sequence.
- **Complete Player Actions**:
  - **Hit (`H`)**: Draw additional cards with automatic soft/hard Ace adjustment (`11` or `1`).
  - **Stand (`S`)**: Lock in your current hand and advance to the next hand or Dealer's turn.
  - **Double Down (`D`)**: Double your bet on any initial 2-card hand and receive one final card.
  - **Split Pair (`SP`)**: Split matching-rank pairs into separate visual hands with independent bets.
- **Configurable Shoe**: Select `1` to `8` decks per shoe, manually shuffle between rounds, or let the shoe auto-shuffle when cards run low.

### 2. Basic Strategy Assistor & Always-On Console Evaluator
- **Live Strategy Assistor Toggle**: Toggle **`Strategy Assistor: ON`** to see real-time optimal play advice on the table banner based on your hand (**Hard Totals**, **Soft Totals**, or **Pair Splitting**) vs. the Dealer's upcard.
- **Built-in Reference Chart**: Click **`View Chart`** at any time to view the full Basic Strategy reference chart (`basicStrategy.png`).
- **Always-On Console & Database Auditing**: Even when the visual assistor toggle is **OFF**, every decision you make is audited against Basic Strategy, logged to the terminal console (`CORRECT PLAY` / `INCORRECT PLAY`), and any mistake is persisted to PostgreSQL.

### 3. Hi-Lo Card Counting & True Count Bet Spread
- **Running Count**: Tracks `+1` for cards `2–6`, `0` for `7–9`, and `-1` for `10–A` (counting the Dealer's hole card only when revealed).
- **True Count & Decks Remaining**: Dynamically computes $\text{True Count} = \text{Running Count} \div \text{Decks Remaining}$.
- **Optimal Bet Spread Recommender**: Recommends bet multipliers (`1x` to `8x` base unit) based on the pre-deal True Count.
- **Independent Visibility Toggles**: Hide or reveal the **Running Count**, **True Count**, and **Cards Left** independently to test your mental counting accuracy.

### 4. PostgreSQL Persistence & Java2D Analytics Dashboard
- **`round_results` Table**: Stores every hand played, player/dealer cards and scores, bet amount, outcome (`WIN`, `BLACKJACK`, `LOSS`, `PUSH`), net chips won/lost, and timestamp.
- **`basic_strategy_errors` Table**: Records every Basic Strategy mistake, including your hand, the dealer's upcard, the action you chose, the optimal action, and the strategy rule explanation.
- **Visual Analytics Dashboard (`DB Dashboard`)**:
  - **KPI Summary Cards**: Total Hands Played, Win Rate % (excluding pushes), Lifetime Net Profit, and Total Strategy Mistakes.
  - **Cumulative Bankroll Progression Line Chart**: Plots your net profit/loss curve across all hands played.
  - **Hand Outcomes Distribution Bar**: Visual breakdown of Wins, Pushes, and Losses.
  - **Weak Spots & Error Breakdown**: Groups mistakes by hand category (*Hard Totals*, *Soft Totals*, *Pair Splitting*) and ranks your top 5 most frequent mistakes.

---

## Tech Stack

| Component | Technology |
| :--- | :--- |
| **Language** | Java 17 |
| **Framework** | Spring Boot 3.3.4 (`spring-boot-starter-jdbc`, non-headless mode) |
| **Desktop UI & Charts** | Java Swing & Custom Java2D Graphics |
| **Database** | PostgreSQL 16 |
| **Build Tool** | Apache Maven |
| **Containerization** | Docker & Docker Compose |
| **Testing** | JUnit 5 |

---

## Getting Started

### Prerequisites
- **Java JDK 17+**
- **Apache Maven 3.8+**
- **PostgreSQL 16** (local installation or via Docker)

### 1. Configure Environment Variables (`.env`)
Copy `.env.example` to `.env` in the project root (`Blackjack-Practise/.env`) and fill in your PostgreSQL credentials:

```bash
cp .env.example .env
```

Example `.env`:
```properties
DB_URL=jdbc:postgresql://localhost:5433/blackjack
DB_USER=postgres
DB_PASSWORD=your_secure_password

POSTGRES_DB=blackjack
POSTGRES_PORT=5434
```
> **Note:** `.env` is ignored by Git (`.gitignore`) and Docker builds (`.dockerignore`) so your credentials are never exposed.

### 2. Run Locally with Maven
```bash
# Run unit tests
mvn clean test

# Start the Spring Boot + Swing application
mvn spring-boot:run
```
The database tables (`round_results` and `basic_strategy_errors`) are created automatically on startup. You can also configure or reconnect to PostgreSQL live inside the app via the **`DB Dashboard`** button.

---

## Running with Docker

### Option A: Run PostgreSQL in Docker + Swing UI Natively
```bash
docker compose up -d postgres
```
Update `DB_URL` in your `.env` to `jdbc:postgresql://localhost:5434/blackjack`, then run:
```bash
mvn spring-boot:run
```

### Option B: Full Stack in Docker (PostgreSQL + Blackjack App)
> Requires an X11 server on your host machine (such as **WSLg**, **VcXsrv / XLaunch** on Windows, or **XQuartz** on macOS) to render the Swing desktop window.

```bash
docker compose up --build
```

---

## Project Structure

```text
Blackjack-Practise/
├── docker/
│   └── init-db.sql                 # PostgreSQL schema initialization script
├── src/
│   ├── main/
│   │   ├── java/com/blackjack/trainer/
│   │   │   ├── BlackjackTrainerApplication.java  # Spring Boot + Swing EDT entry point
│   │   │   ├── model/
│   │   │   │   ├── BasicStrategyAssistor.java    # Basic Strategy engine (Hard, Soft, Pairs)
│   │   │   │   ├── Deck.java                     # Shoe, shuffling, Running & True Count
│   │   │   │   ├── Hand.java                     # Hand evaluation, soft/hard Ace handling
│   │   │   │   └── Player.java                   # Multi-hand state, chips, bets, actions
│   │   │   ├── service/
│   │   │   │   └── BlackjackDatabaseService.java # PostgreSQL JDBC persistence & analytics queries
│   │   │   └── ui/
│   │   │       ├── MainFrame.java                # Main casino table Swing GUI
│   │   │       └── StatsDashboardPanel.java      # Java2D visual analytics dashboard
│   │   └── resources/
│   │       └── application.properties            # Spring Boot config (imports .env)
│   └── test/
│       └── java/com/blackjack/trainer/
│           └── BlackjackModelTest.java           # JUnit 5 unit tests
├── .dockerignore
├── .env.example
├── .gitignore
├── basicStrategy.png               # Basic Strategy reference chart image
├── Dockerfile                      # Multi-stage Maven build + JRE 17 X11 runtime
├── docker-compose.yml              # Orchestrates PostgreSQL 16 + Blackjack Trainer app
└── pom.xml
```
