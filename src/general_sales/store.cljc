(ns general-sales.store
  "SSoT for the ISCO-08 5249 independent general-sales sole-
  proprietor actor. Store is a protocol injected into the
  `general-sales.actor` StateGraph — `MemStore` is the default,
  deterministic, zero-dep backend; a Datomic/kotoba-server-backed
  implementation can be swapped in without touching the actor or
  governor (itonami actor pattern, per ADR-2607011000 / CLAUDE.md
  Actors section).

  Domain:

    sale     — a registered sale (:sale-id, :name)
    record   — a committed operating record under a sale (sell step,
               fulfill entry, deep discretionary discount, regulated-
               product sale) — written ONLY via commit-record!, never
               mutated in place
    ledger   — an append-only audit trail of every proposal/verdict/
               disposition, regardless of outcome (commit or hold)")

(defprotocol Store
  (sale [s sale-id])
  (records-of [s sale-id])
  (ledger [s])
  (register-sale! [s sale])
  (commit-record! [s record])
  (append-ledger! [s fact]))

(defrecord MemStore [a]
  Store
  (sale [_ sale-id] (get-in @a [:sales sale-id]))
  (records-of [_ sale-id] (filter #(= sale-id (:sale-id %)) (:records @a)))
  (ledger [_] (:ledger @a))
  (register-sale! [s sale]
    (swap! a assoc-in [:sales (:sale-id sale)] sale) s)
  (commit-record! [s record]
    (swap! a update :records (fnil conj []) record) s)
  (append-ledger! [s fact]
    (swap! a update :ledger (fnil conj []) fact) s))

(defn mem-store
  ([] (mem-store {}))
  ([seed] (->MemStore (atom (merge {:sales {} :records [] :ledger []} seed)))))
