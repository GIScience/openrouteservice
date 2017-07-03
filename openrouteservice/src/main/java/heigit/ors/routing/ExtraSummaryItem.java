package heigit.ors.routing;

public class ExtraSummaryItem {
   private int value;
   private double distance;
   private double amount;
   
   public ExtraSummaryItem(int value, double distance, double amount)
   {
	   this.value = value;
	   this.distance = distance;
	   this.amount = amount;
   }
   
   public int getValue()
   {
	   return value;
   }
   
   public double getDistance()
   {
	   return distance;
   }
   
   public double getAmount()
   {
	   return amount;
   }
}
