(ns passa-paraula.core
    (:require [reagent.core :as reagent :refer [atom]]
              [reagent.session :as session]
              [secretary.core :as secretary :include-macros true]
              [goog.events :as events]
              [goog.history.EventType :as EventType])
    (:import goog.History))

 

;; -------------------------
;; config
(def center-x 150)
(def center-y 150)
(def radius 100)
(def num-letters 25)
(def circle-radius 12)

(def status-colors {:ok "green"
                    :failed "red"
                    :pass "yellow"
                    :init "blue"})


(def letter-color "black")
(def letter-width "1")
(def highlight-letter-color "orange")
(def highlight-letter-width "5")


(def starting-state {:pos 0
                     :score 0
                     :status (vec (take num-letters (repeat :init)))})

(def app-state (atom starting-state))


(defn reset-state! []
  (reset! app-state starting-state))


;; -------------------------
;; Views


(defn cur-letter-id []
  (:pos @app-state))

(defn letter-circle-component
  [x y pos letter]
  [:g {:transform (str "translate(" x "," y ")")}
   ^{:key pos} 
   [:circle {:id pos 
             :r circle-radius 
             :stroke (if (= pos (cur-letter-id)) 
                       highlight-letter-color
                       letter-color)
             :strokeWidth (if (= pos (cur-letter-id)) 
                             highlight-letter-width 
                             letter-width)
             :fill (get status-colors (get-in @app-state [:status pos]))}]
   [:Text {:dx "-5" :dy "5"} letter]])


(defn build-circles
  []
  (let [get-circle (fn [x] {
                            :x (Math/round (+ center-x (* radius (Math/cos (/ (* Math/PI 2 x) num-letters)))))
                            :y (Math/round (+ center-y (* radius (Math/sin (/ (* Math/PI 2 x) num-letters)))))
                            :pos x
                            :letter (char (+ 65 x))})]
       (map get-circle (range num-letters))))


(defn board-component
  []
  [:svg {:height "500" :width "500"}
   (for [circle (build-circles)]
     [letter-circle-component (:x circle) (:y circle) (:pos circle) (:letter circle)])])


(defn home-page []
  [:div 
   [:h2 "Welcome to passa-paraula"]
   [:div {:id "score"} (str "score:" (:score @app-state))]
   [board-component]
   [:div [:a {:href "#/about"} "go to about page"]]])


(defn about-page []
  [:div [:h2 "About passa-paraula"]
   [:div [:a {:href "#/"} "go to the home page"]]])

(defn end-page []
  [:div [:h2 "The End"]
   [:div (str "score:" (:score @app-state))]
   [:div [:a {:href "#/" 
              :on-click (fn [e] 
                          (reset-state!)
                          (secretary/dispatch! "/"))} 
          "play again!"]]])

(defn current-page []
  [:div [(session/get :current-page)]])

;; -------------------------
;; Routes
(secretary/set-config! :prefix "#")

(secretary/defroute "/" []
  (session/put! :current-page #'home-page))

(secretary/defroute "/about" []
  (session/put! :current-page #'about-page))

(secretary/defroute "/end" []
  (session/put! :current-page #'end-page))

;; -------------------------
;; History
;; must be called after routes have been defined
(defn hook-browser-navigation! []
  (doto (History.)
    (events/listen
     EventType/NAVIGATE
     (fn [event]
       (secretary/dispatch! (.-token event))))
    (.setEnabled true)))


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


(defn end-game? []
  (every? #(or (= :failed %) (= :ok %)) (:status @app-state)))

(defn end-game []
  (secretary/dispatch! "/end"))


(defn score []
  (count (filter #(= :ok %) (:status @app-state)))) 

(defn update-score []
  (swap! app-state update-in [:score] score))

(defn handle-letter [letter-id status]
  (do
    (change-letter-status letter-id status)
    (update-score)
    (jump-next-letter)
    (when (end-game?)
      (end-game))))

(defn handle-keys [event]
  (when-let [key (.-charCode event)]
    (case key
      111 (handle-letter (cur-letter-id) :ok)
      112 (handle-letter (cur-letter-id) :pass)
      107 (handle-letter (cur-letter-id) :failed)
      (.log js/console (str "key pressed not valid" key)))))
 
(defn hook-keyboard-listener
  []
   (.addEventListener js/window "keypress" handle-keys))

;; -------------------------
;; Initialize app
(defn mount-root []
  (reagent/render [current-page] (.getElementById js/document "app")))


(defn init! []
  (reset-state!)
  (hook-browser-navigation!)
  (hook-keyboard-listener)
  (mount-root))
