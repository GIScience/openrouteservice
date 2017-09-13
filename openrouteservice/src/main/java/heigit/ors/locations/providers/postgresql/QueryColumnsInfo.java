package heigit.ors.locations.providers.postgresql;

import java.sql.ResultSet;
import java.util.List;

import com.graphhopper.util.Helper;

public class QueryColumnsInfo 
{
	private String _query1Columns;
	private String _query2Columns;
	private String _returnTable;
	private ColumnDescription[] _columns;
	private int _returnColumnCount;

	public QueryColumnsInfo(ColumnDescription[] columns, List<String> ignoreQuery2Columns)
	{
		_columns = columns;

		_query1Columns = "";
		_query2Columns = "";
		String returnTable = "";

		int nColumns = _columns.length;
		_returnColumnCount = nColumns;
		
		for (int i = 0; i < nColumns; i++)
		{
			String clmName = _columns[i].getName();
			
			// Skp distance as it is added as the last column
			if (clmName.equals("distance"))
				continue;

			_query1Columns += clmName + ", ";

			if (!clmName.equalsIgnoreCase("geom"))
			{
				if (ignoreQuery2Columns != null && ignoreQuery2Columns.contains(clmName))
				{
					// skip column from result
					_returnColumnCount--;
				}
				else
				{
					_query2Columns += clmName + ", ";
				}
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
		case "osm_type":
			return "smallint";
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
		return _columns[index].getName();
	}
	
	@SuppressWarnings("rawtypes")
	public Object getType(int index, ResultSet resultSet) throws Exception
	{
		Class type = _columns[index].getType();
		
		if (type == String.class)
		{
			String str = resultSet.getString(index + 1);
			if (!Helper.isEmpty(str))
				return str;
		}
		else if (type == Integer.class)
		{
			return resultSet.getInt(index + 1);
		}
		else if (type == Short.class)
		{
			return resultSet.getShort(index + 1);
		}
		else if (type == Long.class)
		{
			return resultSet.getLong(index + 1);
		}
		else if (type == Double.class)
		{
			return resultSet.getDouble(index + 1);
		}
		
		return null;
	}

	public int getReturnColumnsCount()
	{
		return _returnColumnCount;
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
