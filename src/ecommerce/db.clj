(ns ecommerce.db
  (:use clojure.pprint)
  (:require [datomic.api :as d]))

(def db-uri "datomic:dev://localhost:4334/ecommerce")

(defn abre-conexao []
  (d/create-database db-uri)
  (d/connect db-uri))

(defn apaga-banco []
  (d/delete-database db-uri))

; Produtos
; id?
; nome String 1 ==> Computador Novo
; slug String 1 ==> /computador_novo
; preco ponto flutuantes 1 ==> 3500.10

; id_entidade atributo valor
; 15 :produto/nome Computador Novo     ID_TX     operacao
; 15 :produto/slug /computador_novo    ID_TX     operacao
; 15 :produto/preco 3500.10            ID_TX     operacao
; 17 :produto/nome Telefone Caro       ID_TX     operacao
; 17 :produto/slug /telefone           ID_TX     operacao
; 17 :produto/preco 8888.88            ID_TX     operacao

; 45 :produto/nome Telefone Caro       ID_TX     true
; 45 :produto/slug /telefone           ID_TX     true
; 45 :produto/preco 8888.88            ID_TX     true

; 45 :produto/preco 8888.88            ID_TX     false
; 45 :produto/preco 0.1                ID_TX     true
(def schema [{:db/ident       :produto/nome
              :db/valueType   :db.type/string
              :db/cardinality :db.cardinality/one
              :db/doc         "O nome de um produto"}
             {:db/ident       :produto/slug
              :db/valueType   :db.type/string
              :db/cardinality :db.cardinality/one
              :db/doc         "O caminho para acessar esse produto via http"}
             {:db/ident       :produto/preco
              :db/valueType   :db.type/bigdec
              :db/cardinality :db.cardinality/one
              :db/doc         "O preco de um produto com precisao monetaria"}
             {:db/ident       :produto/palavra-chave
              :db/valueType   :db.type/string
              :db/cardinality :db.cardinality/many}
             ])

(defn cria-schema [conn]
  (d/transact conn schema))

(defn todos-os-produtos [db]
  (d/q '[:find ?entidade
         :where [?entidade :produto/nome]] db))

; pull explicito atributo a atributo
(defn todos-os-produtos [db]
  (d/q '[:find (pull ?entidade [:produto/nome :produto/preco :produto/slug])
         :where [?entidade :produto/nome]] db))


; pull generico, vantagem preguica, desvantagem pode trazer mais do que eu queira
(defn todos-os-produtos [db]
  (d/q '[:find (pull ?entidade [*])
         :where [?entidade :produto/nome]] db))

; no sql eh comum fazer:
; String sql - "meu codigo sql"
; conexao.query(sql)

;esse aqui eh similar ao String sql
; eh comum e voce pode querer extrair em um def ou let
; porem... -q é notacao hungara... indica o TIPO... hum.. não parece ser legal
; em clojure
; vc vai encontrar esse padrao em alguns exemplos e documentacao
; nao recomendamos notacao hungara dessa maneira, ainda menos abrevidada ;)

(def todos-os-produtos-por-slug-fixo-q
  '[:find ?entidade
    :where [?entidade :produto/slug "/computador-novo"]])

(defn todos-os-produtos-por-slug-fixo [db]
  (d/q todos-os-produtos-por-slug-fixo-q db))

; não estou usando notacao hungara e extract
; eh comum no sql: String sql = "select * from where slug==::SLUG::"
; conexao.query(sql, {::SLUG:: "/computador-novo"})
(defn todos-os-produtos-por-slug [db slug]
  (d/q '[:find ?entidade
         :in $ ?slug-procurado                              ; proposital difernete da variavel de clojure para evitar erros
         :where [?entidade :produto/slug ?slug-procurado]]
       db slug))

; ?entity => ?entidade => ?e => ?produto => ?p
; se não vai usar, pode ser um _
(defn todos-os-slugs [db]
  (d/q '[:find ?slug
         :where [_ :produto/slug ?slug]] db))

(defn todos-os-produtos-por-preco [db]
  (d/q '[:find ?nome, ?preco
         :keys nome, preco
         :where [?produto :produto/preco ?preco]
         [?produto :produto/nome ?nome]] db))

; estou sendo explicito nos campos 1 a 1
(defn todos-os-produtos-por-preco [db]
  (d/q '[:find ?nome, ?preco
         :keys produto/nome, produto/preco
         :where [?produto :produto/preco ?preco]
         [?produto :produto/nome ?nome]] db))

(defn todos-os-produtos-por-preco [db preco-mimo-requisitado]
  (d/q '[:find ?nome, ?preco
         :in $, ?preco-minimo
         :keys produto/nome, produto/preco
         :where [?produto :produto/preco ?preco]
         [(> ?preco ?preco-minimo)]
         [?produto :produto/nome ?nome]
         ]
       db, preco-mimo-requisitado))

; eu tenho 10mil... se eu tneho 1000 produtos com preco > 5000, so 10 produtos com quantidade < 10
;passar por 10 mil
;[(> preco 5000)]                                            ; => 5000 datom
;[(> quantidade 10)]                                         ; => 10 datom
;
;;passar por 10 mil
;[(> quantidade 10)]                                         ; => 10 datom
;[(> preco 5000)]                                            ; => 10 datom

; em geral vamos deixar as condicoes da mais restritivas pra menos restritiva...
; pois o plano de ação somos nos quem tomamos


(defn todos-os-produtos-por-palavra-chave [db palavra-chave-buscada]
  (d/q '[:find (pull ?produto [*])
         :in $ ?palavra-chave
         :where [?produto :produto/palavra-chave ?palavra-chave]]
    db palavra-chave-buscada))









