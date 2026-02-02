-- Create separate databases for each service
CREATE DATABASE menu_db;
CREATE DATABASE order_db;

-- Create users for each service
CREATE USER menu_user WITH PASSWORD 'password';
CREATE USER order_user WITH PASSWORD 'password';

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE menu_db TO menu_user;
GRANT ALL PRIVILEGES ON DATABASE order_db TO order_user;

-- Connect to menu_db and create tables
\c menu_db;

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
('Margherita Pizza', 'Classic pizza with tomato sauce, mozzarella, and basil', 12.99, 'Pizza'),
('Caesar Salad', 'Fresh romaine lettuce with caesar dressing and croutons', 8.99, 'Salad'),
('Grilled Chicken Sandwich', 'Grilled chicken breast with lettuce and tomato', 10.99, 'Sandwich'),
('Spaghetti Carbonara', 'Pasta with bacon, eggs, and parmesan cheese', 11.99, 'Pasta'),
('Chocolate Cake', 'Rich chocolate cake with chocolate frosting', 6.99, 'Dessert');

-- Connect to order_db and create tables
\c order_db;

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
('John Doe', 'john@email.com', 23.98, 'COMPLETED'),
('Jane Smith', 'jane@email.com', 19.98, 'PENDING');

INSERT INTO order_items (order_id, menu_item_id, item_name, quantity, price, subtotal) VALUES
(1, 1, 'Margherita Pizza', 1, 12.99, 12.99),
(1, 2, 'Caesar Salad', 1, 8.99, 8.99),
(2, 3, 'Grilled Chicken Sandwich', 1, 10.99, 10.99),
(2, 5, 'Chocolate Cake', 1, 6.99, 6.99);
