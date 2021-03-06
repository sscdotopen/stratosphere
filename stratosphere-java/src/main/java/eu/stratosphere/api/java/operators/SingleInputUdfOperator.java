/***********************************************************************************************************************
 *
 * Copyright (C) 2010-2013 by the Stratosphere project (http://stratosphere.eu)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 **********************************************************************************************************************/
package eu.stratosphere.api.java.operators;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import eu.stratosphere.api.java.DataSet;
import eu.stratosphere.api.java.typeutils.TypeInformation;
import eu.stratosphere.configuration.Configuration;

/**
 *
 * @param <IN> The data type of the input data set.
 * @param <OUT> The data type of the returned data set.
 */
public abstract class SingleInputUdfOperator<IN, OUT, O extends SingleInputUdfOperator<IN, OUT, O>>
	extends SingleInputOperator<IN, OUT, O> implements UdfOperator<O>
{
	private Configuration parameters;
	
	private Map<String, DataSet<?>> broadcastVariables;
	
	// --------------------------------------------------------------------------------------------
	
	protected SingleInputUdfOperator(DataSet<IN> input, TypeInformation<OUT> resultType) {
		super(input, resultType);
	}
	
	// --------------------------------------------------------------------------------------------
	// Fluent API methods
	// --------------------------------------------------------------------------------------------
	
	@Override
	public O withParameters(Configuration parameters) {
		this.parameters = parameters;
		
		@SuppressWarnings("unchecked")
		O returnType = (O) this;
		return returnType;
	}
	
	@Override
	public O withBroadcastSet(DataSet<?> data, String name) {
		if (this.broadcastVariables == null) {
			this.broadcastVariables = new HashMap<String, DataSet<?>>();
		}
		
		this.broadcastVariables.put(name, data);
		
		@SuppressWarnings("unchecked")
		O returnType = (O) this;
		return returnType;
	}
	
	
	// --------------------------------------------------------------------------------------------
	// Accessors
	// --------------------------------------------------------------------------------------------
	
	@Override
	public Map<String, DataSet<?>> getBroadcastSets() {
		return this.broadcastVariables == null ? Collections.<String, DataSet<?>>emptyMap() : this.broadcastVariables;
	}
	
	@Override
	public Configuration getParameters() {
		return this.parameters;
	}
}
