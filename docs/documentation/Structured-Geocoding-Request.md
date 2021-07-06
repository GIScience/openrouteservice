---
parent: Documentation
nav_order: 8
title: Structured Geocoding Query
---

# Structured Geocoding Query

A structured geocoding request is more precise than a normal one. It is also very useful for querying locations from tables.
For a structured request insert a JSON Object with at least on of the following parameters into the query parameter of the geocoding request:

  |   Parameter   | Description                                                                                                                                                                                              |
  |:-------------:|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
  |    address    | Can contain a full address with house number or only a street name                                                                                                                                       |
  | neighbourhood | Vernacular geographic entities that may not necessarily be official administrative divisions but are important nonetheless                                                                               |
  |    borough    | Mostly known in the context of New York City, even though they may exist in other cities, such as Mexico City                                                                                            |
  |    locality   | Name of a City                                                                                                                                                                                           |
  |     county    | Administrative division between localities and regions                                                                                                                                                   |
  |     region    | Normally the first-level administrative divisions within countries, analogous to states and provinces in the United States and Canada, respectively, though most other countries contain regions as well |
  |   postalcode  | A postalcode                                                                                                                                                                                             |
  |    country    | Name of a country. Supports two- and three-letter abbreviations                                                                                                                                          |

## Example

```json
{
  "address": "Berliner Straße 45",
  "locality": "Heidelberg",
  "country": "Germany",
  "postalcode": "69120"
}
```

## Uglyfied and encoded:

`%7B"address": "Berliner Straße 45","locality": "Heidelberg","country": "Germany","postalcode": "69120"%7D`
