-- Create single database for the restaurant system
CREATE DATABASE restaurant_db;

-- Create user for the application
CREATE USER restaurant_user WITH PASSWORD 'password';

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE restaurant_db TO restaurant_user;

-- Connect to restaurant_db and create tables
\c restaurant_db;

CREATE TABLE menu_items (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL,
    category VARCHAR(100),
    available BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insert sample menu items
INSERT INTO menu_items (name, description, price, category) VALUES
('Isombe', 'Cassava leaves with palm oil and groundnuts', 3500.00, 'Main Dish'),
('Ibihaza', 'Pumpkin and beans cooked together', 2800.00, 'Main Dish'),
('Umutsima', 'Traditional maize and beans porridge', 2500.00, 'Main Dish'),
('Brochettes', 'Grilled meat skewers with spices', 4000.00, 'Street Food'),
('Mizuzu', 'Fried plantains served with sauce', 2000.00, 'Side Dish'),
('Ugali', 'Stiff porridge made from maize flour', 1500.00, 'Staple Food'),
('Sambaza', 'Small fried fish served with vegetables', 3200.00, 'Main Dish'),
('Ikivuguto', 'Traditional fermented milk drink', 1800.00, 'Beverage');

CREATE TABLE orders (
    id SERIAL PRIMARY KEY,
    customer_name VARCHAR(255) NOT NULL,
    customer_email VARCHAR(255),
    total_amount DECIMAL(10,2) NOT NULL,
    status VARCHAR(50) DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE order_items (
    id SERIAL PRIMARY KEY,
    order_id INTEGER REFERENCES orders(id),
    menu_item_id INTEGER,
    item_name VARCHAR(255),
    quantity INTEGER NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    subtotal DECIMAL(10,2) NOT NULL
);

-- Insert sample orders
INSERT INTO orders (customer_name, customer_email, total_amount, status) VALUES
('Jean Mugabo', 'jean.mugabo@email.rw', 6300.00, 'COMPLETED'),
('Grace Uwimana', 'grace.uwimana@email.rw', 5500.00, 'PENDING'),
('Eric Niyonzima', 'eric.niyonzima@email.rw', 7500.00, 'PREPARING');

INSERT INTO order_items (order_id, menu_item_id, item_name, quantity, price, subtotal) VALUES
(1, 1, 'Isombe', 1, 3500.00, 3500.00),
(1, 5, 'Mizuzu', 1, 2000.00, 2000.00),
(1, 8, 'Ikivuguto', 1, 1800.00, 1800.00),
(2, 2, 'Ibihaza', 1, 2800.00, 2800.00),
(2, 6, 'Ugali', 1, 1500.00, 1500.00),
(2, 8, 'Ikivuguto', 1, 1800.00, 1800.00),
(3, 4, 'Brochettes', 1, 4000.00, 4000.00),
(3, 7, 'Sambaza', 1, 3200.00, 3200.00),
(3, 3, 'Umutsima', 1, 2500.00, 2500.00);
