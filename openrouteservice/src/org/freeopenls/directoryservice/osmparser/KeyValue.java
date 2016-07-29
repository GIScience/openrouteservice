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

/**
 * <p><b>Title: TODO Enter class name</b></p>
 * <p><b>Description:</b> TODO Enter a description for this class </p>
 *
 * @author Jo
 * created 04.11.2009 / 17:24:49
 * @version
 * $Revision$
 * $LastChangedDate$
 * @author last edited by: $Author$
 * 
 */
public class KeyValue {

	String key;
	String value;
	
	public KeyValue(String key, String value){
		this.key = key;
		this.value = value;
	}

	/**
	 * @return the key
	 */
	public String getKey() {
		return key;
	}

	/**
	 * @param key the key to set
	 */
	public void setKey(String key) {
		this.key = key;
	}

	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}
	
	

}
