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
package eu.stratosphere.api.java.typeutils;

import java.util.HashMap;
import java.util.Map;


/**
 *
 */
public class BasicTypeInfo<T> implements TypeInformation<T> {

	public static final BasicTypeInfo<String> STRING_TYPE_INFO = new BasicTypeInfo<String>(String.class);
	public static final BasicTypeInfo<Boolean> BOOLEAN_TYPE_INFO = new BasicTypeInfo<Boolean>(Boolean.class);
	public static final BasicTypeInfo<Byte> BYTE_TYPE_INFO = new BasicTypeInfo<Byte>(Byte.class);
	public static final BasicTypeInfo<Short> SHORT_TYPE_INFO = new BasicTypeInfo<Short>(Short.class);
	public static final BasicTypeInfo<Integer> INT_TYPE_INFO = new BasicTypeInfo<Integer>(Integer.class);
	public static final BasicTypeInfo<Long> LONG_TYPE_INFO = new BasicTypeInfo<Long>(Long.class);
	public static final BasicTypeInfo<Float> FLOAT_TYPE_INFO = new BasicTypeInfo<Float>(Float.class);
	public static final BasicTypeInfo<Double> DOUBLE_TYPE_INFO = new BasicTypeInfo<Double>(Double.class);
	public static final BasicTypeInfo<Character> CHAR_TYPE_INFO = new BasicTypeInfo<Character>(Character.class);
	
	// --------------------------------------------------------------------------------------------

	private final Class<T> clazz;
	
	
	private BasicTypeInfo(Class<T> clazz) {
		this.clazz = clazz;
	}
	
	// --------------------------------------------------------------------------------------------
	
	@Override
	public boolean isBasicType() {
		return true;
	}

	@Override
	public boolean isTupleType() {
		return false;
	}

	@Override
	public int getArity() {
		return 1;
	}

	@Override
	public Class<T> getType() {
		return this.clazz;
	}
	
	// --------------------------------------------------------------------------------------------
	
	public static <X> BasicTypeInfo<X> getInfoFor(Class<X> type) {
		if (type == null)
			throw new NullPointerException();
		
		@SuppressWarnings("unchecked")
		BasicTypeInfo<X> info = (BasicTypeInfo<X>) TYPES.get(type);
		return info;
	}
	
	private static final Map<Class<?>, BasicTypeInfo<?>> TYPES = new HashMap<Class<?>, BasicTypeInfo<?>>();
	
	static {
		TYPES.put(String.class, STRING_TYPE_INFO);
		TYPES.put(Boolean.class, BOOLEAN_TYPE_INFO);
		TYPES.put(Byte.class, BYTE_TYPE_INFO);
		TYPES.put(Short.class, SHORT_TYPE_INFO);
		TYPES.put(Integer.class, INT_TYPE_INFO);
		TYPES.put(Long.class, LONG_TYPE_INFO);
		TYPES.put(Float.class, FLOAT_TYPE_INFO);
		TYPES.put(Double.class, DOUBLE_TYPE_INFO);
		TYPES.put(Character.class, CHAR_TYPE_INFO);
	}
}