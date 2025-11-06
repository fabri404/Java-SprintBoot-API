CREATE TABLE IF NOT EXISTS products (
  id BIGSERIAL PRIMARY KEY,
  name TEXT NOT NULL,
  description TEXT NOT NULL,
  price NUMERIC(15,2) NOT NULL CHECK (price >= 0),
  sku VARCHAR(64),
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Único por 'sku'
CREATE UNIQUE INDEX IF NOT EXISTS ux_products_sku
  ON products (sku);
