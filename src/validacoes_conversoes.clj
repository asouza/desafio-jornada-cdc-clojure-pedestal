(ns validacoes-conversoes
  (:import [java.time LocalDate LocalDateTime]
           (java.time.format DateTimeFormatter))
  )

(defn valid-date? [pattern data-string]
  (try
    (let [formatter (DateTimeFormatter/ofPattern pattern)]
      (LocalDate/parse data-string formatter)
      )
    (catch Exception _ ;; Se der erro ao converter, retorna nulo
      nil)))

(defn future-date? [pattern data-string]
  (try
    (let [formatter (DateTimeFormatter/ofPattern pattern)
          parsed-date (LocalDate/parse data-string formatter)]
      (.isAfter parsed-date (LocalDate/now))) ;; Verifica se a data é depois da data atual
    (catch Exception _ ;; Se der erro ao converter retorna true, seguindo a ideia da bean validation
      true)))

(defn decimal-string? [valor-string]
  (try
    (BigDecimal. valor-string)
    true
    (catch Exception _ ;; Captura qualquer exceção lançada pela tentativa de conversão
      false)))

(defn decimal-greater-than? [min valor-string]

  (if (and (decimal-string? min) (decimal-string? valor-string))
    (let [decimal-value (BigDecimal. valor-string)]
      (> (.compareTo decimal-value (BigDecimal. min)) 0)
      );; Compara os decimais

    ;se tiver algum erro de conversao, ignora. Alguém deveria ter tratado.
    true
    )
  )
