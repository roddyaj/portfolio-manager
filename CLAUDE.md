# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project overview

Portfolio Manager is a personal finance tool with two parts:
- `server/` — a Java 25 backend (Jetty + servlets) that parses brokerage CSV exports and computes portfolio allocation/rebalancing/options-selling suggestions.
- `client-react/` — a Create React App frontend that displays the computed data.

The backend never talks to a brokerage API for account data; it reads CSV/export files a user manually downloads from Fidelity or Schwab into `~/Downloads`, and reads user config from `~/.invest/settings.json` (both outside the repo, not present in source control).

## Common commands

Run both server and client together (from repo root):
```
./start.sh   # starts server (server/start.sh) and client (npm start) in background
./stop.sh    # kills node/java processes
```

### Server (`server/`)
```
./build.sh                 # mvn clean, mvn package, copies runtime deps + jar into install/
./start.sh                 # runs the built jar: java -cp install/* com.roddyaj.portfoliomanager.Main
mvn compile                # compile only
mvn test                   # run tests (JUnit 5 via surefire) — no tests currently exist in src/test/java
mvn test -Dtest=ClassName  # run a single test class
```
The server listens on `http://127.0.0.1:8090` (loopback only, hardcoded in `Main.java`).

### Client (`client-react/`)
```
npm start   # dev server on :3000, expects backend on :8090
npm test    # CRA/Jest test runner (watch mode)
npm run build
```

## Architecture

### Request flow
`Main.java` starts a Jetty `Server` with three servlets mapped directly (no framework routing):
- `GET /accounts` (`AccountsServlet`) — lists account names from settings.
- `GET /portfolio?accountName=X` (`PortfolioServlet`) — builds and returns the full `Output` for one account by running `PortfolioManager.process(...)`.
- `GET /stop-poll` (`ApplicationServlet`) — clears `State.lastRefresh`, used to halt the (currently disabled) quote-polling loop.

All servlets extend `EnhancedServlet`, which centralizes Jackson JSON serialization (with `JavaTimeModule`) and CORS (`Access-Control-Allow-Origin: *`) for the local React dev server.

### Global mutable state
`State` (`model/State.java`) is a process-wide singleton (`State.getInstance()`) holding: parsed `Settings` (via `SettingsReader`, which hot-reloads `~/.invest/settings.json` when its mtime changes), a live quote cache (`Map<String, Quote>`), and bookkeeping for the quote-polling loop (`lastRefresh`, `symbolsToLookup`). `Main.startQuotePolling()` is currently disabled (commented out) — quotes are not live-refreshed in the running app.

### Reading portfolio data
`PortfolioReader` is the interface for turning brokerage exports into a `Portfolio` record (positions, open orders, transactions, cash, balance, time). `PortfolioManager.process()` picks the implementation by account number format: a 9-character account number routes to `FidelityPortfolioReader`, anything else to `SchwabPortfolioReader` (`api/fidelity/` and `api/schwab/` packages). Schwab has separate readers for positions, orders, and transactions (`SchwabPositionsReader`, `SchwabOrdersReader`, `SchwabTransactionsReader`) composed by `SchwabPortfolioReader`. Readers look for files in `State.getInputDir()` (`~/Downloads`) — there is no upload endpoint; the user must export CSVs from their brokerage first.

### Core business logic — `PortfolioManager`
This is the central orchestrator (`logic/PortfolioManager.java`), called fresh on every `/portfolio` request:
1. Reads the `Portfolio` via the appropriate `PortfolioReader`.
2. Merges in "phantom" positions for symbols with a target allocation or open buy order but no current holding (`getNewPositions`).
3. Applies live quotes from `State` cache (only if newer than the portfolio snapshot, or the position is empty) to compute market value / gain-loss.
4. Uses `AllocationMap` (built from `AccountSettings.allocations`, category strings resolved against the actual portfolio via `getCatLastToken()`) to compute each position's target percentage and `sharesToBuy` (respects `minOrder`, per-allocation `minOrder`/`sellLimit`/`sell` flag, and a rounding "cutoff" fudge factor).
5. Computes `callsToSell` for covered-call candidates (>=100 shares, options enabled, not excluded via `settings.optionsExclude`), accounting for calls already sold.
6. Computes cash-on-hold (strike × contracts for short puts), open buy-order amount, and derived `cashAvailable`.
7. Builds `putsToSell` suggestions from `settings.optionsInclude`, pulling price either from an existing position or a live Finnhub quote.
8. Aggregates monthly income (options premium, dividends, contributions) from transaction history into `MonthlyIncome` rows.

`Settings`/`AccountSettings`/`Allocation`/`Api` (in `settings/`) are the Jackson-deserialized shape of `~/.invest/settings.json`: per-account allocation targets/limits, options include/exclude lists, and third-party API keys (e.g. Finnhub).

### External API
`api/FinnhubAPI.java` fetches live quotes/prices, rate-limited per `Api` settings (`requestsPerMinute`). `SP500ReturnAPI` exists but is currently unused (caller commented out in `PortfolioManager`).

### Frontend
Plain CRA app, no router, no state library — `App.js` fetches `/accounts` then `/portfolio?accountName=...` from `http://localhost:8090` directly (hardcoded host/port) and passes the resulting JSON down through props to display components (`Positions`, `Options`, `Cash`, `Income`, `PutsToSell`, `TitleBar`).

## Code style notes (server)
Existing Java code uses tabs for indentation and Allman-style braces (opening brace on its own line) — match this when editing existing files.
