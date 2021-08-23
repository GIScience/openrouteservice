---
grand_parent: Documentation
parent: Travel Speeds
nav_order: 1
title: Waytype Speeds
---

# Waytype Speeds
The waytypes correspond to the OSM [highway](https://wiki.openstreetmap.org/wiki/Key:highway) tag value.
This table aggregates the values in the [speed value files][svf].

_(all Values in km/h)_

  | Waytype \ Profile -> | driving-car | driving-hgv | cycling-regular |
  |:--------------------:|:-----------:|:-----------:|:---------------:|
  |       motorway       |     100     |      85     |        -        |
  |     motorway_link    |      60     |      50     |        -        |
  |       motorroad      |      90     |      80     |        -        |
  |         trunk        |      85     |      60     |        18       |
  |      trunk_link      |      60     |      50     |        18       |
  |        primary       |      65     |      60     |        18       |
  |     primary_link     |      50     |      50     |        18       |
  |       secondary      |      60     |      60     |        18       |
  |    secondary_link    |      50     |      50     |        18       |
  |       tertiary       |      50     |      50     |        18       |
  |     tertiary_link    |      40     |      40     |        18       |
  |     unclassified     |      30     |      30     |        16       |
  |      residential     |      30     |      30     |        18       |
  |     living_street    |      10     |      10     |        6        |
  |        service       |      20     |      20     |        14       |
  |         road         |      20     |      20     |        12       |
  |         track        |      15     |      15     |        12       |
  |         path         |      -      |      -      |        12       |
  |        footway       |      -      |      -      |        6        |
  |      pedestrian      |      -      |      -      |        6        |
  |       cycleway       |      -      |      -      |        18       |

[svf]: https://github.com/GIScience/openrouteservice/tree/master/openrouteservice/src/main/resources/resources/services/routing/speed_limits
