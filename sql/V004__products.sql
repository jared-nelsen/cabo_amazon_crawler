CREATE TABLE products (
    ID uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
    -- FK: Crawl
    crawl_id uuid NOT NULL,
    -- Defaults
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
	updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
	deleted_at TIMESTAMP WITH TIME ZONE,
    -- Data
    product_title TEXT NOT NULL,
    origin_site TEXT NOT NULL,
    search_page_link TEXT NOT NULL,
    unique_identifier TEXT,
    product_url TEXT NOT NULL,
    product_category TEXT NOT NULL,
    url_with_affiliate_tag TEXT
);