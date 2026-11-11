-- Initial schema for the development database.
-- Table and column names follow Hibernate's Spring physical naming convention.

CREATE TABLE users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    username VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uk_users_email UNIQUE (email)
);

CREATE INDEX idx_email ON users (email);

CREATE TABLE farms (
    id BIGINT NOT NULL AUTO_INCREMENT,
    farm_name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    owner_id BIGINT NOT NULL,
    CONSTRAINT pk_farms PRIMARY KEY (id),
    CONSTRAINT uq_owner_farm_name UNIQUE (owner_id, farm_name),
    CONSTRAINT fk_farms_owner FOREIGN KEY (owner_id) REFERENCES users (id)
);

CREATE INDEX idx_farm_name ON farms (farm_name);
CREATE INDEX idx_farm_owner_id ON farms (owner_id);

CREATE TABLE fields (
    id BIGINT NOT NULL AUTO_INCREMENT,
    field_name VARCHAR(255) NOT NULL,
    total_area DECIMAL(10,2) NOT NULL,
    available_area DECIMAL(10,2) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    version BIGINT,
    farm_id BIGINT NOT NULL,
    CONSTRAINT pk_fields PRIMARY KEY (id),
    CONSTRAINT fk_fields_farm FOREIGN KEY (farm_id) REFERENCES farms (id)
);

CREATE INDEX idx_field_name ON fields (field_name);
CREATE INDEX idx_field_farm_id ON fields (farm_id);

CREATE TABLE crop_types (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    default_maturity_time_in_days INT,
    farm_id BIGINT NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT pk_crop_types PRIMARY KEY (id),
    CONSTRAINT uk_crop_type_farm_name UNIQUE (farm_id, name),
    CONSTRAINT fk_crop_types_farm FOREIGN KEY (farm_id) REFERENCES farms (id)
);

CREATE INDEX idx_crop_type_farm_id ON crop_types (farm_id);

CREATE TABLE crop_plantings (
    id BIGINT NOT NULL AUTO_INCREMENT,
    field_id BIGINT NOT NULL,
    crop_type_id BIGINT NOT NULL,
    crop_area DECIMAL(10,2) NOT NULL,
    planted_on DATE NOT NULL,
    expected_harvest_on DATE,
    completed_on DATE,
    maturity_time_in_days INT NOT NULL,
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT pk_crop_plantings PRIMARY KEY (id),
    CONSTRAINT fk_crop_plantings_field FOREIGN KEY (field_id) REFERENCES fields (id),
    CONSTRAINT fk_crop_plantings_crop_type FOREIGN KEY (crop_type_id) REFERENCES crop_types (id)
);

CREATE INDEX idx_crop_planting_field_id ON crop_plantings (field_id);
CREATE INDEX idx_crop_planting_crop_type_id ON crop_plantings (crop_type_id);

CREATE TABLE crop_activity_logs (
    id BIGINT NOT NULL AUTO_INCREMENT,
    crop_planting_id BIGINT NOT NULL,
    day_number INT NOT NULL,
    crop_stage VARCHAR(40),
    daily_activity VARCHAR(2000) NOT NULL,
    nutrition_and_water_management VARCHAR(2000),
    pest_and_disease_control VARCHAR(2000),
    notes VARCHAR(2000),
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT pk_crop_activity_logs PRIMARY KEY (id),
    CONSTRAINT fk_activity_logs_planting FOREIGN KEY (crop_planting_id) REFERENCES crop_plantings (id)
);

CREATE INDEX idx_activity_planting_id ON crop_activity_logs (crop_planting_id);
CREATE INDEX idx_activity_planting_day ON crop_activity_logs (crop_planting_id, day_number);

CREATE TABLE expenses (
    id BIGINT NOT NULL AUTO_INCREMENT,
    farm_id BIGINT NOT NULL,
    crop_planting_id BIGINT,
    amount DECIMAL(14,2) NOT NULL,
    expense_date DATE NOT NULL,
    category VARCHAR(40) NOT NULL,
    description VARCHAR(1000) NOT NULL,
    voided BIT NOT NULL DEFAULT b'0',
    voided_at TIMESTAMP(6),
    void_reason VARCHAR(500),
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT pk_expenses PRIMARY KEY (id),
    CONSTRAINT fk_expenses_farm FOREIGN KEY (farm_id) REFERENCES farms (id),
    CONSTRAINT fk_expenses_planting FOREIGN KEY (crop_planting_id) REFERENCES crop_plantings (id)
);

CREATE INDEX idx_expense_farm_date ON expenses (farm_id, expense_date);
CREATE INDEX idx_expense_planting_id ON expenses (crop_planting_id);

CREATE TABLE harvest_records (
    id BIGINT NOT NULL AUTO_INCREMENT,
    crop_planting_id BIGINT NOT NULL,
    harvest_date DATE NOT NULL,
    quantity DECIMAL(14,3) NOT NULL,
    unit VARCHAR(30) NOT NULL,
    notes VARCHAR(2000),
    voided BIT NOT NULL DEFAULT b'0',
    voided_at TIMESTAMP(6),
    void_reason VARCHAR(500),
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT pk_harvest_records PRIMARY KEY (id),
    CONSTRAINT fk_harvest_records_planting FOREIGN KEY (crop_planting_id) REFERENCES crop_plantings (id)
);

CREATE INDEX idx_harvest_planting_date ON harvest_records (crop_planting_id, harvest_date);
