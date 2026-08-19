-- 開発用初期データ

INSERT INTO categories (category_name)
VALUES ('花束')
ON CONFLICT (category_name) DO NOTHING;


INSERT INTO products (
    product_name,
    price,
    stock,
    category_id,
    description,
    image_url,
    is_active
)
SELECT
    '季節のおまかせ花束',
    5500,
    10,
    category_id,
    '季節の花を使用したおまかせ花束です。',
    '/images/products/seasonal_bouquet.jpg',
    TRUE
FROM categories
WHERE category_name = '花束';


INSERT INTO products (
    product_name,
    price,
    stock,
    category_id,
    description,
    image_url,
    is_active
)
SELECT
    'ひまわりブーケ',
    4000,
    8,
    category_id,
    '明るいひまわりを中心にしたブーケです。',
    '/images/products/sunflower_bouquet.jpg',
    TRUE
FROM categories
WHERE category_name = '花束';


INSERT INTO products (
    product_name,
    price,
    stock,
    category_id,
    description,
    image_url,
    is_active
)
SELECT
    'ピンクローズブーケ',
    6500,
    5,
    category_id,
    'ピンク系のバラを中心にした華やかな花束です。',
    '/images/products/pink_rose_bouquet.jpg',
    TRUE
FROM categories
WHERE category_name = '花束';