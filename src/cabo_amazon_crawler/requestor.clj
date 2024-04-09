(ns cabo-amazon-crawler.requestor
  (:import (org.apache.http.impl.client HttpClientBuilder))
  (:require [clj-http.client :as http]))

;; Manages HTTP calls through the proxy

(defn- cookie-disabler [^HttpClientBuilder builder
                        request]
  (when (:disable-cookies request)
    (.disableCookieManagement builder)))

(def scrapeDoAPIToken "MY_SCRAPE_DO_API_TOKEN")

(defn formRequestURL
  [url]
  (let [encodedURL (java.net.URLEncoder/encode url)]
    (format "http://api.scrape.do?token=%s&url=%s&geoCode=us" scrapeDoAPIToken encodedURL)))

(defn httpGET
  [url]
  (try
    (http/get (formRequestURL url) {:http-builder-fns [cookie-disabler] :disable-cookies true})
    (catch Exception _
      {:hitException true})))

(defn httpGETImage
  [url]
  (try
    (http/get (formRequestURL url) {:as :byte-array
                                    :http-builder-fns [cookie-disabler]
                                    :disable-cookies true})
    (catch Exception _
      {:hitException true})))