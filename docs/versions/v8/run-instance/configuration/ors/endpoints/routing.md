
# `ors.endpoints.routing`

Settings for routing endpoint.

| key                          | type    | description                                                                            | default value                                                                    |
|------------------------------|---------|----------------------------------------------------------------------------------------|----------------------------------------------------------------------------------|
| enabled                      | boolean | Enables or disables the end-point                                                      | `true`                                                                           |
| attribution                  | string  | Attribution added to the response metadata                                             | `openrouteservice.org, OpenStreetMap contributors, tmc - BASt`                   |
| gpx_name                     | string  | Specifies the `name` tag that is returned in `metadata` of a gpx response              | `ORSRouting`                                                                     |
| gpx_description              | string  | Specifies the `desc` tag that is returned in `metadata` of a gpx response              | `This is a directions instructions file as GPX, generated from openrouteservice` |
| gpx_base_url                 | string  | Specifies the `link` tag that is returned in `metadata/author` of a gpx response       | `https://openrouteservice.org/`                                                  |
| gpx_support_mail             | string  | Specifies the `email` tag that is returned in `metadata/author` of a gpx response      | `support@openrouteservice.org`                                                   |
| gpx_author                   | string  | Specifies the `name` tag that is returned in `metadata/author` of a gpx response       | `openrouteservice`                                                               |
| gpx_content_licence          | string  | Specifies the `license` tag that is returned in `metadata/copyright` of a gpx response | `LGPL 3.0`                                                                       |
| maximum_avoid_polygon_area   | number  | The maximum allowed total area of a polygon in square kilometers, optional             | `200000000`                                                                      |
| maximum_avoid_polygon_extent | number  | The maximum extent (i.e. envelope side length) of a polygon in kilometers, optional    | `20000`                                                                          |
| maximum_alternative_routes   | number  | The maximum number of alternative routes in a request                                  | `3`                                                                              |
