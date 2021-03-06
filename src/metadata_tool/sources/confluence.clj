;
; Copyright 2017 Fintech Open Source Foundation
; SPDX-License-Identifier: Apache-2.0
;
; Licensed under the Apache License, Version 2.0 (the "License");
; you may not use this file except in compliance with the License.
; You may obtain a copy of the License at
;
;     http://www.apache.org/licenses/LICENSE-2.0
;
; Unless required by applicable law or agreed to in writing, software
; distributed under the License is distributed on an "AS IS" BASIS,
; WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
; See the License for the specific language governing permissions and
; limitations under the License.
;
(ns metadata-tool.sources.confluence
    (:require [clojure.string        :as s]
              [clj-http.client       :as http]
              [clj-http.conn-mgr     :as conn]
              [metadata-tool.config  :as cfg]
              ))

(def cm (conn/make-reusable-conn-manager {}))
(defn client []
    (:http-client
        (http/get (:host (:confluence cfg/config)) {
            :connection-manager cm 
            :cache true})))

(defn cget [& args]
    (http/get (str (:host (:confluence cfg/config)) "/wiki/rest/api/" (apply str args)) {
        :basic-auth [
            (:username (:confluence cfg/config))
            (:password (:confluence cfg/config))]
        :connection-manager cm 
        :http-client (client)
        :cache true
        :as :json}))

(defn page-id
    [url]
    (nth 
        (s/split url #"/") 4))

(defn content
    [id]
    (:value (:storage (:body (:body 
        (cget "content/" id "?expand=body.storage"))))))

(defn children
    [id]
    (try
        (:results (:body (cget "content/" id "/child/page")))
        ; TODO - check Exception status code (clj-http)
        ; tried with (:status (:data e))
        (catch Exception e [])))
