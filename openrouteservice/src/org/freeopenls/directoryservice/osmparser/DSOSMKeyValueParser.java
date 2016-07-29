/*+-------------+----------------------------------------------------------*
 *|        /\   |   University of Bonn                                     *
 *|       |  |  |     Department of Geopgraphy                             *
 *|      _|  |_ |     Chair of Cartography                                 *
 *|    _/      \|     (C) 2009                                             *
 *|___|         |                                                          *
 *|             |     Meckenheimer Allee 172                               *
 *|             |     D-53115 Bonn, Germany                                *
 *+-------------+----------------------------------------------------------*/

package org.freeopenls.directoryservice.osmparser;

import java.util.ArrayList;

/**
 * <p><b>Title: TODO Enter class name</b></p>
 * <p><b>Description:</b> TODO Enter a description for this class </p>
 *
 * @author Jo
 * created 04.11.2009 / 16:59:34
 * @version
 * $Revision$
 * $LastChangedDate$
 * @author last edited by: $Author$
 * 
 */
public class DSOSMKeyValueParser {
	
	ArrayList<KeyValue> keyvalues;
	
	
	public DSOSMKeyValueParser(String keyvaluepairs, String tuplesplitter){
		cutStrings(keyvaluepairs, tuplesplitter);
		
	}
	
	private void cutStrings(String keyvaluepairs, String tuplesplitter){
		this.keyvalues = new ArrayList<KeyValue>();
		String[] kvs = keyvaluepairs.split(tuplesplitter);
		
		for (int i = 0; i < kvs.length; i++) {
			String[] kvtupel = kvs[i].split("=");
			this.keyvalues.add(new KeyValue(kvtupel[0], kvtupel[1]));
		}
		
	}
	
	public String getSQLString(){
		String sqlstring = "";
		for (KeyValue kv : keyvalues) {
			if(kv.getKey().equalsIgnoreCase("disabled")){
				sqlstring += kv.getKey()+" like '%"+kv.getValue()+"%' AND ";
			}else{
				sqlstring += kv.getKey()+"='"+kv.getValue()+"' AND ";	
			}
			
		}
		sqlstring = sqlstring.substring(0, sqlstring.length()-5); // delete last &&
		
		return sqlstring;
	}
	
}
