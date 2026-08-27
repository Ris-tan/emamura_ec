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

INSERT INTO delivery_areas (
    prefecture,
    shipping_fee,
    lead_days,
    available
) VALUES

-- 北海道
('北海道', 1000, 4, true),

-- 東北
('青森県', 500, 3, true),
('岩手県', 500, 3, true),
('宮城県', 500, 2, true),
('秋田県', 500, 3, true),
('山形県', 500, 2, true),
('福島県', 500, 2, true),

-- 関東
('茨城県', 500, 2, true),
('栃木県', 500, 2, true),
('群馬県', 500, 2, true),
('埼玉県', 500, 2, true),
('千葉県', 500, 2, true),
('東京都', 500, 2, true),
('神奈川県', 500, 2, true),

-- 甲信越・北陸
('新潟県', 500, 2, true),
('富山県', 500, 2, true),
('石川県', 500, 2, true),
('福井県', 500, 2, true),
('山梨県', 500, 2, true),
('長野県', 500, 2, true),

-- 東海
('岐阜県', 500, 2, true),
('静岡県', 500, 2, true),
('愛知県', 500, 2, true),
('三重県', 500, 2, true),

-- 近畿
('滋賀県', 500, 2, true),
('京都府', 500, 2, true),
('大阪府', 500, 2, true),
('兵庫県', 500, 2, true),
('奈良県', 500, 2, true),
('和歌山県', 500, 2, true),

-- 中国
('鳥取県', 500, 3, true),
('島根県', 500, 3, true),
('岡山県', 500, 3, true),
('広島県', 500, 3, true),
('山口県', 500, 3, true),

-- 四国
('徳島県', 500, 3, true),
('香川県', 500, 3, true),
('愛媛県', 500, 3, true),
('高知県', 500, 3, true),

-- 九州
('福岡県', 500, 3, true),
('佐賀県', 500, 3, true),
('長崎県', 500, 3, true),
('熊本県', 500, 3, true),
('大分県', 500, 3, true),
('宮崎県', 500, 3, true),
('鹿児島県', 500, 3, true),

-- 沖縄
('沖縄県', 1000, 4, true);