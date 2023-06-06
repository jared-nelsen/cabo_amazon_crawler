
CREATE TABLE crawls (
    ID uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
    -- FK: Domain
    domain_id uuid,
    -- Defaults
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
	updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
	deleted_at TIMESTAMP WITH TIME ZONE,
    -- Data
    topic TEXT NOT NULL,
    search_terms Text [],
    sites_to_crawl TEXT [],
    -- Constraints
    CONSTRAINT fk_domain
      FOREIGN KEY(domain_id) 
	    REFERENCES domains(ID)
        ON DELETE CASCADE
);