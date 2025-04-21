CREATE TABLE game_session_players (
    game_session_id UUID NOT NULL REFERENCES game_session(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    PRIMARY KEY (game_session_id, user_id)
);
