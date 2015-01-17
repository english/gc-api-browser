(ns load-test-om.utils)

(defn max-min-difference [f coll]
  (let [new-coll (map f coll)]
    (- (apply max new-coll)
       (apply min new-coll))))

(defn mean [xs]
  {:pre [(seq xs)]}
  (/ (apply + xs)
     (count xs)))

(defn hit-rate [bucket]
  {:x (if (= (count bucket) 0)
        0
        (-> (map :time bucket) mean Math/round))
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
  {:pre [(seq data-points)
         (contains? (first data-points) :time)]}
  (let [bucketed (bucket-into-seconds data-points)]
    (->> (bucket-into-seconds data-points)
         (map (comp :y hit-rate))
         mean
         Math/round)))
