CREATE TABLE cards (
    uuid UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    type card_type NOT NULL,
    value INTEGER NOT NULL,
    game_session_id UUID REFERENCES game_session(id) ON DELETE CASCADE,
    played_by_id UUID REFERENCES users(id) ON DELETE SET NULL,
    played BOOLEAN DEFAULT false
    order_index INTEGER NOT NULL DEFAULT 0
);