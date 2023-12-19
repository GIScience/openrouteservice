# Properties in the `cors` block

| key                    | type                          | description                                                                        | default value                                                                                                                  |
|------------------------|-------------------------------|------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------|
| cors.allowed_origins   | string / comma separated list | Configures the Access-Control-Allow-Origins CORS header. `*` for all origins       | `*`                                                                                                                            |
| cors.allowed_headers   | string / comma separated list | Configures the Access-Control-Allow-Headers CORS header. `*` for all headers     s | `Content-Type, X-Requested-With, accept, Origin, Access-Control-Request-Method, Access-Control-Request-Headers, Authorization` |
| cors.preflight_max_age | int                           | Duration in seconds. Specifies how long the OPTIONS response is cached by browsers | `600`                                                                                                                          |
