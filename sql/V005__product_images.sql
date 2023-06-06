CREATE TABLE product_images (
    ID uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
    -- FK: Product
    product_id uuid NOT NULL,
    -- Defaults
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
	updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
	deleted_at TIMESTAMP WITH TIME ZONE,
    -- Data
    image_url TEXT NOT NULL,
    image_file_name TEXT NOT NULL,
    image_byte_arr bytea,
    -- Constraints
    CONSTRAINT fk_product
      FOREIGN KEY(product_id) 
	    REFERENCES products(ID)
        ON DELETE CASCADE
);