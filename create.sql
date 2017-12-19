CREATE TABLE "task_queue" (
  id     BIGSERIAL NOT NULL,
  queued TIMESTAMP NOT NULL DEFAULT (STATEMENT_TIMESTAMP()),
  PRIMARY KEY (id)
);

CREATE TABLE "task" (
  id        BIGSERIAL    NOT NULL,
  task_type VARCHAR(255) NOT NULL,
  task_data JSONB        NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE "task_events" (
  task_id   BIGINT    NOT NULL REFERENCES task (id) ON DELETE CASCADE,
  timestamp TIMESTAMP NOT NULL DEFAULT (STATEMENT_TIMESTAMP()),
  type      INT       NOT NULL,
  data      VARCHAR
);
