(ns passa-paraula.core
    (:require [reagent.core :as reagent :refer [atom]]
              [reagent.session :as session]
              [secretary.core :as secretary :include-macros true]
              [goog.events :as events]
              [goog.history.EventType :as EventType])
    (:import goog.History))

 

;; -------------------------
;; Views
(def center-x 150)
(def center-y 150)
(def radius 100)
(def num-circles 25)
(def circle-radius 15)


;; -------------------------
;; Views


(defn letter-circle-component
  [x y letter]
  [:g {:transform (str "translate(" x "," y ")")}
   [:circle {:r circle-radius :stroke "black" :stroke-witdth "3" :fill "red"}]
   [:text {:dx "-1"} letter]])


(defn build-circles
  []
  (let [get-circle (fn [x] {
                            :x (Math/round (+ center-x (* radius (Math/cos (/ (* Math/PI 2 x) num-circles)))))
                            :y (Math/round (+ center-y (* radius (Math/sin (/ (* Math/PI 2 x) num-circles)))))
                            :letter (char (+ 65 x))})]
       (map get-circle (range num-circles))))

(defn board-component
  []
  [:svg {:height "500" :width "500"}
   (for [circle (build-circles)]
     [letter-circle-component (:x circle) (:y circle) (:letter circle)])])

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

;; -------------------------
;; Initialize app
(defn mount-root []
  (reagent/render [current-page] (.getElementById js/document "app")))

(defn init! []
  (hook-browser-navigation!)
  (mount-root))
