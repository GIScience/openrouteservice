# `ors.cors`

Properties concerning cross-origin resource sharing:

| key               | type                          | description                                                                        | default value                                                                                                                  |
|-------------------|-------------------------------|------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------|
| allowed_origins   | string / comma separated list | Configures the Access-Control-Allow-Origins CORS header. `*` for all origins       | `*`                                                                                                                            |
| allowed_headers   | string / comma separated list | Configures the Access-Control-Allow-Headers CORS header. `*` for all headers     s | `Content-Type, X-Requested-With, accept, Origin, Access-Control-Request-Method, Access-Control-Request-Headers, Authorization` |
| preflight_max_age | int                           | Duration in seconds. Specifies how long the OPTIONS response is cached by browsers | `600`                                                                                                                          |
