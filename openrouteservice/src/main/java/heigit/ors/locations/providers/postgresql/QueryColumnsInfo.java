package heigit.ors.locations.providers.postgresql;

public class QueryColumnsInfo 
{
	private String _query1Columns;
	private String _query2Columns;
	private String _returnTable;
	private String[] _columnNames;

	public QueryColumnsInfo(String[] columnNames)
	{
		_columnNames = columnNames;

		_query1Columns = "";
		_query2Columns = "";
		String returnTable = "";

		int nColumns = _columnNames.length;
		for (int i = 0; i < nColumns; i++)
		{
			String clmName = _columnNames[i];
			
			// Skp distance as it is added as the last column
			if (clmName.equals("distance"))
				continue;

			_query1Columns += clmName + ", ";

			if (!clmName.equalsIgnoreCase("geom"))
			{
				_query2Columns += clmName + ", ";
			}
			else
			{
				_query2Columns += "ST_AsBinary(location::geometry) as geom, ";
			}

			returnTable += clmName + " " + getColumnDataType(clmName) + ", ";
		}

		_query1Columns += " location, 1.0 as distance";
		_query2Columns += " distance";
		returnTable += " location geography, distance numeric";

		_returnTable = "pois(" + returnTable + ")";
	}

	private String getColumnDataType(String clmName)
	{
		switch(clmName)
		{
		case"osm_id":
			return "bigint";
		case "category":
			return "smallint";
		case "name":
			return "text";
		case "phone":
			return "text";
		case "website":
			return "text";
		case "opening_hours":
			return "text";
		case "wheelchair":
			return "text";
		case "smoking":
			return "text";
		case "fee":
			return "text";
		case "address":
			return "text";
		case "geom":
			return "geometry";
		case "location":
			return "geography";
		default:
			return "";
		}
	}

	public String getName(int index)
	{
		return _columnNames[index];
	}

	public int getCount()
	{
		return _columnNames.length;
	}

	public String getQuery1Columns()
	{
		return _query1Columns;
	}

	public String getQuery2Columns()
	{
		return _query2Columns;
	}

	public String getReturnTable()
	{
		return _returnTable;
	}
}
