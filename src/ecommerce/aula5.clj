(ns ecommerce.aula5
  (:use clojure.pprint)
  (:require [datomic.api :as d]
            [ecommerce.db :as db]
            [ecommerce.model :as model]))

(def conn (db/abre-conexao))

(db/cria-schema conn)

; não me importa como voce extrai o MOMENTO da transacao
; o que importa é vc usar esse momento pro seu as-of
; aqui no resultado da transacao eu podia usaro db-after
(let [computador (model/novo-produto "Computador Novo", "/computador-novo", 2500.10M)
      celular (model/novo-produto "Celular Caro", "/celular", 888888.10M)
      resultado  @(d/transact conn [computador, celular])]
  (pprint resultado))

; meu snapshot, posso usar o momento real
(def fotografia-no-passado (d/db conn))

(let [calculadora {:produto/nome "Calculadora com 4 operacoes"}
      celular-barato (model/novo-produto "Celular Barato", "/celular-barato", 0.1M)
      resultado @(d/transact conn [calculadora, celular-barato])]
  (pprint resultado))

; um snapshot no instante do d/db
(pprint (count (db/todos-os-produtos (d/db conn))))

;rodando a query num banco filtrado com dados do passado
(pprint (count (db/todos-os-produtos fotografia-no-passado)))

; antes
(pprint (count (db/todos-os-produtos (d/as-of (d/db conn) #inst "2022-06-01T15:05:31.626"))))
; no meio
(pprint (count (db/todos-os-produtos (d/as-of (d/db conn) #inst "2022-06-01T15:04:31.626"))))
; depois
(pprint (count (db/todos-os-produtos (d/as-of (d/db conn) #inst "2022-06-01T15:06:31.626"))))


;(db/apaga-banco)