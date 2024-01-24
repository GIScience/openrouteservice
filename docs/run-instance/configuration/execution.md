# Execution settings

| key     | type   | description | example value                |
|---------|--------|-------------|------------------------------| 
| methods | object |             | [methods](#execution-methods) |

## **execution.methods**

| key  | type   | description                                           | example value                 |
|------|--------|-------------------------------------------------------|-------------------------------| 
| ch   | object | Settings for using contraction hierarchies in routing | [ch](#execution-methods-ch)     |
| lm   | object | Settings for using landmarks in routing               | [lm](#execution-methods-lm)     |
| core | object | Settings for using landmarks in routing               | [core](#execution-methods-core) |

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
