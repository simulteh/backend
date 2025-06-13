CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS laboratory_works (
    id_work SERIAL PRIMARY KEY,
    id_user BIGINT NOT NULL,
    id_recipient BIGINT NOT NULL,
    id_departure VARCHAR(36) NOT NULL,
    id_time TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(255) NOT NULL,
    file_size DOUBLE PRECISION NOT NULL,
    file_type VARCHAR(10) NOT NULL,
    comment VARCHAR(1000),
    status VARCHAR(20) NOT NULL,
    grade INTEGER,
    is_closed BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (id_user) REFERENCES users(id),
    FOREIGN KEY (id_recipient) REFERENCES users(id)
);

-- Другие таблицы...