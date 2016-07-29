/*
 * The Unified Mapping Platform (JUMP) is an extensible, interactive GUI
 * for visualizing and manipulating spatial features with geometry and attributes.
 *
 * Copyright (C) 2003 Vivid Solutions
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * For more information, contact:
 *
 * Vivid Solutions
 * Suite #1A
 * 2328 Government Street
 * Victoria BC  V8T 5G5
 * Canada
 *
 * (250)385-6040
 * www.vividsolutions.com
 */

package org.freeopenls.overlay;

import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.IndexedFeatureCollection;

/**
 * Class for "Overlay"(Intersection/Within) two FeatureCollecion with/without an Index.
 * Creates new FeatureCollection with containing intersections of all pairs of
 * features from two input featurecollection.
 *
 * @author revised & modified by Pascal Neis, neis@geographie.uni-bonn.de
 * @version 1.0 2007-07-25
 * @version 1.1 2008-04-22
 */
public class Overlay {
  /** FeatureCollectone A for Overlay */
  private IndexedFeatureCollection m_A = null;
  /** FeatureCollection B for Overlay */
  private FeatureCollection m_B = null;
  /** FeatureCollection Overlay-Result */
  private FeatureCollection m_resultFeatColl = null;

  /**
  * Construktor for Overlay
  *
  * @param A FeatureCollection A
  * @param B FeatureCollection B
  */
  public Overlay(IndexedFeatureCollection A, FeatureCollection B) {
	  OverlayEngine overlayEngine = new OverlayEngine();
	  m_A = A;
	  m_B = B;
	  m_resultFeatColl = overlayEngine.overlay(m_A, m_B);
  }

  /**
   * Construktor for Overlay
   *
   * @param A FeatureCollection A
   * @param B FeatureCollection B
   */
   public Overlay(FeatureCollection A, FeatureCollection B) {
	   OverlayEngine overlayEngine = new OverlayEngine();
	   m_A = new IndexedFeatureCollection(A);
	   m_B = B;
	   m_resultFeatColl = overlayEngine.overlay(m_A, m_B);
   }
   
  /**
  * Method that return the Result of the Overlay
  *
  * @return FeatureCollection
  *				FeatureCollection Result
  */
  public FeatureCollection getResultFeatColl() throws Exception {
         return m_resultFeatColl;
  }
}