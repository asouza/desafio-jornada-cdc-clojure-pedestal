(defproject my-project "0.1.0-SNAPSHOT"
  :description "My Pedestal Project"
  :dependencies [[org.clojure/clojure "1.12.0"]
                 [io.pedestal/pedestal.jetty "0.7.0" ]
                 [org.clojure/data.json "2.5.0" ]
                 [org.slf4j/slf4j-simple "2.0.10"]
                 [metosin/malli "0.10.0" ]
                 [org.postgresql/postgresql "42.5.1"]
                 [com.datomic/peer "1.0.7187"]
                 ]
  :plugins [[lein-exec "0.3.7"]]

  ;; Caminhos das fontes e testes
  :source-paths ["src"]
  :test-paths ["test"]
  :java-source-paths ["src/java"]

  ;; Perfil de testes
  :profiles
  ;o state-flow depende de uma versão antiga do org.clj-commons/pretty e que confluta com a versão 0.7 do pedestal
  {:test {:dependencies [[nubank/state-flow "5.18.0" :exclusions [org.clj-commons/pretty]]
                         [nubank/matcher-combinators "3.9.1"]
                         [org.clojure/test.check "1.1.1"]]
          ;; Use o executor de testes padrão do lein
          }})
