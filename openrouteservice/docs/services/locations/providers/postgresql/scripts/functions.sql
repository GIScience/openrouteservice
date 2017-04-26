DROP FUNCTION IF EXISTS ORS_FindLocations(text, text, bytea, double precision, int);

CREATE FUNCTION ORS_FindLocations(table_name text, condition text, geom_wkb bytea, distance float, limit_size int)
 RETURNS SETOF record AS $$
DECLARE
  geog_feature geography; 
  geom_feature geometry;
  geom_feature_simplified geometry;
  geom_env geometry;
  geom_type text;
  geom_length float;
  max_latitude float;
  distance_factor float;
  distance_buffer float;
  simplification_tolerance float;
  rec RECORD;
  query text;
BEGIN
    IF (geom_wkb IS NULL) THEN
       RETURN;
    END IF;
    
    geom_feature := ST_SetSrid(ST_GeomFromWKB(geom_wkb), 4326);

    IF (ST_IsEmpty(geom_feature)) THEN 
	RETURN;
    END IF;

    geom_type := GeometryType(geom_feature);
    geog_feature := geom_feature::geography;

    CASE geom_type 
      WHEN 'POINT'  THEN
        query := 'SELECT * FROM ' || table_name ||' WHERE (' || condition || ') AND ST_DWithin(location, $1 , $2)';

        IF (limit_size > 0) THEN
           query := query || ' LIMIT '  || limit_size::text; 
        END IF;
        
	FOR rec IN EXECUTE format(query) USING geog_feature, distance LOOP
   	  --IF ST_DWithin(rec.location, geog_feature, distance) THEN 
            rec.distance := round(ST_Distance(rec.location, geog_feature)::numeric, 2);   	  
            IF (rec.distance <= distance) THEN
	       RETURN NEXT rec;
	    END IF;   
        END LOOP;
        
      WHEN 'LINESTRING'  THEN
	geom_env := ST_Envelope(geom_feature);

        simplification_tolerance := 0.0001;
        distance_buffer := 0.0;
        IF (ST_NumPoints(geom_feature) > 200) THEN
           geom_length := ST_Length(geog_feature);
           IF geom_length > 500000 THEN
             distance_buffer := 100;
             simplification_tolerance := 0.002;
           ELSEIF geom_length > 300000 THEN      
             distance_buffer := 80;
             simplification_tolerance := 0.0007;
           ELSEIF geom_length > 200000 THEN      
             distance_buffer := 50;
             simplification_tolerance := 0.0005;
           ELSEIF geom_length > 100000 THEN      
             distance_buffer := 30;
             simplification_tolerance := 0.0002;
           END IF;
        END IF;   
           
	-- Simplify geometry to reduce number of vertices
	geom_feature := ST_SimplifyPreserveTopology(geom_feature, 0.0001);
	geom_feature_simplified := ST_SimplifyPreserveTopology(geom_feature, simplification_tolerance);
	geom_feature_simplified := ST_Transform(ST_SimplifyPreserveTopology(geom_feature, simplification_tolerance), 900913);
	geom_feature := ST_Transform(geom_feature, 900913);
	max_latitude := GREATEST(abs(ST_YMin(geom_env)), abs(ST_YMax(geom_env)));
	distance_factor := 1/COS(max_latitude*3.1416/180);

        IF (condition = '') THEN
	    query := 'SELECT * FROM ' || table_name ||' WHERE ST_DWithin(geom, $1 , $2)';
        ELSE
            query := 'SELECT * FROM ' || table_name ||' WHERE (' || condition || ') AND ST_DWithin(geom, $1 , $2)';
        END IF;

	IF (limit_size > 0) THEN
           query := query || ' LIMIT '  || limit_size::text; 
        END IF;
        -- We search points in two passes.
        -- First pass looks for points using DWithin and geometries in 900913, which is much faster than geography. In this approach we have to consider scale factor introduced by Mercator distortion.
        -- Second pass checks found points using more accurate DWithin function that operates with geographies.
        
	FOR rec IN EXECUTE format(query) USING geom_feature_simplified, (distance_factor*distance + distance_buffer) LOOP
	    --IF ST_DWithin(geog_feature, rec.location, distance) THEN -- this approach is 2 times slowe than if we compute ST_Distance directly and make a comparison
	    -- direct calling ST_Distance(rec.location, geog_feature) is about 150% slower.  
	    rec.distance := round(ST_Distance(rec.location, ST_Transform(ST_ClosestPoint(geom_feature, rec.geom), 4326)::geography)::numeric, 2);
	    IF (rec.distance <= distance) THEN
		RETURN NEXT rec; 
	    END IF;
        END LOOP;

     WHEN 'POLYGON'  THEN
        IF (distance > 0) THEN
	   geog_feature := ST_Buffer(geog_feature, distance);
        END IF;

        IF (condition = '') THEN
           query := 'SELECT * FROM ' || table_name ||' WHERE ST_DWithin(location, $1, 0)';
        ELSE
           query := 'SELECT * FROM ' || table_name ||' WHERE (' || condition || ') AND ST_DWithin(location, $1, 0)';
        END IF;   

        IF (limit_size > 0) THEN
           query := query || ' LIMIT '  || limit_size::text; 
        END IF;
       
        FOR rec IN EXECUTE format(query) USING geog_feature LOOP
            rec.distance := 0.0;   
            RETURN NEXT rec;
        END LOOP;
        
    END CASE;

    RETURN;
