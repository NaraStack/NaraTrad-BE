INSERT INTO portfolio (id, name) VALUES (1, 'My Portfolio');

INSERT INTO holding (id, portfolio_id, symbol, quantity, avg_price)
VALUES
 (1, 1, 'AAPL', 10, 150.00),
 (2, 1, 'TSLA', 5, 200.00);

INSERT INTO latest_price (id, symbol, price)
VALUES
 (1, 'AAPL', 178.55),
 (2, 'TSLA', 410.20);
