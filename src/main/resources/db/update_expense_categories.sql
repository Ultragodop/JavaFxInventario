-- Verificar si la tabla ya existe
CREATE TABLE IF NOT EXISTS expense_categories (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL UNIQUE,
    description TEXT,
    account_code TEXT
);

-- Verificar si la columna active existe y agregarla si no
PRAGMA table_info(expense_categories);

-- Si la columna active no existe, agregarla
ALTER TABLE expense_categories ADD COLUMN active BOOLEAN NOT NULL DEFAULT 1;
