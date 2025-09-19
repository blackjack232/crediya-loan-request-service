-- Nos aseguramos de que usamos la base creada por POSTGRES_DB
-- (ya definida en docker-compose como authentication_db o la que uses para loans)
-- Si prefieres otra base para loans, usa POSTGRES_DB=loan_system en docker-compose

-- Crear esquema para préstamos (propiedad de auth_user)
CREATE SCHEMA IF NOT EXISTS loan_schema AUTHORIZATION auth_user;

-- Permisos sobre el esquema
GRANT ALL PRIVILEGES ON SCHEMA loan_schema TO auth_user;

-- Crear tablas
CREATE TABLE loan_schema.states (
    id_state SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT
);

CREATE TABLE loan_schema.loan_type (
    id_loan_type SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    minimum_amount DECIMAL(12,2) NOT NULL,
    maximum_amount DECIMAL(12,2) NOT NULL,
    interest_rate DECIMAL(5,2) NOT NULL,
    automatic_validation BOOLEAN DEFAULT FALSE
);

CREATE TABLE loan_schema.request (
    id_request SERIAL PRIMARY KEY,
    amount DECIMAL(12,2) NOT NULL,
    term INT NOT NULL,
    email VARCHAR(150) NOT NULL,
    id_state INT NOT NULL,
    id_loan_type INT NOT NULL,
    CONSTRAINT fk_request_state FOREIGN KEY (id_state) REFERENCES loan_schema.states (id_state),
    CONSTRAINT fk_request_loan_type FOREIGN KEY (id_loan_type) REFERENCES loan_schema.loan_type (id_loan_type)
);

-- Permisos sobre tablas y secuencias
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA loan_schema TO auth_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA loan_schema TO auth_user;

-- Datos iniciales
INSERT INTO loan_schema.states (name, description) VALUES
('En espera', 'Solicitud creada en espera de validación'),
('Aprobada', 'Solicitud aprobada'),
('Rechazada', 'Solicitud rechazada');

INSERT INTO loan_schema.loan_type (name, minimum_amount, maximum_amount, interest_rate, automatic_validation)
VALUES ('Crédito Personal', 1000.00, 10000.00, 12.50, TRUE);
