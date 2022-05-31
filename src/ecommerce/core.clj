(ns ecommerce.core
  (:use clojure.pprint)
  (:require [datomic.api :as d]
            [ecommerce.db :as db]
            [ecommerce.model :as model]))
;iniciar datomic
;cd /Users/diego.lacerda/Documents/softwares/datomic-pro-0.9.5951
;bin/transactor config/dev-transactor-template.properties
