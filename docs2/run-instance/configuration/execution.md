# Execution settings

| key     | type   | description | example value                |
|---------|--------|-------------|------------------------------| 
| methods | object |             | [methods](#executionmethods) |

## **execution.methods**

| key  | type   | description                                           | example value                 |
|------|--------|-------------------------------------------------------|-------------------------------| 
| ch   | object | Settings for using contraction hierarchies in routing | [ch](#executionmethodsch)     |
| lm   | object | Settings for using landmarks in routing               | [lm](#executionmethodslm)     |
| core | object | Settings for using landmarks in routing               | [core](#executionmethodscore) |

## **execution.methods.ch**

| key               | type    | description | example value |
|-------------------|---------|-------------|---------------| 
| disabling_allowed | boolean |             | `true`        |

## **execution.methods.lm**

| key               | type    | description                                      | default value |
|-------------------|---------|--------------------------------------------------|---------------| 
| disabling_allowed | boolean |                                                  | `true`        |
| active_landmarks  | number  | Number of landmarks used for computing the route | `8`           |

## **execution.methods.core**

| key               | type    | description                                      | example value |
|-------------------|---------|--------------------------------------------------|---------------| 
| disabling_allowed | boolean |                                                  | `true`        |
| active_landmarks  | number  | Number of landmarks used for computing the route | `6`           |
