-- Baseline schema (matches the entities Hibernate previously created with ddl-auto)

CREATE TABLE notifications (
    id         varchar(255) NOT NULL,
    user_id    varchar(255) NOT NULL,
    type       varchar(255) NOT NULL,
    message    varchar(500) NOT NULL,
    read       boolean NOT NULL,
    created_at timestamptz(6) NOT NULL,
    PRIMARY KEY (id)
);

CREATE INDEX idx_notifications_user ON notifications (user_id);
