DROP TABLE IF EXISTS planet_osm_pois;

CREATE TABLE planet_osm_pois AS 
(
(SELECT osm_id, 
      COALESCE(
         CASE
          WHEN amenity IS NOT NULL THEN 'amenity'
          WHEN shop IS NOT NULL THEN 'shop'
          WHEN tourism IS NOT NULL THEN 'tourism'
          WHEN leisure IS NOT NULL THEN 'leisure'
          ELSE 'none'
         END, '') as category, 

      COALESCE(
         CASE
          WHEN amenity IS NOT NULL THEN amenity
          WHEN shop IS NOT NULL THEN shop
          WHEN tourism IS NOT NULL THEN tourism
          WHEN leisure IS NOT NULL THEN leisure
          ELSE NULL
         END, '') as category_type,
         
      name, 
      
      COALESCE(
         CASE
          WHEN (tags->'phone') IS NOT NULL THEN tags->'phone'
          WHEN (tags->'contact:phone') IS NOT NULL THEN tags->'contact:phone'
          ELSE NULL
         END, '') as phone,
         
      tags->'website' as website, 
      
      tags->'opening_hours' as opening_hours,
      
      COALESCE(
         CASE
          WHEN (tags->'wheelchair' = 'yes') THEN 'wheelchair'
          ELSE NULL
         END, '') as access,
         
      tags->'smoking' as smoking,
      
      way 
FROM planet_osm_point 
WHERE (amenity IS NOT NULL OR shop IS NOT NULL  OR tourism IS NOT NULL OR leisure IS NOT NULL))

UNION ALL

(SELECT osm_id, 
      COALESCE(
         CASE
          WHEN amenity IS NOT NULL THEN 'amenity'
          WHEN (tags->'shop') IS NOT NULL THEN 'shop'
          WHEN tourism IS NOT NULL THEN 'tourism'
          WHEN leisure IS NOT NULL THEN 'leisure'
          ELSE 'none'
         END, '') as category, 

      COALESCE(
         CASE
          WHEN amenity IS NOT NULL THEN amenity
          WHEN (tags->'shop') IS NOT NULL THEN (tags->'shop')
          WHEN tourism IS NOT NULL THEN tourism
          WHEN leisure IS NOT NULL THEN leisure
          ELSE NULL
         END, '') as category_type,
         
      name, 
      
      COALESCE(
         CASE
          WHEN (tags->'phone') IS NOT NULL THEN tags->'phone'
          WHEN (tags->'contact:phone') IS NOT NULL THEN tags->'contact:phone'
          ELSE NULL
         END, '') as phone,
         
      tags->'website' as website, 
      
      tags->'opening_hours' as opening_hours,
      
      COALESCE(
         CASE
          WHEN (tags->'wheelchair' = 'yes') THEN 'wheelchair'
          ELSE NULL
         END, '') as access,
         
      tags->'smoking' as smoking,
      
      ST_PointOnSurface(way) as way 
FROM planet_osm_polygon 
WHERE (amenity IS NOT NULL OR (tags->'shop') IS NOT NULL))
);


DROP INDEX IF EXISTS  planet_osm_pois_index;

CREATE INDEX planet_osm_pois_index
  ON planet_osm_pois
  USING gist
  (way );

CLUSTER planet_osm_pois USING planet_osm_pois_index;
CREATE INDEX planet_osm_pois_category_type_idx ON planet_osm_pois(category_type);
CREATE INDEX planet_osm_pois_name_idx ON planet_osm_pois USING btree (name); 

