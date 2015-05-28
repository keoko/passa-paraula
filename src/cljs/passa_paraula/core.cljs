(ns passa-paraula.core
    (:require [reagent.core :as reagent]
              [secretary.core :as secretary :include-macros true]
              [passa-paraula.game :as game]
              [secretary.core :as secretary :include-macros true]
            [reagent.session :as session]
            [goog.events :as events]
            [goog.history.EventType :as EventType])
    (:import goog.History))
;; -------------------------
;; config
(def center-x 150)
(def center-y 150)
(def radius 100)
(def circle-radius 12)

(def status-colors {:ok "green"
                    :failed "red"
                    :pass "yellow"
                    :init "blue"}) 


(def letter-color "black")
(def letter-width "1")
(def highlight-letter-color "orange")
(def highlight-letter-width "5")
  


;; -------------------------
;; Views


(defn letter-circle-component
  [x y pos letter]
  [:g {:transform (str "translate(" x "," y ")")}
   ^{:key pos} 
   [:circle {:id pos 
             :r circle-radius 
             :stroke (if (= pos (game/cur-letter-id)) 
                       highlight-letter-color
                       letter-color)
             :strokeWidth (if (= pos (game/cur-letter-id)) 
                             highlight-letter-width 
                             letter-width)
             :fill (get status-colors (game/get-letter-status pos))}]
   [:Text {:dx "-5" :dy "5"} letter]])


(defn build-circles
  []
  (let [get-circle (fn [x] {
                            :x (Math/round (+ center-x (* radius  (Math/cos (-  (/ (* Math/PI 2 x) game/num-letters) (/ Math/PI 2))))))
                            :y (Math/round (+ center-y (* radius  (Math/sin (-  (/ (* Math/PI 2 x) game/num-letters) (/ Math/PI 2))))))
                            :pos x
                            :letter (get game/letters x)})]
       (map get-circle (range game/num-letters))))


(defn board-component
  []
  [:svg {:height "500" :width "500"}
   (for [circle (build-circles)]
     [letter-circle-component (:x circle) (:y circle) (:pos circle) (:letter circle)])])


(defn home-page []
  [:div 
   [:h2 "Welcome to passa-paraula"]
   [:div {:id "score"} (str "score:" (game/get-score))]
   [:div {:id "time:"} (str "time:" (game/get-time))]
   [board-component]
   [:div [:a {:href "#/about"} "go to about page"]]])


(defn about-page []
  [:div [:h2 "About passa-paraula"]
   [:div [:a {:href "#/"} "go to the home page"]]])

(defn end-page []
  [:div [:h2 "The End"]
   [:div (str "score:" (game/get-score))]
   [:div [:a {:href "#/" 
              :on-click (fn [e] 
                          (game/reset-state!)
                          (secretary/dispatch! "/"))} 
          "play again!"]]])




(defn end-game []
  (secretary/dispatch! "/end"))


(defn handle-letter [letter-id status]
  (do
    (game/change-letter-status letter-id status)
    (game/update-score)
    (game/jump-next-letter)
    (when (game/end-game?)
      (end-game))))

(defn handle-keys [event]
  (when-let [key (.-charCode event)]
    (case key
      111 (handle-letter (game/cur-letter-id) :ok)
      112 (handle-letter (game/cur-letter-id) :pass)
      107 (handle-letter (game/cur-letter-id) :failed)
      (.log js/console (str "key pressed not valid" key)))))
 
(defn hook-keyboard-listener!
  []
   (.addEventListener js/window "keypress" handle-keys))


(defn tick []
  (game/update-time)
  (js/setTimeout tick 1000)
  (when (game/end-game?)
    (end-game)))

(defn hook-clock-update! []
  (js/setTimeout tick 1000))



;; -------------------------
;; Initialize app

(secretary/set-config! :prefix "#")

(defn current-page []
  [:div [(session/get :current-page)]])

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


(defn mount-root []
  (reagent/render [current-page] (.getElementById js/document "app")))



(defn init! []
  (game/reset-state!)
  (hook-browser-navigation!)
  (hook-keyboard-listener!)
  (hook-clock-update!)
  (mount-root))
