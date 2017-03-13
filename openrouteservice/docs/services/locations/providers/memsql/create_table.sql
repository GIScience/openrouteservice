  CREATE TABLE `planet_osm_pois` (
  `osm_id` bigint(20) NOT NULL,
  `category` longtext CHARACTER SET utf8 COLLATE utf8_general_ci,
  `category_type` longtext CHARACTER SET utf8 COLLATE utf8_general_ci,
  `name` longtext CHARACTER SET utf8 COLLATE utf8_general_ci,
  `phone` longtext CHARACTER SET utf8 COLLATE utf8_general_ci,
  `website` longtext CHARACTER SET utf8 COLLATE utf8_general_ci,
  `opening_hours` longtext CHARACTER SET utf8 COLLATE utf8_general_ci,
  `access` longtext CHARACTER SET utf8 COLLATE utf8_general_ci,
  `smoking` longtext CHARACTER SET utf8 COLLATE utf8_general_ci,
  `way` geographypoint DEFAULT NULL,
   SHARD KEY (`osm_id`),
   index (way)
);
