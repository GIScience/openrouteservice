# Preparation settings

| key                      | type   | description                                                       | default value |
|--------------------------|--------|-------------------------------------------------------------------|---------------|
| min_network_size         | number | Minimum size (number of edges) of an independent routing subgraph | `200`         |
| min_one_way_network_size | number |                                                                   | `200`         |
| methods                  | object | [methods properties](#preparationmethods)                         |               |

## **preparation.methods**

| key  | type   | description                                            | example value                   |
|------|--------|--------------------------------------------------------|---------------------------------|
| ch   | object | Settings for preprocessing contraction hierarchies     | [ch](#preparationmethodsch)     |
| lm   | object | Settings for preprocessing A* with landmarks           | [lm](#preparationmethodslm)     |
| core | object | Settings for preprocessing core routing with landmarks | [core](#preparationmethodscore) |

## **preparation.methods.ch**

| key        | type    | description                                              | example value            |
|------------|---------|----------------------------------------------------------|--------------------------|
| enabled    | boolean | Enables or disables the routing algorithm                | `true`                   |
| threads    | number  | Number of parallel threads for computing the preparation | `1`                      |
| weightings | string  | Comma-separated list of weightings                       | `"recommended,shortest"` |

## **preparation.methods.lm**

| key        | type    | description                                                                                                               | default value            |
|------------|---------|---------------------------------------------------------------------------------------------------------------------------|--------------------------|
| enabled    | boolean | Enables or disables the routing algorithm                                                                                 | `true`                   |
| threads    | number  | Number of parallel threads for computing the preparation                                                                  | `1`                      |
| weightings | string  | Comma-separated list of weightings                                                                                        | `"recommended,shortest"` |
| landmarks  | number  | Total number of precomputed landmarks; the subset used during the query is set in `execution.methods.lm.active_landmarks` | `16`                     |

## **preparation.methods.core**

| key        | type    | description                                                                                                               | example value                                                 |
|------------|---------|---------------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------|
| enabled    | boolean | Enables or disables the routing algorithm                                                                                 | `true`                                                        |
| threads    | number  | Number of parallel threads for computing the preparation                                                                  | `1`                                                           |
| weightings | string  | Comma-separated list of weightings                                                                                        | `"recommended,shortest"`                                      |
| landmarks  | number  | Total number of precomputed landmarks, the subset used during the query is set in `execution.methods.lm.active_landmarks` | `32`                                                          |
| lmsets     | string  | Landmark sets tailored for specific avoid-filters enabled                                                                 | `"highways,tollways;highways;tollways;country_193;allow_all"` |
