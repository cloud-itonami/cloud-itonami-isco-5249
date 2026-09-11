(ns general-sales.actor-test
  (:require [clojure.test :refer [deftest is testing]]
            [general-sales.actor :as actor]
            [general-sales.store :as store]))

(defn- fresh-store []
  (let [st (store/mem-store)]
    (store/register-sale! st {:sale-id "sale-1" :name "Floor Display Sale"})
    st))

(deftest commits-a-clean-low-risk-request
  (let [st (fresh-store)
        graph (actor/build-graph {:store st})
        request {:sale-id "sale-1" :op :sell :stake :low}
        result (actor/run-request! graph request {} "thread-1")]
    (is (= :done (:status result)))
    (is (some? (get-in result [:state :record])))
    (is (= 1 (count (store/records-of st "sale-1"))))))

(deftest holds-on-unregistered-sale-without-committing
  (let [st (fresh-store)
        graph (actor/build-graph {:store st})
        request {:sale-id "no-such-sale" :op :sell :stake :low}
        result (actor/run-request! graph request {} "thread-2")]
    (is (= :done (:status result)))
    (is (nil? (get-in result [:state :record])))
    (is (empty? (store/records-of st "no-such-sale")))
    (is (= :hold (:disposition (:state result))))))

(deftest interrupts-then-commits-on-human-approval
  (let [st (fresh-store)
        graph (actor/build-graph {:store st})
        ;; deep discretionary discount always escalates (governor invariant)
        request {:sale-id "sale-1" :op :deep-discretionary-discount :stake :high}
        interrupted (actor/run-request! graph request {} "thread-3")]
    (is (= :interrupted (:status interrupted)))
    (is (empty? (store/records-of st "sale-1")))
    (let [resumed (actor/approve! graph "thread-3")]
      (is (= :done (:status resumed)))
      (is (some? (get-in resumed [:state :record])))
      (is (= 1 (count (store/records-of st "sale-1")))))))
