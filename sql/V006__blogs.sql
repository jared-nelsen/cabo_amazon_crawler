CREATE TABLE blogs (
    ID uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
    -- FK: Product
    product_id uuid NOT NULL,
    -- Defaults
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
	updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
	deleted_at TIMESTAMP WITH TIME ZONE,
    -- Data
    blog_title TEXT NOT NULL,
    blog_string TEXT NOT NULL,
    blog_url TEXT NOT NULL,
    uploaded BOOLEAN NOT NULL DEFAULT FALSE,
    uploaded_date TIMESTAMP WITH TIME ZONE,
    prompts TEXT [],
    meta_description TEXT NOT NULL,
    -- Constraints
    CONSTRAINT fk_product
      FOREIGN KEY(product_id) 
	    REFERENCES products(ID)
        ON DELETE CASCADE
);