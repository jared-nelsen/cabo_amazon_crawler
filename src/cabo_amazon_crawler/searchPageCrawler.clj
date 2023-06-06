(ns cabo-amazon-crawler.searchPageCrawler
  (:require [cabo-amazon-crawler.common :as common]
            [cabo-amazon-crawler.selectors :as selector]
            [clojure.string :as str]))

;; --------------------------------------------------------------------------------------------------------------------------------
;; Crawling Logic
;; --------------------------------------------------------------------------------------------------------------------------------

(defn enqueueProductLinkMap
  "Enqueues the link to redis"
  [linkMap]
  (let [productLink (:productLink linkMap)]
    (when (false? (contains? @common/crawledProductPageLinkDetectionSet productLink))
      (swap! common/crawledProductPageLinkDetectionSet conj productLink)
      (swap! common/productLinkMapQueue conj linkMap))))

(defn handleBeingBlocked
  "Handles being blocked by re-enqueueing the search page link to the queue"
  [searchPageLink searchTerm]
  (let [searchPageLinkMap {:searchPageLink searchPageLink
                           :searchTerm searchTerm}]
    (swap! common/searchPageLinkMapQueue conj searchPageLinkMap)))

(defn crawlSearchResultsPage
  "Crawls a given search results page given its link"
  [searchPageLink searchTerm category]
  (let [searchPageHTML (common/downloadPage searchPageLink)]
    (if (true? (:blocked searchPageHTML))
      (handleBeingBlocked searchPageLink searchTerm)
      (let [productLinksOnSearchPage (selector/retrieveProductLinksOnSearchPage searchPageHTML)]
        (doseq [productLink productLinksOnSearchPage]
          (let [linkMap {:productLink productLink
                         :searchPageLink searchPageLink
                         :category category}]
            (enqueueProductLinkMap linkMap)))
        (println (str "**** Crawled Search Page: " (count @common/searchPageLinkMapQueue) " pages remaining..."))))))

(defn generateSearchPageLinksForTerm
  "Generates a vector of search pages to crawl based on the given search term"
  [category searchTerm]
  (loop [index 1
         generatedLinks []
         searchPagePrototypes (repeat common/searchPageCount common/searchPagePrototypeURL)]
    (if (empty? searchPagePrototypes)
      generatedLinks
      (let [prototype (first searchPagePrototypes)
            formattedLink (format prototype searchTerm index)
            pageLinkMap {:searchPageLink formattedLink :searchTerm searchTerm :category category}]
        (recur (inc index) (conj generatedLinks pageLinkMap) (rest searchPagePrototypes))))))

(defn generateAllSearchLinksForSearchEntry
  "Takes a vector of search terms and generates all search page links to crawl for them"
  [searchEntry]
  (let [searchEntryTokens (str/split searchEntry #",")
        category (first searchEntryTokens)
        searchTerms (rest searchEntryTokens)]
    (->> searchTerms
         (map #(generateSearchPageLinksForTerm category %))
         (flatten)
         (vec))))

(defn generateAllSearchLinksForSearchConfig
  "Takes the search config and generates links based on it"
  [searchConfigLines]
  (->> (map #(generateAllSearchLinksForSearchEntry %) searchConfigLines)
       (flatten)
       (vec)))

;; --------------------------------------------------------------------------------------------------------------------------------
;; Processor
;; --------------------------------------------------------------------------------------------------------------------------------

(defn dequeueSearchPageLink
  "Dequeues a single search term from the product link queue and attampts to process it"
  []
  (let [searchPageLinkMap (ffirst (swap-vals! common/searchPageLinkMapQueue pop))]
    (when (false? (nil? searchPageLinkMap))
      (let [searchTermLink (:searchPageLink searchPageLinkMap)
            searchTerm (:searchTerm searchPageLinkMap)
            category (:category searchPageLinkMap)]
        (crawlSearchResultsPage searchTermLink searchTerm category)))))

(defn processSearchPageLinks 
  [threadIndex]
  (while true
    (dequeueSearchPageLink)))

;; --------------------------------------------------------------------------------------------------------------------------------
;; --------------------------------------------------------------------------------------------------------------------------------