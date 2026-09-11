(ns general-sales.governor
  "GeneralSalesGovernor — the independent safety/traceability layer
  for the ISCO-08 5249 independent general-sales actor. Wired as its
  own `:govern` node in `general-sales.actor`'s StateGraph,
  downstream of `:advise` — the Advisor has no notion of sale
  provenance or discount/regulated-product risk, so this MUST be a
  separate system able to reject a proposal (itonami actor pattern,
  per ADR-2607011000 / CLAUDE.md Actors section).

  `check` is a pure function of (request, context, proposal, store) ->
  verdict; it never mutates the store. The StateGraph's `:decide` node
  routes on the verdict:
    :hard? true                → :hold  (irreversible, no write)
    :escalate? true            → :request-approval (interrupt-before)
    otherwise                  → :commit

  HARD invariants (:hard? true, ALWAYS :hold, never overridable):
    1. sale provenance     — the request's sale must be registered.
    2. no-actuation        — proposal :effect must be :propose.
  ESCALATION invariants (:escalate? true, ALWAYS human sign-off, per the
  README robotics-premise: offering a deep discretionary discount, or
  handling a regulated-product sale, always require human sign-off):
    3. :op :deep-discretionary-discount.
    4. :op :regulated-product-sale.
    5. low confidence (< `confidence-floor`)."
  (:require [general-sales.store :as store]))

(def confidence-floor 0.6)
(def ^:private escalating-ops #{:deep-discretionary-discount :regulated-product-sale})

(defn- hard-violations [{:keys [proposal]} sale-record]
  (cond-> []
    (nil? sale-record)
    (conj {:rule :no-sale :detail "未登録 sale"})

    (not= :propose (:effect proposal))
    (conj {:rule :no-actuation :detail "effect は :propose のみ許可（直接書込禁止）"})))

(defn check
  "Assess a proposal against `request`/`context`/`proposal` and a
  `store` implementing `general-sales.store/Store`. Returns
  `{:ok? bool :violations [...] :confidence n :hard? bool :escalate? bool}`."
  [request context proposal store]
  (let [sale-record (store/sale store (:sale-id request))
        hard (hard-violations {:proposal proposal} sale-record)
        hard? (boolean (seq hard))
        conf (or (:confidence proposal) 0.0)
        low? (< conf confidence-floor)
        risky-op? (contains? escalating-ops (:op proposal))]
    {:ok? (and (not hard?) (not low?) (not risky-op?))
     :violations hard
     :confidence conf
     :hard? hard?
     :escalate? (and (not hard?) (or low? risky-op?))}))
