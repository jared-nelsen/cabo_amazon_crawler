(defproject cabo_amazon_crawler "1.0.0"
  :description "Big Amazon Blogs Big Dreams"
  :url "https://github.com/jared-nelsen/operation_cabo_affinitas"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [org.clj-commons/hickory "0.7.3"]
                 [clj-http "3.12.3"]
                 [com.github.seancorfield/next.jdbc "1.3.847"]
                 [org.postgresql/postgresql "42.2.9"]
                 [com.github.seancorfield/honeysql "2.4.972"]]
  :repl-options {:init-ns cabo-amazon-crawler.core}
  :main ^:skip-aot cabo-amazon-crawler.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}}
  :uberjar-name "cabo-amazon-crawler.jar"
  :autoclean true
  :jvm-opts ["-Xmx225g"]
  :reflection-warnings {:global-vars {*warn-on-reflection* false}})
