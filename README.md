# cloud-itonami-isco-5249

Open Occupation Blueprint for **ISCO-08 5249**: Sales Workers Not Elsewhere Classified.

This repository designs a forkable OSS business for an independent sales worker: a sales-floor-assist robot performs product retrieval and display restocking under a governor-gated actor, so the practice keeps its own sales and fulfillment records instead of renting a closed retail-sales SaaS.

## Robotics premise

All cloud-itonami verticals are designed on the premise that a **robot performs
the physical domain work**. Here a sales-floor-assist robot performs product retrieval, display restocking and receipt handling under an actor that proposes
actions and an independent **General Sales Governor** that gates them. The governor never
dispatches hardware itself; `:high`/`:safety-critical` actions (such as
offering a deep discretionary discount, or handling a regulated-product sale) require human sign-off.

A live sample of the operator console (robotics safety console, shared template) is rendered in [docs/samples/operator-console.html](docs/samples/operator-console.html) — pure-data HTML output of `kotoba.robotics.ui`.

## Core Contract

```text
sales scope + pricing rules + product-handling policy
        |
        v
Sales Advisor -> General Sales Governor -> sell/fulfill, or human sign-off
        |
        v
robot actions (gated) + operating records + audit ledger
```

No automated advice can dispatch a robot action the governor refuses, suppress
an operating record, or disclose sensitive data without governor approval and
audit evidence.

## Capability layer

Resolves via [`kotoba-lang/occupation`](https://github.com/kotoba-lang/occupation)
(ISCO-08 `5249`). Required capabilities:

- :robotics
- :forms
- :identity
- :audit-ledger
- :bpmn

See [`docs/business-model.md`](docs/business-model.md) and
[`docs/operator-guide.md`](docs/operator-guide.md).

## License

AGPL-3.0-or-later.
