(ns redstats.core
  (require [clojure.core.reducers :as r]))

;; Reducing over a collection to produce the mean would require two
;; bits of information: the running sum and the current population.
;;
;; Taking the purely serial route the function would probably look
;; like the following (for brevity, a vetcor will be used to hold the
;; data):

;; The initial value for will be zero (current sum) and zero (current
;; count)
(def serial-initial-value
  [0 0])

(defn mean-aggregation
  [[running-sum running-count] next-value]
  (let [s (+ running-sum next-value)
        c (inc running-count)]
    ;; Output another vector to be used for the next reduction...
    [s c]))

;; The final reduction will just be a couple in a vector: sum and
;; count.  It can then be
(defn serial-mean
  [coll]
  (let [[final-sum final-count]
        (reduce
         mean-aggregation
         serial-initial-value
         coll)]

    ;; empty list or nil will break this function for now.
    (/ final-sum final-count)))

;; Now, dealing with the calculating the mean using the new
;; reducers...

;; There needs to be a combiner function that will be called with no
;; args, but it *may* also be called to combine two reduced sub-lists.
;; If there are sub-lists created, it *will* be called.
(defn mean-combiner
  ;; The 0-arity version should be equivalent to
  ;; 'serial-initial-value' above
  ([] [0 0])

  ;; The 2-arity version will combine two completed sub-lists: in this
  ;; case it will need to add the sums and also add the counts
  ([[s1 c1] [s2 c2]]
     [(+ s1 s2) (+ c1 c2)]))

(defn par-mean [n coll]
  (let [[final-sum final-count]
        (r/fold
         n
         mean-combiner
         mean-aggregation
         coll)]

    ;; again, empty list or nil will break this function for now.
    (/ final-sum final-count)))

;; -----------------------------------------------------------------------------
;;  Variance
;;     We use Var(X) = E(X^2) - E(X)^2

;; So first deal with X^2 term.  Reducers can be combined, so simply put a X^2 term ahead of the sum terms in the exp reducer.
(defn- var-aggregation
  "I think composing this at the wrong level !  Reducers are supposed to compose as such, but I'm composing functions here - am I missing the point?"
  [coll x]
  (mean-aggregation coll (* x x)))

(defn- mean-sq [n coll]
  (let [[final-sum final-count]
        (r/fold
         n
         mean-combiner
         var-aggregation
         coll)]
    (/ final-sum final-count)))

(defn par-var
  "Calculate the variance.  Not currently done with X^2 and mean in parallel."
  [n coll]
  (let [mean    (par-mean n coll)
        mean-sq (mean-sq  n coll)]
    (- mean-sq (* mean mean))))

;;(par-var 1 [1 2 3 4])


;; -------------------
;; Sandpit
;; Could achievethe above by directly transforming mean-aggregation
(defn- pre-square
  "Transform a reducing fn such that the input is pre-squared"
  )


(comment
  (reduce * ((comp (r/filter even?) (r/map inc)) [1 2 3]))
  (reduce + (r/filter odd? [1 2 3]))
  (reduce + (x2 [1 2 3 4]))

  ;; --------------
  (def id (r/map identity))
  (reduce + (id [1 2 3]))

  (defn red-sum [coll]
    (r/fold + id [1 2 3 4]))
)
