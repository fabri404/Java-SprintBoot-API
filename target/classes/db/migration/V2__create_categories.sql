-- V2__create_categories.sql
CREATE TABLE IF NOT EXISTS categories (
  id          BIGSERIAL PRIMARY KEY,
  name        TEXT NOT NULL UNIQUE,
  created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- (Opcional) datos de prueba
INSERT INTO categories (name) VALUES
  ('Periféricos'),
  ('Notebooks')
ON CONFLICT (name) DO NOTHING;

