package heigit.ors.locations.providers.postgresql;

public class ColumnDescription {
   public String _name;
   public Class _type;
   
   public ColumnDescription(String name, Class type)
   {
	   _name = name;
	   _type = type;
   }
   
   public String getName()
   {
	   return _name;
   }
   
   public Class getType()
   {
	   return _type;
   }
}
