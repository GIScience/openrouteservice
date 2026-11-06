---
title: Hosted API Restrictions
---

# Hosted API Restrictions

To provide services for thousands of users we have to restrict queries in our API to ensure our services stay stable for everyone.
When you are using our API the following restrictions apply to the respective endpoints.
If you want to make queries beyond these limits you should look into setting up your own customized [openrouteservice backend](./run-instance).

## Directions restrictions
| Modes              | Maximum distance | Maximum distance with profile restrictions |
|--------------------|------------------|--------------------------------------------|
| Foot profiles      | 6.000 km         | 6,000 km                                   |
| Cycling profiles   | 6.000 km         | 6.000 km                                   |
| Driving profiles   | 6.000 km         | 6.000 km                                   |
| Wheelchair profile | 6.000 km         | 300 km                                     |

| Option                                 | Maximum |
|----------------------------------------|---------|
| Distance (with avoid areas)            | 150 km  |
| Distance (alternative & round trip)    | 100 km  |
| Alternative route count                | 3       |
| Avoid polygon area                     | 200 km² |
| Avoid polygon extent (height or width) | 20 km   |
| Route waypoints                        | 50      |

## Isochrones restrictions
| Option                        | Maximum |
|-------------------------------|---------|
| Locations                     | 5       |
| Intervals                     | 10      |
| Range distance                | 120 km  |
| Range time (Foot profiles)    | 20 h    |
| Range time (Cycling profiles) | 5 h     |
| Range time (Driving profiles) | 1 h     |

## Matrix restrictions
| Option                             | Maximum                          |
|------------------------------------|----------------------------------|
| Locations (origin x destination)   | 3.500 (e.g. 50 x 50) per request |
| Locations (with dynamic arguments) | 25 (e.g. 5 x 5) per request      |

## Snap restrictions
| Option    | Maximum           |
|-----------|-------------------|
| Locations | 5.000 per request |

## Export restrictions
| Option            | Standard Plan | Collaborative Plan |
|-------------------|---------------|--------------------|
| Maximum bbox area | 10km²         | 50km²              |

## POIs restrictions
| Option                    | Maximum |
|---------------------------|---------|
| Maximum Area              | 50 km²  |
| Maximum Linestring length | 500 km  |
| Maximum search radius     | 2 km    |

## Elevation restrictions
| Option        | Maximum |
|---------------|---------|
| Vertex amount | 2000    |

## Optimization restrictions

| Option   | Maximum |
|----------|---------|
| Routes   | 50      |
| Vehicles | 3       |
