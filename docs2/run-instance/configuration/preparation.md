# Preparation settings

| key                      | type   | description | example value                  |
|--------------------------|--------|-------------|--------------------------------| 
| min_network_size         | number |             | `200`                          |
| min_one_way_network_size | number |             | `200`                          |
| methods                  | object |             | [methods](#preparationmethods) |     

## **preparation.methods**

| key  | type   | description                                        | example value                   |
|------|--------|----------------------------------------------------|---------------------------------| 
| ch   | object | Settings for preprocessing contraction hierarchies | [ch](#preparationmethodsch)     |
| lm   | object | Settings for preprocessing landmarks               | [lm](#preparationmethodslm)     |
| core | object | Settings for preprocessing landmarks               | [core](#preparationmethodscore) |

## **preparation.methods.ch**

| key        | type    | description | example value   |
|------------|---------|-------------|-----------------| 
| enabled    | boolean |             | `true`          |
| threads    | number  |             | `1`             |
| weightings | string  |             | `"recommended"` |

## **preparation.methods.lm**

| key        | type    | description                                                                                                               | example value            |
|------------|---------|---------------------------------------------------------------------------------------------------------------------------|--------------------------| 
| enabled    | boolean |                                                                                                                           | `true`                   |
| threads    | number  |                                                                                                                           | `1`                      |
| weightings | string  |                                                                                                                           | `"recommended,shortest"` |
| landmarks  | number  | Total number of precomputed landmarks, the subset used during the query is set in `execution.methods.lm.active_landmarks` | `16`                     |

## **preparation.methods.core**

| key        | type    | description                                                                                                               | example value                                                 |
|------------|---------|---------------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------| 
| enabled    | boolean |                                                                                                                           | `true`                                                        |
| threads    | number  |                                                                                                                           | `1`                                                           |
| weightings | string  |                                                                                                                           | `"recommended,shortest"`                                      |
| landmarks  | number  | Total number of precomputed landmarks, the subset used during the query is set in `execution.methods.lm.active_landmarks` | `32`                                                          |
| lmsets     | string  |                                                                                                                           | `"highways,tollways;highways;tollways;country_193;allow_all"` |
