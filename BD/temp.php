<?php
WITH RECURSIVE search_constituida(super, sub, depth, path, cycle) AS (
	    SELECT c.super, c.sub, 1,
	    	   ARRAY[g.super],
	    	   false
	    FROM constituida c
    UNION ALL
	    SELECT c.super, c.sub, sc.depth + 1,
	    	   path || c.super,
	    	   c.super = ANY(path)
	    FROM constituida c, search_constituida sc
	    WHERE c.super = sc.sub AND NOT cycle
)
SELECT * FROM search_constituida;

?>