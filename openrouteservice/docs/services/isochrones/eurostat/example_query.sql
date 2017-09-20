SELECT Round(SUM(c.ratio * c.%%column_name%%)) 
FROM   ( 
              SELECT St_area(St_intersection(a.geog,poly)) / St_area(a.geog) ratio, 
                     a.* 
              FROM   geostat_grd_2016_ageclasses_nuts a, 
                     %%wkb_geom%% 
              WHERE  a.gid IN 
                     ( 
                            SELECT a.gid 
                            FROM   geostat_grd_2016_ageclasses_nuts a, 
                                   %%wkb_geom%% 
                            WHERE  st_intersects (a.geog, poly))) AS c;
