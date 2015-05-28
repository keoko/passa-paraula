(ns passa-paraula.ui.views
  (:require [passa-paraula.game :as game]))

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
                          #_(secretary/dispatch! "/"))} 
          "play again!"]]])
