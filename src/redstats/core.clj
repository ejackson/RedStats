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

(defn serial-mean-aggregation
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
         serial-mean-aggregation
         serial-initial-value
         coll)]

    ;; empty list or nil will break this function for now.
    (/ final-sum final-count)
    ))

;; Now, dealing with the calculating the mean using the new
;; reducers...



;; There needs to be a combiner function that will be called with no
;; args, but it *may* also be called to combine two reduced sub-lists.
;; If there are sub-lists created, it *will* be called.
(defn par-combiner
  ;; The 0-arity version should be equivalent to
  ;; 'serial-initial-value' above
  ([] [0 0])

  ;; The 2-arity version will combine two completed sub-lists: in this
  ;; case it will need to add the sums and also add the counts
  ([[s1 c1] [s2 c2]]

     [(+ s1 s2) (+ c1 c2)]))

;; We can still use the previous serial version of the mean
;; aggregation: it will work in the same way.
(def par-mean-aggregation serial-mean-aggregation)

(defn par-mean [n coll]
  (let [[final-sum final-count]
        (r/fold
         n
         par-combiner
         par-mean-aggregation
         coll)]

    ;; again, empty list or nil will break this function for now.
    (/ final-sum final-count)
    ))
