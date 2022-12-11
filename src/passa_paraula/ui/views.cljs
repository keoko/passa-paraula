(ns passa-paraula.ui.views
  (:require [passa-paraula.game :as game]
            [reagent.core :as reagent :refer [atom]]
            [reagent.session :as session]
            [clojure.string :as str]
            [goog.string :as gstring]
            [goog.string.format]
            [secretary.core :as secretary :include-macros true]
            [accountant.core :as accountant]
            [reagent-forms.core :refer [bind-fields]]))

(def ui-state (atom {}))

(def status-colors {:ok "green"
                    :failed "red"
                    :pass "orange"
                    :init "black"})

(def cur-letter-color "purple")

(defn set-letter-color [pos]
  (if (= pos (game/cur-letter-id))
    cur-letter-color
    (get status-colors (game/get-letter-status pos))))

(defn letter-circle-component [pos degree]
  (let [letter (get (game/get-letters) pos)
        circle-radius-px (str (:circle-radius @ui-state) "px")
        board-radius (:radius @ui-state)]
    [:div.circle {:style {:transform (gstring/format "rotate(%sdeg) translate(%spx) rotate(-%sdeg)" degree board-radius degree)
                          :width circle-radius-px
                          :height circle-radius-px
                          :line-height circle-radius-px
                          :background (set-letter-color pos)}}
     letter]))

(defn board-component []
  [:div.container
   [:div.row
    (doall
     (for [pos (range (game/num-letters))]
       (let [step (/ 360 (game/num-letters))
             degree (+ 270 (* pos step))]
         (letter-circle-component pos degree))))]])

(defn format-time [t]
  (let [minutes (quot t 60)
        seconds (rem t 60)]
    (str
     (if (> 10 minutes) (str "0" minutes) minutes)
     ":"
     (if (> 10 seconds) (str "0" seconds) seconds))))

(defn start-component []
  [:span.glyphicon.glyphicon-play-circle
   {:aria-hidden "true"
    :on-click game/toggle-status}])

(defn end-component []
  [:span.glyphicon.glyphicon-repeat
   {:aria-hidden "true"
    :on-click game/toggle-status}])

(defn pause-component []
  [:span.glyphicon.glyphicon-pause
   {:aria-hidden "true"
    :on-click game/toggle-status}])

(defn run-component []
  [:div
   [:span.glyphicon.glyphicon-ok
    {:aria-hidden "true"
     :style {:color (:ok status-colors)}
     :on-click (fn [_] (game/handle-letter (game/cur-letter-id) :ok))}]
   [:span.glyphicon.glyphicon-remove
    {:aria-hidden "true"
     :style {:color (:failed status-colors)}
     :on-click (fn [_] (game/handle-letter (game/cur-letter-id) :failed))}]
   [:span.glyphicon.glyphicon-question-sign
    {:aria-hidden "true"
     :style {:color (:pass status-colors)}
     :on-click (fn [_] (game/handle-letter (game/cur-letter-id) :pass))}]])

(defn buttons-component []
  (let [buttons-by-state {:start (start-component)
                          :end (end-component)
                          :pause (pause-component)
                          :run (run-component)}
        buttons (get buttons-by-state (game/get-state))]
    [:div
     {:id "buttons"
      :style {:position :absolute
              :color (:init status-colors)
              :font-size (str (:circle-radius @ui-state) "px")
              :transform "translateX(-50%) translateY(-50%)"}}
     buttons]))

(defn score-component []
  [:span.navbar-text.pull-left {:id "score"}
   " score " [:span.badge (game/get-score)]])

(defn timer-component []
  [:span.navbar-text.pull-right {:id "time"}
   "timer " [:span.badge (format-time (game/get-time))]])

(defn preferences-button-component []
  [:a {:on-click #(secretary/dispatch! "/preferences")} "preferences"])

(defn navbar-component []
  [:nav.navbar.navbar-fixed-top.navbar-default
   {:style {:background-color (game/get-team-color)}}
   [:div.container
    [:div.navbar-header
     [:a.navbar-brand {:href "#"}
      (game/get-team-name)]]
    [:ul.nav.navbar-nav
     [:li [score-component]]]
    [:ul.nav.navbar-nav.navbar-right
     [:li [preferences-button-component]]
     [:li [timer-component]]]]])

(defn home-page []
  [:div.container
   [navbar-component]
   [buttons-component]
   [board-component]])

(defn row [label input]
  [:div.row
   [:div.col-md-2 [:label label]]
   [:div.col-md-5 input]])

(defn input [label type id]
  (row label [:input.form-control {:field type :id id}]))

(defn convert-letters [[id] value state]
  (when (= id :letters)
    (let [letters (str/split value #",")]
      (merge state {:letters letters
                    :status (game/init-letters-status letters)}))))

(def form-template
  [:div
   (input "team color" :text :team-color)
   (input "team name" :text :team-name)
   (input "time (seconds)" :text :time)
   (input "letters" :text :letters)])

(defn preferences-page []
  [:div
   [navbar-component]
   [:div.container
    [:h1.page-header "preferences"]
    [:div.jumbotron
     [bind-fields form-template (game/get-app-state) convert-letters]
     [:button.btn.btn-default
      {:on-click #(secretary/dispatch! "/")}
      "save"]]]])

(defn current-page []
  [:div [(session/get :current-page)]])

(defn init-current-page! []
  (session/put! :current-page #'home-page))

;; -------------------------
;; Routes

(secretary/defroute "/" []
  (session/put! :current-page #'home-page))

(secretary/defroute "/preferences" []
  (session/put! :current-page #'preferences-page))

(defn recalculate-window-center! []
  (let [width  (.-innerWidth js/window)
        height (.-innerHeight js/window)
        radius (if (< width height) width height)]
    (swap! ui-state merge {:circle-radius (/ radius 16)
                           :radius (/ radius 2.5)})))

(defn init! []
  (init-current-page!)
  (accountant/configure-navigation!
   {:nav-handler (fn [path]
                   (secretary/dispatch! path))})
  (accountant/dispatch-current!))
