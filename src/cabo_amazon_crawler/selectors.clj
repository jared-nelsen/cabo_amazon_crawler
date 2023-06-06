(ns cabo-amazon-crawler.selectors
  (:require [hickory.select :as s]
            [clojure.string :as str]
            [cabo-amazon-crawler.common :as common]))

(defn formNonRelativeProductURL
  "Forms a non relative product URL given a url fragment"
  [urlFragment]
  (str "https://www.amazon.com" urlFragment))

(defn retrieveProductTitle
  "Retrieves the products title off the given page"
  [page]
  (let [product-title-content (-> (s/select (s/child (s/id "productTitle")) page))
        product-title-str (->> product-title-content
                               (first)
                               (:content)
                               (first))]
    (when (false? (nil? product-title-str))
      (str/trim product-title-str))))

(defn retrievePrimaryImageURL
  [page]
  (let [image-url (-> (s/select (s/child (s/id "landingImage")) page))]
    (->> image-url
         (first)
         (:attrs)
         (:data-old-hires))))

(defn retrieveProductLinksOnSearchPage
  "Takes a product page and returns a vector of all product links on the page. There may be some links that are not product but those will be
   filtered out later."
  [pageHTML]
  (let [hickory-links (-> (s/select (s/child (s/class "s-underline-link-text")) pageHTML))]
    (->> hickory-links
         (map :attrs)
         (map :href)
         (map #(formNonRelativeProductURL %))
         (filter #(common/isAProductLinkPage %))
         (map #(common/trimToAbosuleProductLink %))
         (filter #(not= "" %))
         (vec))))