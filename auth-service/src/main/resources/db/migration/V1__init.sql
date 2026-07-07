-- Baseline schema (matches the entities Hibernate previously created with ddl-auto)

CREATE TABLE users (
    id            varchar(255) NOT NULL,
    username      varchar(50)  NOT NULL,
    email         varchar(255) NOT NULL,
    password_hash varchar(255) NOT NULL,
    created_at    timestamptz(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_users_username UNIQUE (username),
    CONSTRAINT uk_users_email UNIQUE (email)
);

CREATE TABLE user_roles (
    user_id varchar(255) NOT NULL,
    roles   varchar(255),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users (id)
);
