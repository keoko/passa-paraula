(ns passa-paraula.game
  (:require  [reagent.core :as reagent :refer [atom]]))

(def default-letters [\A \B \C \D \E \F \G \H \I \J \K \L \M \N \O \P \Q \R \S \T \U \V \W \X \Y \Z])

(def default-team-name "passa-paraula")
(def default-team-color "black")

(defn init-letters-status [letters]
  (vec (take (count letters) (repeat :init))))

(def default-state {:pos 0
                    :score 0
                    :state :start
                    :time (* 60 60) ; 1 hour
                    :team-name default-team-name
                    :team-color default-team-color
                    :letters default-letters
                    :status (init-letters-status default-letters)})

(def app-state (atom default-state))

(defn reset-state! []
  (reset! app-state default-state))

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
  (when (= :run (:state @app-state))
    (swap! app-state update-in [:time] dec)))

(defn get-letter-status [pos]
  (get-in @app-state [:status pos]))

(defn get-letters []
  (:letters @app-state))

(defn num-letters []
  (count (get-letters)))

(defn get-score []
  (:score @app-state))

(defn get-time []
  (:time @app-state))

(defn get-team-color []
  (:team-color @app-state))

(defn get-team-name []
  (:team-name @app-state))

(defn game-paused? []
  (= :pause (:state @app-state)))

(defn game-ended? []
  (= :end (:state @app-state)))

(defn game-in-run? []
  (= :run (:state @app-state)))

(defn game-in-start? []
  (= :start (:state @app-state)))

(defn game-in-pause? []
  (= :pause (:state @app-state)))

(defn end-game []
  (swap! app-state update-in [:state] (fn [_] :end)))

(defn pause-game []
  (swap! app-state update-in [:state] (fn [_] :pause)))

(defn get-state []
  (:state @app-state))

(defn get-app-state []
  app-state)

(defn handle-letter [letter-id status]
  (when (game-in-run?)
    (change-letter-status letter-id status)
    (update-score)
    (jump-next-letter)
    (when (end-game?)
      (end-game))))

(defn toggle-status []
  (let [next-status {:pause :run
                     :run :pause
                     :start :run
                     :end :start}
        toggle-it (fn [ts] (get next-status ts))]
    (swap! app-state update-in [:state] toggle-it)
    (when (game-in-start?)
      (reset-state!))))
