(ns ecommerce.core
  (:use clojure.pprint)
  (:require [datomic.api :as d]
            [ecommerce.db :as db]))
;iniciar datomic
;cd /Users/diego.lacerda/Documents/softwares/datomic-pro-0.9.5951
;bin/transactor config/dev-transactor-template.properties


(def conn (db/abre-conexao))
(pprint conn)

; Produtos
; id?
; nome String 1 ==> Computador Novo
; slug String 1 ==> /computador_novo
; preco ponto flutuantes 1 ==> 3500.10
; 17 nome Telefone Caro
; 17 slug /telefone
; 17 preco 8888.88
