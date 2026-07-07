-- Baseline schema (matches the entities Hibernate previously created with ddl-auto)

CREATE TABLE goals (
    id                   varchar(255) NOT NULL,
    user_id              varchar(255) NOT NULL,
    title                varchar(200) NOT NULL,
    description          varchar(500),
    frequency            varchar(255) NOT NULL,
    target_days_per_week integer,
    created_at           timestamptz(6) NOT NULL,
    active               boolean NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE checkins (
    id            varchar(255) NOT NULL,
    goal_id       varchar(255) NOT NULL,
    user_id       varchar(255) NOT NULL,
    checkin_date  date NOT NULL,
    checked_in_at timestamptz(6) NOT NULL,
    notes         varchar(500),
    PRIMARY KEY (id),
    CONSTRAINT uk_checkins_goal_date UNIQUE (goal_id, checkin_date),
    CONSTRAINT fk_checkins_goal FOREIGN KEY (goal_id) REFERENCES goals (id)
);

CREATE INDEX idx_goals_user ON goals (user_id);
