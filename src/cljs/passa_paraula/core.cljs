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

(def cur-letter-id (atom 0))

(def status-colors {:ok "green"
                    :failed "red"
                    :pass "yellow"
                    :init "blue"})

;; -------------------------
;; Views


(defn letter-circle-component
  [x y pos letter]
  [:g {:transform (str "translate(" x "," y ")")}
   ^{:key pos} [:circle {:id pos :r circle-radius :stroke "black" :stroke-witdth "3" :fill (:init status-colors)}]
   [:text {:dx "-5" :dy "5"} letter]])


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
  [:div [:h2 "Welcome to passa-paraula"]
   [board-component]
   [:div [:a {:href "#/about"} "go to about page"]]])


(defn about-page []
  [:div [:h2 "About passa-paraula"]
   [:div [:a {:href "#/"} "go to the home page"]]])


(defn current-page []
  [:div [(session/get :current-page)]])

;; -------------------------
;; Routes
(secretary/set-config! :prefix "#")

(secretary/defroute "/" []
  (session/put! :current-page #'home-page))

(secretary/defroute "/about" []
  (session/put! :current-page #'about-page))

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
  (let [color (get status-colors status)
        letter (.getElementById js/document @cur-letter-id)]
    (set! (.-fill (.-style letter)) color)))

(defn jump-next-letter []
  (swap! cur-letter-id #(mod (inc %) num-letters)))


(defn handle-letter [letter-id status]
  (do
    (change-letter-status letter-id status)
    (jump-next-letter)))

(defn handle-keys [event]
  (when-let [key (.-charCode event)]
    (case key
      111 (handle-letter @cur-letter-id :ok)
      112 (handle-letter @cur-letter-id :pass)
      107 (handle-letter @cur-letter-id :failed)
      (.log js/console (str "key pressed not valid" key)))))

(defn hook-keyboard-listener 
  []
   (.addEventListener js/window "keypress" handle-keys))

;; -------------------------
;; Initialize app
(defn mount-root []
  (reagent/render [current-page] (.getElementById js/document "app")))

(defn init! []
  (hook-browser-navigation!)
  (hook-keyboard-listener)
  (mount-root))
