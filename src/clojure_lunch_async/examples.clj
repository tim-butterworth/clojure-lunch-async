(ns clojure-lunch-async.examples
  (:require [clojure.core.async :as async :refer :all]))

;; It really is async and not just a bluff
(import java.io.Thread)
(def file "/tmp/clojure-go-file.txt")

(defn get-thread-string []
  (let [thread-name (. (Thread/currentThread) getName)]
    (str thread-name " -> " thread-name "\n")))

(defn write-to-file []
  (doseq [x (range 0 50)]
    (spit file (get-thread-string)
          :append true)))

(defn write-file-example []
  (async/go
    (loop [index 0]
      (if (< index 1000)
        (do
          (write-to-file)
          (recur (inc index)))))))
;;















;; Channels
;;
;; Read from some channels

(def running (atom true))

(def channels
  {:sports (async/chan)
   :justice (async/chan)
   :news (async/chan)
   :trending-now (async/chan)})

(defn start-read-machine [channel-map loop-fn]
  (do
    (swap! running (fn [n] true))
    (loop-fn channel-map)))

;; blocking channel take example
(defn blocking-loop [key-array channel-map]
  (loop []
    (if @running
      (do
        (doseq [key key-array]
          (println (<!! (channel-map key))))
        (recur)))))

(defn blocking-thread [channel-map]
  (async/thread
    (let [key-array (keys channel-map)]
      (do (println key-array)
          (blocking-loop key-array channel-map)))))

(defn start-blocking-read-thread []
  (start-read-machine channels blocking-thread))
;; blocking channel take example


;; non-blocking take example
(defn non-blocking-loop [channel-map]
  (let [channel-keys (keys channel-map)]
    (doseq [key channel-keys]
      (let [channel (key channel-map)]
        (async/go-loop []
          (if @running
            (do
              (println (<! channel))
              (recur))))))))

(defn start-non-blocking-read []
  (start-read-machine channels non-blocking-loop))
;; non-blocking take example


(defn blocking-write-to-channel
  ([channel-key]
   (blocking-write-to-channel channel-key (str "name " (name channel-key))))
  ([channel-key message]
   (async/>!!
    (channels channel-key)
    message)))

(defn parking-write-to-channel
  ([channel-key]
   (parking-write-to-channel channel-key (str "name " (name channel-key))))
  ([channel-key message]
   (async/go
     (async/>!
      (channels channel-key)
      message))))

(defn shutdown-read-loop [channel-map]
  (let [channel-keys (keys channel-map)]
    (do
      (swap! running (fn [n] false))
      (doseq [key channel-keys]
        (parking-write-to-channel key (str (name key) " loop ending"))))))

;; either way, the reading thread is going to read from the channels in key order, everyone has to take a turn before anyone gets a second turn



(defn upper-case [string]
  (clojure.string/upper-case string))

(defn str-reverse [string]
  (clojure.string/reverse string))

(defn add-style [string]
  (str "$-^-*-" string "-*-^-$"))(defn upper-case [string]
(clojure.string/upper-case string))

(defn str-reverse [string]
  (clojure.string/reverse string))

(defn add-style [string]
  (str "$-^-*-" string "-*-^-$"))

(defn do-all-the-things [string]
  (add-style
   (str-reverse
    (upper-case string))))

;; Shamelessly taken from http://www.braveclojure.com/core-async/#Escape_Callback_Hell_with_Process_Pipelines
(defn upper-caser
  [in]
  (let [out (chan)]
    (go (while true (>! out (upper-case (<! in)))))
    out))

(defn reverser
  [in]
  (let [out (chan)]
    (go (while true (>! out (str-reverse (<! in)))))
    out))

(defn add-style
  [in]
  (let [out (chan)]
    (go (while true (>! out (add-style (<! in)))))
    out))

(defn printer
  [in]
  (go (while true (println (<! in)))))

(def in-chan (chan))
(def upper-caser-out (upper-caser in-chan))
(def reverser-out (reverser upper-caser-out))
(def add-style-out (add-style reverser-out))
(printer add-style-out)


;;;
(defn blocking-call-to-first-service [body]
  (if (= (:credentials body) :ok)
    {"response"
     {"content"
      {:super-secret :fancy-secret1}}}
    {"response" {"content" "error"}}))

(defn blocking-call-to-second-service [body]
  (if (= (:super-secret body) :fancy-secret1)
    {"response" {"content" [1 2 3]}}
    {"response" {"content" "error"}}))

(defn make-the-service-calls [initial-request]
  (let [response-content (((blocking-call-to-first-service initial-request) "response") "content")]
    (blocking-call-to-second-service response-content)))

(let [c1 (chan) c2 (chan)]
  (pipe (go 42) c2)
  (<!! c2))


