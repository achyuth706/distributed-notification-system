-- Distributed Notification System - Database Initialization

CREATE TABLE IF NOT EXISTS notifications (
    id              VARCHAR(36) PRIMARY KEY,
    user_id         VARCHAR(255) NOT NULL,
    channel         VARCHAR(20)  NOT NULL,
    template_id     VARCHAR(100),
    subject         TEXT,
    body            TEXT,
    priority        VARCHAR(20)  NOT NULL DEFAULT 'NORMAL',
    status          VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    retry_count     INTEGER      NOT NULL DEFAULT 0,
    correlation_id  VARCHAR(36),
    error_message   TEXT,
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    sent_at         TIMESTAMP
);

CREATE TABLE IF NOT EXISTS dead_letter_events (
    id              VARCHAR(36) PRIMARY KEY,
    notification_id VARCHAR(36),
    topic           VARCHAR(100),
    partition_id    INTEGER,
    offset_value    BIGINT,
    payload         TEXT,
    error_message   TEXT,
    retry_count     INTEGER NOT NULL DEFAULT 0,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_notifications_user_id    ON notifications(user_id);
CREATE INDEX IF NOT EXISTS idx_notifications_status     ON notifications(status);
CREATE INDEX IF NOT EXISTS idx_notifications_channel    ON notifications(channel);
CREATE INDEX IF NOT EXISTS idx_notifications_created_at ON notifications(created_at);
