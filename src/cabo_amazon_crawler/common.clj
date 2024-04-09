
(ns cabo-amazon-crawler.common
  (:gen-class)
  (:require [clojure.string :as strng]
            [hickory.core :as hickory]
            [cabo-amazon-crawler.requestor :as requestor]))

;; ------------------------------------------------------------------------
;; Search Parameters
;; ------------------------------------------------------------------------

;; The count of how many pages to search per term
(def searchPageCount 300)
;; The prototype search page URL for amazon.com
(def searchPagePrototypeURL "https://www.amazon.com/s?k=%s&page=%s&rh=p_85%%3A2470955011&dc")

;; ------------------------------------------------------------------------
;; Common queues, counts, and sets
;; ------------------------------------------------------------------------

;; Determines the modes of operation
(def debugMode false)
;; Determines if data will actually be inserted into the database
(def insertingData false)
;; Determings if ASINs are going to be read in at start time
(def loadPriorASINs false)
;; The queue of amazon product page links that are to be crawled
;;    Populated by the page crawler processors
;;    Consumed by the product crawling processesors
(def productLinkMapQueue (atom clojure.lang.PersistentQueue/EMPTY))
;; The set of Amazon Unique Identifier numbers crawled during this session
;;    Optionally loaded with ASINS crawled so far at start time     
(def crawledASINsDetectionSet (atom #{}))
;; The count of products crawled during this session
(def crawledProductCount (atom 0))
;; The queue of search pages to crawl for links
(def searchPageLinkMapQueue (atom clojure.lang.PersistentQueue/EMPTY))
;; The set of Crawled Search Page Links
(def crawledProductPageLinkDetectionSet (atom #{}))

(defn populateSearchPageQueue
  "Populates the search page queue with the given links"
  [links]
  (let [populatedQueue (into (clojure.lang.PersistentQueue/EMPTY) links)]
    (reset! searchPageLinkMapQueue populatedQueue)))

;; --------------------------------------------------------------------------
;; Common Functions
;; --------------------------------------------------------------------------

(defn trimToAbosuleProductLink
  "Trims a given product link to its absolute path"
  [link]
  (try
    (let [indexOfBeginIdentifier (strng/index-of link "/dp/B0")
          indexOfEndOfIdentifier (+ 14 indexOfBeginIdentifier)]
      (subs link 0 indexOfEndOfIdentifier))
    (catch Exception _
      "")))

(defn isAPhysicalProduct
  "Detects if an assembled product is a physical product"
  [productData]
  (false? (or (nil? (:product-title productData))
              (nil? (:unique-identifier productData)))))

(defn isAProductLinkPage
  "Detects if a link is not a product page link by searching for a substring common to all product links"
  [link]
  (true? (and (true? (strng/includes? link "/dp/B0"))
              (false? (strng/includes? link "#customerReviews")))))

(defn parseASIN
  [url]
  (if (strng/includes? url "/B0")
    (let [asinStart (strng/index-of url "B0")
          asinEnd (+ asinStart 10)
          ASIN (subs url asinStart asinEnd)]
      ASIN)
    ""))

(defn seenProductBefore?
  "Detects if a product has been seen before by its link"
  [link]
  (contains? @crawledASINsDetectionSet (parseASIN link)))

(defn markProductVisited
  "Sets a product as visited by updating sets"
  [productData]
  (let [ASIN (:unique-identifier productData)]
    (when (not= "" ASIN)
      (swap! crawledASINsDetectionSet conj ASIN)))
  (swap! crawledProductCount inc))

(defn hitCaptchaOrAutomatedAccessRequest
  "Detects two cases that prevent crawling:
   - Hit a Captcha
   - Response requests that you get automated access to Amazon Data."
  [page]
  (let [pageStr (str page)]
    (true? (or (strng/includes? pageStr "not a robot")
               (strng/includes? pageStr "automated access to Amazon data")))))

(defn downloadPage
  "Downloads the given URL and converts it to hickory format"
  [url]
  (let [response (requestor/httpGET url)
        exceptionHit (:hitException response)
        hickoryPage (-> response
                        :body
                        hickory/parse
                        hickory/as-hickory)]
    (if (or exceptionHit (hitCaptchaOrAutomatedAccessRequest hickoryPage))
      {:blocked true}
      hickoryPage)))