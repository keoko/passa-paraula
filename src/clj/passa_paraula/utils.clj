(ns passa-paraula.utils
  (:gen-class))


(defn foo
  []
  (println "test!!!"))

(foo)

(def center-x 250)
(def center-y 500)
(def radius 100)
(def num-circles 25)
(def circle-radius 15)


(defn int-to-letter
  [n]
  (char (+ 65 n)))

(defn to-svg
  [n]
  (let [t (quot n num-circles)
        letter (int-to-letter n)
        x (Math/round (+ center-x (* radius (Math/cos (/ (* Math/PI 2 n) num-circles)))))
        y (Math/round (+ center-y (* radius (Math/sin (/ (* Math/PI 2 n) num-circles)))))]
    (format "<g transform='translate(%d,%d)'><circle r=%d stroke='black' stroke-width='3' fill='red'></circle><text dx='-1'>%s</text></g>" x y circle-radius letter)))

(def body (clojure.string/join  (map to-svg (range num-circles))))

(def html (format "<html><body><svg height='1000' width='1000'>%s</svg></body></html>" body))

(spit "/Users/icabrebarrera/dev/clojure/passa-paraula/index.html" html)
