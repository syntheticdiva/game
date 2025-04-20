CREATE TABLE turns (
    id UUID PRIMARY KEY,
    game_session_id UUID NOT NULL REFERENCES game_session(id) ON DELETE CASCADE,
    player_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    card_id UUID REFERENCES cards(uuid) ON DELETE SET NULL,
    action VARCHAR(255),
    timestamp TIMESTAMP NOT NULL,
    score_before INTEGER NOT NULL,
    score_after INTEGER NOT NULL
);