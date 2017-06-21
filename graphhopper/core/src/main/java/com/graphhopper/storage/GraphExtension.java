/*
 *  Licensed to GraphHopper GmbH under one or more contributor
 *  license agreements. See the NOTICE file distributed with this work for 
 *  additional information regarding copyright ownership.
 * 
 *  GraphHopper GmbH licenses this file to you under the Apache License, 
 *  Version 2.0 (the "License"); you may not use this file except in 
 *  compliance with the License. You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.graphhopper.storage;

import java.util.ArrayList;

/**
 * If you need custom storages, like turn cost tables, or osmid tables for your graph you implement
 * this interface and put it in any graph storage you want.
 */
public interface GraphExtension extends Storable<GraphExtension> {
    /**
     * @return true, if and only if, if an additional field at the graphs node storage is required
     */
    boolean isRequireNodeField();

    /**
     * @return true, if and only if, if an additional field at the graphs edge storage is required
     */
    boolean isRequireEdgeField();

    /**
     * @return the default field value which will be set for default when creating nodes
     */
    int getDefaultNodeFieldValue();

    /**
     * @return the default field value which will be set for default when creating edges
     */
    int getDefaultEdgeFieldValue();

    /**
     * initializes the extended storage by giving the base graph
     */
    void init(Graph graph, Directory dir);

    /**
     * sets the segment size in all additional data storages
     */
    void setSegmentSize(int bytes);

    /**
     * creates a copy of this extended storage
     */
    GraphExtension copyTo(GraphExtension extStorage);

    /**
     * default implementation defines no additional fields or any logic. there's like nothing , like
     * the default behavior.
     */
    class NoOpExtension implements GraphExtension {

        @Override
        public boolean isRequireNodeField() {
            return false;
        }

        @Override
        public boolean isRequireEdgeField() {
            return false;
        }

        @Override
        public int getDefaultNodeFieldValue() {
            return 0;
        }

        @Override
        public int getDefaultEdgeFieldValue() {
            return 0;
        }

        @Override
        public void init(Graph graph, Directory dir) {
            // noop
        }

        @Override
        public GraphExtension create(long byteCount) {
            // noop
            return this;
        }

        @Override
        public boolean loadExisting() {
            // noop
            return true;
        }

        @Override
        public void setSegmentSize(int bytes) {
            // noop
        }

        @Override
        public void flush() {
            // noop
        }

        @Override
        public void close() {
            // noop
        }

        @Override
        public long getCapacity() {
            return 0;
        }

        @Override
        public GraphExtension copyTo(GraphExtension extStorage) {
            // noop
            return extStorage;
        }

        @Override
        public String toString() {
            return "NoExt";
        }

        @Override
        public boolean isClosed() {
            return false;
        }
    }
    
    public class ExtendedStorageSequence implements GraphExtension
    {
        private GraphExtension[] extensions;
        private int numExtensions;
        private boolean isRequireNodeField = false;
        private boolean isRequireEdgeField = false;
        private int defaultNodeValue = -1;
        private int defaultEdgeValue = -1;
        
        public ExtendedStorageSequence(ArrayList<GraphExtension> seq)
        {
        	numExtensions = seq.size();
        	extensions = seq.toArray(new GraphExtension[numExtensions]);
        	
        	for (int i = 0; i< numExtensions;i++ )
    		{
        		GraphExtension ge = extensions[i];
    			if  (ge.isRequireNodeField())
    			{
    				isRequireNodeField = true;
    				defaultNodeValue = ge.getDefaultNodeFieldValue();
    			}
    			if  (ge.isRequireEdgeField())
    			{
    				isRequireEdgeField = true;
    				defaultEdgeValue = ge.getDefaultEdgeFieldValue();
    			}
    		}
        }
        
        public GraphExtension[] getExtensions()
        {
        	return extensions;
        }
        
        @Override
        public boolean isRequireNodeField()
        {
        	return isRequireNodeField;
        }

        @Override
        public boolean isRequireEdgeField()
        {
        	return isRequireEdgeField;
        }

        @Override
        public int getDefaultNodeFieldValue()
        {
        	return defaultNodeValue;
        }

        @Override
        public int getDefaultEdgeFieldValue()
        {
        	return defaultEdgeValue;
        }

        @Override
        public void init(Graph graph, Directory dir )
        {
        	for (int i = 0; i< numExtensions;i++ )
    		{
    			extensions[i].init(graph, dir);
    		}
        }

        @Override
        public GraphExtension create(long initSize )
        {
        	for (int i = 0; i< numExtensions;i++ )
    		{
    			extensions[i].create(initSize);
    		}
        	
        	return extensions[0];
        }

        @Override
        public boolean loadExisting()
        {
        	boolean result = true;
        	for (int i = 0; i< numExtensions;i++ )
    		{
    			if (!extensions[i].loadExisting())
    			{
    				result = false;
    				break;
    			}
    		}
        	
        	return result;
        }

        @Override
        public void setSegmentSize( int bytes )
        {
        	for (int i = 0; i< numExtensions;i++ )
    		{
    			extensions[i].setSegmentSize(bytes);
    		}
        }

        @Override
        public void flush()
        {
        	for (int i = 0; i< numExtensions;i++ )
    		{
    			extensions[i].flush();
    		}
        }

        @Override
        public void close()
        {
        	for (int i = 0; i< numExtensions;i++ )
    		{
    			extensions[i].close();
    		}
        }

        @Override
        public long getCapacity()
        {
        	return extensions[0].getCapacity();
        }

        @Override
        public GraphExtension copyTo( GraphExtension extStorage )
        {
            // noop
            return extStorage;
        }

        @Override
        public String toString()
        {
            return "ExtSequence";
        }

		@Override
		public boolean isClosed() {
			// TODO Auto-generated method stub
			return false;
		}
    }
}
