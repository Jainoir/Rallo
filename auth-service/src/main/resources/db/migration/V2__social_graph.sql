-- Friendships and accountability groups

CREATE TABLE friendships (
    id           varchar(255) NOT NULL,
    requester_id varchar(255) NOT NULL,
    addressee_id varchar(255) NOT NULL,
    status       varchar(255) NOT NULL,
    created_at   timestamptz(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_friendships_pair UNIQUE (requester_id, addressee_id)
);

CREATE TABLE friend_groups (
    id         varchar(255) NOT NULL,
    name       varchar(100) NOT NULL,
    owner_id   varchar(255) NOT NULL,
    created_at timestamptz(6) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE group_members (
    id        varchar(255) NOT NULL,
    group_id  varchar(255) NOT NULL,
    user_id   varchar(255) NOT NULL,
    joined_at timestamptz(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_group_members UNIQUE (group_id, user_id)
);

CREATE INDEX idx_friendships_addressee ON friendships (addressee_id);
CREATE INDEX idx_group_members_user ON group_members (user_id);
