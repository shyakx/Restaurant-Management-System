-- Create menu_items table
CREATE TABLE IF NOT EXISTS menu_items (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    category VARCHAR(100) NOT NULL,
    available BOOLEAN NOT NULL DEFAULT true,
    image_url VARCHAR(500),
    preparation_time INTEGER NOT NULL DEFAULT 15,
    ingredients TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_menu_items_category ON menu_items(category);
CREATE INDEX IF NOT EXISTS idx_menu_items_available ON menu_items(available);
CREATE INDEX IF NOT EXISTS idx_menu_items_price ON menu_items(price);
CREATE INDEX IF NOT EXISTS idx_menu_items_name ON menu_items(name);

-- Insert sample menu items
INSERT INTO menu_items (name, description, price, category, available, preparation_time, ingredients) VALUES
('Classic Burger', 'Juicy beef patty with lettuce, tomato, onion, and our special sauce', 12.99, 'Burgers', true, 15, 'beef patty, lettuce, tomato, onion, special sauce, brioche bun'),
('Cheese Pizza', 'Traditional pizza with tomato sauce and mozzarella cheese', 10.99, 'Pizza', true, 20, 'tomato sauce, mozzarella cheese, pizza dough'),
('Caesar Salad', 'Fresh romaine lettuce with parmesan cheese and croutons', 8.99, 'Salads', true, 10, 'romaine lettuce, parmesan cheese, croutons, caesar dressing'),
('Grilled Chicken Sandwich', 'Grilled chicken breast with avocado and bacon', 14.99, 'Sandwiches', true, 18, 'chicken breast, avocado, bacon, whole wheat bun'),
('French Fries', 'Crispy golden fries with sea salt', 4.99, 'Sides', true, 8, 'potatoes, sea salt, vegetable oil'),
('Coca Cola', 'Refreshing cola drink', 2.99, 'Beverages', true, 2, 'carbonated water, sugar, natural flavors'),
('Chocolate Cake', 'Rich chocolate cake with chocolate frosting', 6.99, 'Desserts', true, 5, 'chocolate, flour, sugar, eggs, butter'),
('Fish and Chips', 'Beer-battered cod with crispy fries', 16.99, 'Seafood', true, 25, 'cod fillet, beer batter, potatoes, tartar sauce'),
('Vegetarian Wrap', 'Fresh vegetables with hummus in a whole wheat wrap', 11.99, 'Vegetarian', true, 12, 'vegetables, hummus, whole wheat wrap'),
('Iced Coffee', 'Cold brewed coffee with ice', 3.99, 'Beverages', true, 3, 'coffee, ice, water');
