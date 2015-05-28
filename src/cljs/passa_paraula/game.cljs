(ns passa-paraula.game
  (:require  [reagent.core :as reagent :refer [atom]]))


(def letters [\A \B \C \D \E \F \G \H \I \J \K \L \M \N \O \P \Q \R \S \T \V \W \X \Y \Z])
(def num-letters (count letters))


(def starting-state {:pos 0
                     :score 0
                     :time 10000
                     :status (vec (take num-letters (repeat :init)))})

(def app-state (atom starting-state))


(defn reset-state! []
  (reset! app-state starting-state))

(defn cur-letter-id []
  (:pos @app-state))

(defn change-letter-status [letter-id status]
  (swap! app-state update-in [:status letter-id] (fn [_] status)))

(defn jump-next-letter []
  (let [pos (cur-letter-id)
        avail-pos (map first 
                       (filter #(or (= (second %) :init)
                                    (= (second %) :pass))
                               (map-indexed vector (:status @app-state))))
        first-greater-pos (first (filter #(> % pos) avail-pos))
        first-pos (first avail-pos)]
    (swap! app-state update-in [:pos] #(or first-greater-pos first-pos))))


(defn no-more-letters-to-answer? []
  (every? #(or (= :failed %) (= :ok %)) (:status @app-state)))

(defn no-more-time-left? []
  (> 1 (:time @app-state)))

(defn end-game? []
  (or (no-more-letters-to-answer?) (no-more-time-left?)))


(defn score []
  (count (filter #(= :ok %) (:status @app-state)))) 

(defn update-score []
  (swap! app-state update-in [:score] score))

(defn update-time []
  (swap! app-state update-in [:time] dec))

(defn get-letter-status [pos]
  (get-in @app-state [:status pos]))

(defn get-score []
  (:score @app-state))

(defn get-time []
  (:time @app-state))
