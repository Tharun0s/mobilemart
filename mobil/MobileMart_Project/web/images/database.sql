
CREATE DATABASE IF NOT EXISTS mobilemart;
USE mobilemart;

CREATE TABLE admins (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL
);

CREATE TABLE mobiles (
    id INT AUTO_INCREMENT PRIMARY KEY,
    brand VARCHAR(50) NOT NULL,
    model VARCHAR(100) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    image VARCHAR(255)
);

CREATE TABLE orders (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    total DECIMAL(10,2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE order_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    mobile_id INT NOT NULL,
    model VARCHAR(100) NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    line_total DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (order_id)
        REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (mobile_id)
        REFERENCES mobiles(id)
);

CREATE TABLE feedback (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    rating INT NOT NULL,
    comments VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO admins (username, password)
VALUES ('admin', 'admin123');

INSERT INTO mobiles (brand, model, price, image)
VALUES
('Apple', 'iPhone 16', 69999, 'phone1.svg'),
('Apple', 'iPhone 16 Pro', 119999, 'phone2.svg'),
('Apple', 'iPhone 15', 59999, 'phone3.svg'),
('Samsung', 'Galaxy S25', 79999, 'phone4.svg'),
('Samsung', 'Galaxy A56', 39999, 'phone5.svg'),
('Samsung', 'Galaxy M56', 27999, 'phone6.svg'),
('OnePlus', 'OnePlus 13', 69999, 'phone7.svg'),
('OnePlus', 'OnePlus 13R', 42999, 'phone8.svg'),
('OnePlus', 'Nord CE4', 24999, 'phone9.svg'),
('Redmi', 'Redmi Note 14', 17999, 'phone10.svg'),
('Redmi', 'Redmi Note 14 Pro', 24999, 'phone11.svg'),
('Redmi', 'Redmi 14C', 9999, 'phone12.svg'),
('POCO', 'POCO X7', 21999, 'phone13.svg'),
('POCO', 'POCO F7', 32999, 'phone14.svg'),
('POCO', 'POCO M7', 12999, 'phone15.svg'),
('Realme', 'Realme 14 Pro', 24999, 'phone16.svg'),
('Realme', 'Realme GT 7', 39999, 'phone17.svg'),
('Realme', 'Realme C75', 11999, 'phone18.svg'),
('Vivo', 'Vivo V50', 34999, 'phone19.svg'),
('Vivo', 'Vivo T4', 21999, 'phone20.svg'),
('Vivo', 'Vivo Y29', 13999, 'phone21.svg'),
('OPPO', 'OPPO Reno 13', 37999, 'phone22.svg'),
('OPPO', 'OPPO A5', 15999, 'phone23.svg'),
('Google Pixel', 'Pixel 9', 69999, 'phone24.svg'),
('Google Pixel', 'Pixel 9a', 49999, 'phone25.svg'),
('Motorola', 'Edge 60', 29999, 'phone26.svg'),
('Motorola', 'Moto G85', 17999, 'phone27.svg'),
('Nothing', 'Phone (3a)', 24999, 'phone28.svg'),
('iQOO', 'iQOO Neo 10', 31999, 'phone29.svg'),
('iQOO', 'iQOO Z10', 21999, 'phone30.svg');
