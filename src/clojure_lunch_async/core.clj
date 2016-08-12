(ns clojure-lunch-async.core
  (:require [clojure.core.async :as async :refer [chan timeout
                                                  buffer sliding-buffer dropping-buffer
                                                  <! <!! >! >!!
                                                  go go-loop thread
                                                  alt! alt!! alts! alts!!
                                                  poll! offer!
                                                  onto-chan to-chan
                                                  close!
                                                  mix admix unmix]]))

(defn merge-go-blocks [n]
  (let [chans (map #(go %) (range 1 n))
        merged-chan (async/merge chans)]
    (alts!! [(async/reduce + 0 merged-chan)
             (timeout 5000)])))

(merge-go-blocks 1000)
