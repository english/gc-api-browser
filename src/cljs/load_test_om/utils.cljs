(ns load-test-om.utils)

(defn max-min-difference [f coll]
  (- (apply max (map f coll))
     (apply min (map f coll))))

; TODO: return a tuple
(defn hit-rate [bucket]
  {:x (if (= (count bucket) 0)
        0
        (Math/round (/ (apply + (map :time bucket))
                       (count bucket))))
   :y (count bucket)})

(defn bucket-into-seconds [data-points]
  (let [v (reduce (fn [{:keys [bucket coll] :as acc} item]
                    (let [elapsed-time (max-min-difference :time (conj bucket item))]
                      (if (> elapsed-time 1000)
                        {:bucket [item]             :coll (conj coll bucket)}
                        {:bucket (conj bucket item) :coll coll})))
                  {:bucket [] :coll []}
                  data-points)]
    (conj (:coll v) (:bucket v))))

(defn avg-hit-rate [data-points]
  (let [bucketed (bucket-into-seconds data-points)
        total (apply + (map (comp :y hit-rate) bucketed))]
    (Math/round (/ total (count bucketed)))))
