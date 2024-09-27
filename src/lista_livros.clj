(ns lista-livros
  (:require
    [utilitarios]
    )
  )

(defn- converte-livro-saida [livro]
  {
   :titulo (:titulo livro)
   :id (:id livro)
   }
  )

(def handler
  {
   :name :lista-livros
   :enter (fn [context]
            (let [livros (get-in context [:request :database :livros])]
              (utilitarios/respond-with-json context (map converte-livro-saida livros))
              ))
   }
  )
