-- Read model for the nightly reminder job, built from check-in events

CREATE TABLE goal_activity (
    goal_id            varchar(255) NOT NULL,
    user_id            varchar(255) NOT NULL,
    goal_title         varchar(200) NOT NULL,
    frequency          varchar(20)  NOT NULL,
    last_checkin_date  date NOT NULL,
    current_streak     integer NOT NULL,
    last_reminder_date date,
    updated_at         timestamptz(6) NOT NULL,
    PRIMARY KEY (goal_id)
);

CREATE INDEX idx_goal_activity_last_checkin ON goal_activity (last_checkin_date);
