(ns cabo-amazon-crawler.database
  (:require [next.jdbc :as jdbc]
            [honey.sql :as sql]
            [honey.sql.helpers :as h]
            [clojure.core :as c]
            [cabo-amazon-crawler.common :as common]))

(def db-config {:dbtype "postgresql"
                :port 5432
                :host "localhost"
                :user "postgres"
                :dbname "cabo_db"
                :password "my_database_passwd"})

(def DB (jdbc/get-datasource db-config))

(defn- executeOneSQL!
  "Executes SQL that returns a single result"
  [sqlString]
  (try
    (with-open [connection (jdbc/get-connection DB)]
      (jdbc/execute-one! connection sqlString))
    (catch Exception e
      (println (str "Error executing-one SQL: " (.toString e))))))

(defn- executeSQL!
  "Executes SQL that returns results"
  [sqlMap]
  (try
    (with-open [connection (jdbc/get-connection DB)]
      (jdbc/execute! connection sqlMap))
    (catch Exception e
      (println (str "Error executing SQL: " (.toString e))))))

(defn insertProductRecord
  "Inserts a product record into the database"
  [productMap]
  (when (true? common/insertingData)
    (-> (h/insert-into :products)
        (h/values [productMap])
        (h/returning :ID)
        (sql/format)
        (executeOneSQL!)
        (:ID))))

(defn insertProductImage
  "Inserts a product image into the database"
  [imageMap]
  (when (true? common/insertingData)
    (-> (h/insert-into :product_images)
        (h/values [imageMap])
        (h/returning :ID)
        (sql/format)
        (executeOneSQL!)
        (:ID))))

(defn retrieveAllCrawledASINs
  []
  (if (true? common/loadPriorASINs)
    (let [sqlResults (-> (h/select :unique-identifier)
                         (h/from :products)
                         (sql/format)
                         (executeSQL!))
          ASINSet (->> sqlResults
                       (map first)
                       (map second)
                       (set))]
      ASINSet)
    #{}))

(defn loadPreviouslyCrawledASINs!
  "Reads all previously crawled ASINs from the database and loads them into the
   detection set"
  []
  (println "Loading all previously crawled ASINs...")
  (let [previouslyCralwedASINSet (retrieveAllCrawledASINs)]
    (reset! common/crawledASINsDetectionSet previouslyCralwedASINSet)
    (println "... Loaded Successfully!\n\n")))