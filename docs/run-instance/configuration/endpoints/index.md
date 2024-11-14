# `ors.endpoints`

Settings beneath `ors.endpoints` are required at runtime to process API requests.
See [endpoints reference](/api-reference/endpoints/) for usage details for the available endpoints.

More parameters relevant at query time that are specific to the
queried [profile](/run-instance/configuration/engine/profiles/index.md) can be found in
the [ors.engine.profiles.\<profile\>.service](/run-instance/configuration/engine/profiles/service.md) section.

| key        | type   | description                                                                                                                    | 
|------------|--------|--------------------------------------------------------------------------------------------------------------------------------|
| defaults   | object | [Parameter defaults](defaults.md) relevant for all endpoints                                                                   | 
| routing    | object | [Parameters for the routing endpoint](routing.md) (also called [Directions Endpoint](/api-reference/endpoints/directions/))    | 
| isochrones | object | [Parameters for the isochrones endpoint](isochrones.md) (see also [Isochrones Endpoint](/api-reference/endpoints/isochrones/)) | 
| matrix     | object | [Parameters for the matrix endpoint](matrix.md) (see also [Matrix endpoint](/api-reference/endpoints/matrix/))                 | 
| snap       | object | [Parameters for the snapping endpoint](snap.md) (see also [Snapping endpoint](/api-reference/endpoints/snapping/))             | 