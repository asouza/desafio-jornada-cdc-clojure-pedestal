(ns lista-autores
  (:require
    [utilitarios]
    )
  )

;chatgpt me disse que eu poderia ter explodido aqui nas tres variáveis que eu criei.
;Ao mesmo tempo disse que meu código está ok
(defn- converte-linha-autor-saida-lista [autor]
  {
   :nome (:nome autor)
   :email (:email autor)
   :descricao (:descricao autor)
   }
  )

(def handler
  {
   :name :lista-autores
   :enter (fn [context]
            (let [linhas-autores (get-in context [:request :database :autores])]
              (utilitarios/respond-with-json context (map converte-linha-autor-saida-lista linhas-autores))
              ))
   }
  )
