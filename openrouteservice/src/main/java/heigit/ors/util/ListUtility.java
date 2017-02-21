package heigit.ors.util;

import java.util.Iterator;
import java.util.List;

public class ListUtility {
   public static <E> E getElement(List<E> list, E value)
   {
	    Iterator<E> iter = list.iterator();
	    while (iter.hasNext()) {
	        E c = iter.next();
	        if (c.equals(value)) {
	           return c;
	        }
	    }
	    
	    return null;
   }
}
