CREATE TABLE game_session (
    id UUID PRIMARY KEY,
    status game_status NOT NULL DEFAULT 'WAIT_FOR_PLAYERS',
    player_scores JSONB,
    current_player_index INTEGER DEFAULT 0,
    next_play_index INTEGER DEFAULT 0,
    block_next_player BOOLEAN DEFAULT false
);
