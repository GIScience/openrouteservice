---
grand_parent: Documentation
parent: Travel Speeds
nav_order: 4
title: Country Speeds
---

# Country-specific Speeds
As there are various traffic regulations in different countries, if no [maximum
speed](http://wiki.openstreetmap.org/wiki/Key:maxspeed) tag is given in
openstreetmap, we adjust the maximum speed according to the following key
values taken from [country specific speed
limits](http://wiki.openstreetmap.org/wiki/Speed_limits).
This table aggregates the values in the [speed value files][svf].

_(all Values in km/h)_

  |    Country     |         Tags         | driving-car | driving-hgv |
  |:--------------:|:--------------------:|:-----------:|:-----------:|
  |   Austria      |       AT:urban       |      50     |      50     |
  |                |       AT:rural       |     100     |      80     |
  |                |       AT:trunk       |     100     |      80     |
  |                |      AT:motorway     |     130     |      80     |
  | Switzerland    |       CH:urban       |      50     |      50     |
  |                |       CH:rural       |      80     |      80     |
  |                |       CH:trunk       |     100     |      80     |
  |                |      CH:motorway     |     120     |      80     |
  | Czech Republic |       CZ:urban       |      50     |      50     |
  |                |       CZ:rural       |      90     |      90     |
  |                |       CZ:trunk       |      80     |      80     |
  |                |      CZ:motorway     |      80     |      80     |
  |   Denmark      |       DK:urban       |      50     |      50     |
  |                |       DK:rural       |      80     |      80     |
  |                |      DK:motorway     |     130     |      80     |
  |   Germany      |   DE:living_street   |       7     |       7     |
  |                |       DE:urban       |      50     |      50     |
  |                |       DE:rural       |     100     |      80     |
  |                |      DE:motorway     |     130     |      80     |
  |   Finland      |       FI:urban       |      50     |      50     |
  |                |       FI:rural       |      80     |      80     |
  |                |       FI:trunk       |     100     |      80     |
  |                |      FI:motorway     |     120     |      80     |
  |   France       |       FR:urban       |      50     |      50     |
  |                |       FR:rural       |      80     |      80     |
  |                |       FR:trunk       |     110     |      80     |
  |                |      FR:motorway     |     130     |      80     |
  |   Greece       |       GR:urban       |      50     |      50     |
  |                |       GR:rural       |      90     |      80     |
  |                |       GR:trunk       |     110     |      80     |
  |                |      GR:motorway     |     130     |      80     |
  |   Hungary      |       HU:urban       |      50     |      50     |
  |                |       HU:rural       |      90     |      80     |
  |                |       HU:trunk       |     110     |      80     |
  |                |      HU:motorway     |     130     |      80     |
  |    Italy       |       IT:urban       |      50     |      50     |
  |                |       IT:rural       |      90     |      80     |
  |                |       IT:trunk       |     110     |      80     |
  |                |      IT:motorway     |     130     |      80     |
  |    Japan       |      JP:national     |      60     |      60     |
  |                |      JP:motorway     |     100     |      80     |
  |   Poland       |   PL:living_street   |      20     |      20     |
  |                |       PL:urban       |      50     |      50     |
  |                |       PL:rural       |      90     |      80     |
  |                |      PL:motorway     |     140     |      80     |
  |   Romania      |       RO:urban       |      50     |      50     |
  |                |       RO:rural       |      90     |      80     |
  |                |       RO:trunk       |     100     |      80     |
  |                |      RO:motorway     |     130     |      80     |
  | Russia         |   RU:living_street   |      20     |      20     |
  |                |       RU:rural       |      90     |      80     |
  |                |       RU:urban       |      60     |      60     |
  |                |      RU:motorway     |     110     |      80     |
  |  Slovakia      |       SK:urban       |      50     |      50     |
  |                |       SK:rural       |      90     |      80     |
  |                |       SK:trunk       |      90     |      80     |
  |                |      SK:motorway     |      90     |      80     |
  |  Slovenia      |       SI:urban       |      50     |      50     |
  |                |       SI:rural       |      90     |      80     |
  |                |       SI:trunk       |     110     |      80     |
  |                |      SI:motorway     |     130     |      80     |
  |    Spain       |       ES:urban       |      50     |      50     |
  |                |       ES:rural       |      90     |      80     |
  |                |       ES:trunk       |     100     |      80     |
  |                |      ES:motorway     |     120     |      80     |
  |   Sweden       |       SE:urban       |      50     |      50     |
  |                |       SE:rural       |      70     |      70     |
  |                |       SE:trunk       |      90     |      80     |
  |                |      SE:motorway     |     110     |      80     |
  | United Kingdom |     GB:nsl_single    |      95     |      90     |
  |                |      GB:nsl_dual     |     112     |      90     |
  |                |      GB:motorway     |     112     |      90     |
  | Ukraine        |       UA:urban       |      60     |      60     |
  |                |       UA:rural       |      90     |      80     |
  |                |       UA:trunk       |     110     |      80     |
  |                |      UA:motorway     |     130     |      80     |
  | Uzbekistan     |   UZ:living_street   |      30     |      30     |
  |                |       UZ:urban       |      70     |      70     |
  |                |       UZ:rural       |     100     |      90     |
  |                |      UZ:motorway     |     110     |      90     |

[svf]: https://github.com/GIScience/openrouteservice/tree/master/openrouteservice/src/main/resources/resources/services/routing/speed_limits
