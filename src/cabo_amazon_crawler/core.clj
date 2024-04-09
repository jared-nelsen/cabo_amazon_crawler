(ns cabo-amazon-crawler.core
  (:gen-class)
  (:import java.util.concurrent.Executors)
  (:require [clojure.string :as str]
            [clojure.java.io :as io]
            [cabo-amazon-crawler.searchPageCrawler :as searchPageCrawler]
            [cabo-amazon-crawler.productCrawler :as productCrawler]
            [cabo-amazon-crawler.common :as common]
            [cabo-amazon-crawler.database :as database]))

(def threadConfig 
  {8 {:search 2
      :processing 6}
   32 {:search 2
       :processing 13}
   64 {:search 2
       :processing 13}
   128 {:search 5
        :processing 30}})

(defn crawl
  "Starts the crawling session on many threads defined by the above thread config"
  []
  (let [;; Thread Counts
        totalSystemThreads (.availableProcessors (Runtime/getRuntime))
        searchTermPageCrawlingThreadCount (get-in threadConfig [totalSystemThreads :search])
        productProcessingThreadCount (get-in threadConfig [totalSystemThreads :processing])
        totalThreadCount (+ searchTermPageCrawlingThreadCount productProcessingThreadCount)
        ;; Thread Pool
        threadPool (Executors/newFixedThreadPool totalThreadCount)
        ;; Tasks
        searchTermPageCrawlingTasks (map (fn [idx] (fn [] (searchPageCrawler/processSearchPageLinks idx))) (range searchTermPageCrawlingThreadCount))
        productProcessingTasks (map (fn [idx] (fn [] (productCrawler/processProductLinks idx))) (range productProcessingThreadCount))
        allTasks (concat searchTermPageCrawlingTasks productProcessingTasks)
        ;; Gather Search Terms
        searchConfig (str/split-lines (slurp (io/resource "current-search-config.txt")))
        searchPageLinks (searchPageCrawler/generateAllSearchLinksForSearchConfig searchConfig)]
    (database/loadPreviouslyCrawledASINs!)
    (common/populateSearchPageQueue searchPageLinks)
    (.invokeAll threadPool allTasks)))

(defn -main
  "Starts the Cabo Crawler.
   - Starts the product page crawler on 1 thread.
   - Starts N product page processors on remaining available threads."
  [& _] 
  (println "\nCabo Amazon Crawler started...\n")
  (crawl)
  (println "\n\nCabo Crawler finished!\n"))