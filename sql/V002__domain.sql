
CREATE TABLE domains (
    ID uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
    -- Defaults
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
	updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
	deleted_at TIMESTAMP WITH TIME ZONE
);