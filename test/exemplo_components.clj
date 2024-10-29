(ns exemplo-components
  (:require [com.stuartsierra.component :as component])
  )

(defrecord Database [host port]
  component/Lifecycle

  ;o parametro do start é a referência para o próprio record
  (start [component]
    (println "Iniciando o banco")
    (println component)
    (assoc component :connection (fn [] (println "conectando...")))
    )
  
  (stop [component]
    (println "parando o banco ")
    (assoc component :connection nil)
    )
  )

(defn get-user [database username]
  (println (str "Vai realizar uma query usando o " database))
  (str "select * from users where username = " username)
  )

(defrecord ExemploB []
           component/Lifecycle
  (start [this]
    (println "iniciando b")
    this
    )
  
  (stop [this]
       (println "parando b")
       this)  

  )

(defrecord ExampleComponent [database exemplo-b]
  component/Lifecycle

  ;aqui a gente pode assumir que a dependencia já foi inicializada
  (start [this]
         (println ";; Starting ExampleComponent")
         (println (str "dependendo de " database " e " exemplo-b))
         (assoc this :admin (get-user database "admin")))
  ;aqui a gente assume que a dependencia só vai ser parada depois deste stop
 (stop [this]
       (println ";; Stopping ExampleComponent")
       this)
)  

(defn exemplo-sistema [configuracao]
  (component/system-map
   :db (->Database (:host configuracao) (:porta configuracao))
   :exemplo-b (->ExemploB)
   :app (component/using 
         ;pelo que entendi aqui eu preciso usar a construção via mapa...
         ;já que as dependencias gerenciadas vão ser supridas pelo container. 
         (map->ExampleComponent {})
         ;a ligação é feita pelo nome dos parametros. 
         ;se eu troco o nome do parametro, quebra aqui.
         {
          :exemplo-b :exemplo-b
          :database :db 
          }
         ) 
  )
  )

(def system (exemplo-sistema {:host "localhost" :porta 3000}))


(component/start system)