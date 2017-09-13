package heigit.ors.util;

public class HashUtility 
{
   public static int getHashCode(Object obj1, Object obj2)
   {
	   int hash = 23;
	   hash = hash * 31 + obj1.hashCode();
	   hash = hash * 31 + obj2.hashCode();
	   
	   return hash;
   }
}
