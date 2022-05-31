(ns ecommerce.core
  (:use clojure.pprint)
  (:require [datomic.api :as d]
            [ecommerce.db :as db]
            [ecommerce.model :as model]))
;iniciar datomic
;cd /Users/diego.lacerda/Documents/softwares/datomic-pro-0.9.5951
;bin/transactor config/dev-transactor-template.properties


(def conn (db/abre-conexao))

(db/cria-schema conn)

(let [computador (model/novo-produto "Computador Novo", "/computador_novo", 2500.10M)]
  (d/transact conn [computador]))

;o banco no instante que executou a linha
(def db (d/db conn))

(d/q '[:find ?entidade
       :where [?entidade :produto/nome]] db)

(let [celular (model/novo-produto "Celular Caro", "/celular", 888888.10M)]
  (d/transact conn [celular]))

; tirando uma nova fotografia (SNAPSHOT) do banco
(def db (d/db conn))

(d/q '[:find ?entidade
       :where [?entidade :produto/nome]] db)

; o datomic suporte somente um dos identificadores, claro, não foi imposta nenhuma restrição
(let [calculadora {:produto/nome "Calculadora com 4 operacoes"}]
  (d/transact conn [calculadora]))

; não funciona pois se você quer algo "vazio", é só não colocar.
;(let [radio-relogio {:produto/nome "Radio com relogio" :produto/slug nil}]
;  (d/transact conn [radio-relogio]))



; 45 :produto/nome Telefone Caro       ID_TX     true
; 45 :produto/slug /telefone           ID_TX     true
; 45 :produto/preco 8888.88            ID_TX     true

; 45 :produto/preco 8888.88            ID_TX     false
; 45 :produto/preco 0.1                ID_TX     true
(let [celular-barato (model/novo-produto "Celular Barato", "/celular-barato", 888888.10M)
      resultado @(d/transact conn [celular-barato])
      ;id-entidade (first (vals (:tempids resultado)))
      id-entidade (-> resultado :tempids vals first)]
  (pprint resultado)
  (pprint @(d/transact conn [[:db/add id-entidade :produto/preco 0.1M]]))
  (pprint @(d/transact conn [[:db/retract id-entidade :produto/slug "/celular-barato"]]))
  )

(db/apaga-banco)