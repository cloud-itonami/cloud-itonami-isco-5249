(ns general-sales.governor-test
  (:require [clojure.test :refer [deftest is testing]]
            [general-sales.store :as store]
            [general-sales.governor :as governor]))

(defn- fresh-store []
  (let [st (store/mem-store)]
    (store/register-sale! st {:sale-id "sale-1" :name "Floor Display Sale"})
    st))

(deftest ok-on-clean-sell
  (let [st (fresh-store)
        proposal {:op :sell :effect :propose :confidence 0.9 :stake :low}
        v (governor/check {:sale-id "sale-1"} {} proposal st)]
    (is (:ok? v))
    (is (not (:hard? v)))
    (is (not (:escalate? v)))))

(deftest hard-on-unregistered-sale
  (let [st (fresh-store)
        proposal {:op :sell :effect :propose :confidence 0.9 :stake :low}
        v (governor/check {:sale-id "no-such-sale"} {} proposal st)]
    (is (:hard? v))
    (is (some #(= :no-sale (:rule %)) (:violations v)))))

(deftest hard-on-no-actuation-violation
  (let [st (fresh-store)
        proposal {:op :sell :effect :direct-write :confidence 0.9 :stake :low}
        v (governor/check {:sale-id "sale-1"} {} proposal st)]
    (is (:hard? v))
    (is (some #(= :no-actuation (:rule %)) (:violations v)))))

(deftest escalates-on-deep-discretionary-discount
  (let [st (fresh-store)
        proposal {:op :deep-discretionary-discount :effect :propose :confidence 0.9 :stake :high}
        v (governor/check {:sale-id "sale-1"} {} proposal st)]
    (is (:escalate? v))
    (is (not (:hard? v)))))

(deftest escalates-on-regulated-product-sale
  (let [st (fresh-store)
        proposal {:op :regulated-product-sale :effect :propose :confidence 0.9 :stake :high}
        v (governor/check {:sale-id "sale-1"} {} proposal st)]
    (is (:escalate? v))
    (is (not (:hard? v)))))

(deftest escalates-on-low-confidence
  (let [st (fresh-store)
        proposal {:op :sell :effect :propose :confidence 0.2 :stake :low}
        v (governor/check {:sale-id "sale-1"} {} proposal st)]
    (is (:escalate? v))
    (is (not (:hard? v)))))

(deftest store-records-and-ledger-append-only
  (let [st (fresh-store)]
    (store/commit-record! st {:sale-id "sale-1" :op :fulfill})
    (store/append-ledger! st {:disposition :commit})
    (is (= 1 (count (store/records-of st "sale-1"))))
    (is (= 1 (count (store/ledger st))))))
