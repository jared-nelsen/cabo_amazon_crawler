(ns cabo-amazon-crawler.productCrawler
  (:require [cabo-amazon-crawler.selectors :as selector]
            [cabo-amazon-crawler.common :as common]
            [cabo-amazon-crawler.database :as database]
            [cabo-amazon-crawler.requestor :as requestor]))

;; --------------------------------------------------------------------------------------------------------------------------------
;; Data Formation
;; --------------------------------------------------------------------------------------------------------------------------------

(defn handleBeingBlocked
  "Handles being blocked by re-enqueueing the product page link to the queue"
  [linkMap]
  (swap! common/productLinkMapQueue conj linkMap)
  {:blocked true})

(defn formImageDataMap
  "Persists the product image to disk"
  [pageData productData]
  (let [img-url (selector/retrievePrimaryImageURL pageData)
        imageResponseBody (requestor/httpGETImage img-url)]
    (if (true? (:hitException imageResponseBody))
      (handleBeingBlocked {:productLink (:product-url productData)
                           :searchPageLink (:search-page-link productData)
                           :category (:product-category productData)})
      {:product-id (:ID productData)
       :image-url img-url
       :image-file-name (str (:product-title productData) ".jpg")
       :image-byte-arr (:body imageResponseBody)})))

(defn formProductDataMap
  "Retrieves relevant data off of a product page and returns a map of it"
  [pageData linkMap]
  {:ID (java.util.UUID/randomUUID)
   :crawl-id (java.util.UUID/randomUUID)
   :product-title (selector/retrieveProductTitle pageData)
   :origin-site "www.amazon.com"
   :search-page-link (:searchPageLink linkMap)
   :unique-identifier (common/parseASIN (:productLink linkMap))
   :product-url (:productLink linkMap)
   :url-with-affiliate-tag (str (:productLink linkMap) "?tag=980130455-20")
   :product-category (:category linkMap)})

;; --------------------------------------------------------------------------------------------------------------------------------
;; Crawling Logic
;; --------------------------------------------------------------------------------------------------------------------------------

(defn processProductAndImageData
  "Processes the given product data"
  [productData imageData]
  (database/insertProductRecord productData)
  (database/insertProductImage imageData)
  (common/markProductVisited productData)
  (println (str "-- Crawled Product #" @common/crawledProductCount " - " (:product-title productData) "\n\t" (count @common/productLinkMapQueue) " remain in the queue...")))

(defn processLinkMap
  "Processes a link"
  [linkMap]
  (when (false? (common/seenProductBefore? (:productLink linkMap)))
    (let [link (:productLink linkMap)
          productPageHTML (common/downloadPage link)]
      (if (true? (:blocked productPageHTML))
        (handleBeingBlocked linkMap)
        (let [productData (formProductDataMap productPageHTML linkMap)]
          (when (common/isAPhysicalProduct productData)
            (let [imageData (formImageDataMap productPageHTML productData)]
              (when (false? (true? (:blocked imageData)))
                (processProductAndImageData productData imageData)))))))))

;; --------------------------------------------------------------------------------------------------------------------------------
;; Multi Threaded Processor
;; --------------------------------------------------------------------------------------------------------------------------------

(defn dequeueProductLinkMap
  "Dequeues a single link from the product link queue and attempts to process it"
  [] 
  (let [linkMap (ffirst (swap-vals! common/productLinkMapQueue pop))]
    (when (and (false? (nil? linkMap)) (false? common/debugMode))
      (processLinkMap linkMap))))

(defn processProductLinks
  "Processes product links in a loop by calling dequeue link repeatedly."
  [threadIndex]
  (while true
    (dequeueProductLinkMap)))

;; --------------------------------------------------------------------------------------------------------------------------------
;; --------------------------------------------------------------------------------------------------------------------------------