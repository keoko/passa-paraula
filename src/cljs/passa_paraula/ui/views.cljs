(ns passa-paraula.ui.views
  (:require [passa-paraula.game :as game]))

(def center-x 500)
(def center-y 500)
(def radius 400)
(def circle-radius "100px")

(def status-colors {:ok "green"
                    :failed "red"
                    :pass "orange"
                    :init "blue"}) 


(def letter-color "black")
(def letter-width "1")
(def highlight-letter-color "orange")
(def highlight-letter-width "5")
  


(defn svg-letter-circle-component
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
             :fill (get status-colors (game/get-letter-status pos)) }]
   [:Text {:dx "-5" :dy "5"} letter]])


(defn get-letter-color [pos]
  (get status-colors (game/get-letter-status pos)))

(defn letter-circle-component [x y pos letter]
  ^{:key pos} [:div {:id pos
                     :style {:position "absolute"
                             :top y
                             :left x
                             :width circle-radius
                             :height circle-radius
                             :border-radius "50%"
                             :font-size "50px"
                             :color "#fff"
                             :line-height "100px"
                             :text-align "center"
                             :background (get-letter-color pos)}} 
               letter])



(defn build-circles []
  (let [get-circle (fn [x] {
                            :x (Math/round (+ center-x (* radius  (Math/cos (-  (/ (* Math/PI 2 x) game/num-letters) (/ Math/PI 2))))))
                            :y (Math/round (+ center-y (* radius  (Math/sin (-  (/ (* Math/PI 2 x) game/num-letters) (/ Math/PI 2))))))
                            :pos x
                            :letter (get game/letters x)})]
       (map get-circle (range game/num-letters))))



(defn board-component []
  [:div
   (for [circle (build-circles)]
     [letter-circle-component (:x circle) (:y circle) (:pos circle) (:letter circle)])])


(defn svg-board-component []
  [:svg {:height "500" :width "500"}
   (for [circle (build-circles)]
     [svg-letter-circle-component (:x circle) (:y circle) (:pos circle) (:letter circle)])])

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