END;
$$ LANGUAGE plpgsql;


--*********************************************************************************************************

DROP FUNCTION IF EXISTS ORS_FindLocationCategories(text, text, bytea, double precision);

CREATE FUNCTION ORS_FindLocationCategories(table_name text, condition text, geom_wkb bytea, distance float)
 RETURNS SETOF record AS $$
DECLARE
  geog_feature geography; 
  geom_feature geometry;
  geom_env geometry;
  geom_type text;
  max_latitude float;
  factor float;
  rec RECORD;
  query text;
BEGIN
    IF (geom_wkb IS NULL) THEN
       RETURN;
    END IF;
    
    geom_feature := ST_SetSrid(ST_GeomFromWKB(geom_wkb), 4326);

    IF (ST_IsEmpty(geom_feature)) THEN 
	RETURN;
    END IF;

    geom_type := GeometryType(geom_feature);
    geog_feature := geom_feature::geography;
    IF (condition = '' OR condition IS NULL) THEN
       condition := '1=1';
    END IF;

    CASE geom_type 
      WHEN 'POINT'  THEN
          --SELECT category, COUNT(category) FROM %s WHERE (%s) GROUP BY category ORDER BY category
        query := 'SELECT category, COUNT(category) AS count FROM ' || table_name ||' WHERE (' || condition || ') AND ST_DWithin(location, $1 , $2) GROUP BY category ORDER BY category';

	FOR rec IN EXECUTE format(query) USING geog_feature, distance LOOP
	    RETURN NEXT rec;
        END LOOP;
        
      WHEN 'LINESTRING'  THEN
	geom_env := ST_Envelope(geom_feature);
    
	-- Simplify geometry to reduce number of vertices
	geom_feature := ST_Transform(ST_SimplifyPreserveTopology(geom_feature, 0.0001), 900913);
	max_latitude := GREATEST(abs(ST_YMin(geom_env)), abs(ST_YMax(geom_env)));
	factor := 1/COS(max_latitude*3.1416/180);

	query := 'SELECT category, COUNT(category) AS count FROM ' || table_name ||' WHERE (' || condition || ') AND ST_DWithin(geom, $1 , $2) AND ST_DWithin(location, ST_Transform(ST_ClosestPoint($1, geom), 4326)::geography, $3) GROUP BY category ORDER BY category';

	FOR rec IN EXECUTE format(query) USING geom_feature, factor*distance, distance LOOP
	    RETURN NEXT rec;
        END LOOP;

     WHEN 'POLYGON'  THEN
        geom_feature := ST_Transform(geom_feature, 900913);
        
        query := 'SELECT category, COUNT(category) FROM ' || table_name ||' WHERE (' || condition || ') AND ST_Within(geom, $1) GROUP BY category ORDER BY category';

        FOR rec IN EXECUTE format(query) USING geom_feature LOOP
            RETURN NEXT rec;
        END LOOP;
        
    END CASE;

    RETURN;
END;
$$ LANGUAGE plpgsql;


