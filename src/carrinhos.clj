(ns carrinhos

  (:require
    [utilitarios]
    )
  )

;chatgpt me disse que eu poderia ter explodido aqui nas tres variáveis que eu criei.
;Ao mesmo tempo disse que meu código está ok
(defn- converte-carrinho-saida-lista [carrinho]
    (:itens carrinho)
  )




(def handler
  {
   :name :lista-carrinhos
   :enter (fn [context]
            (let [carrinhos (get-in context [:request :database :carrinhos])]
              (println carrinhos)
              (utilitarios/respond-with-json context (map converte-carrinho-saida-lista carrinhos))
              ))
   }
  )
