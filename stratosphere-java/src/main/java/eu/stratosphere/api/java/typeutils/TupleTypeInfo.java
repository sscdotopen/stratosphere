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

import eu.stratosphere.api.common.typeutils.Serializer;
import eu.stratosphere.api.java.tuple.*;
import eu.stratosphere.api.java.typeutils.runtime.TupleSerializer;


public class TupleTypeInfo<T extends Tuple> extends TypeInformation<T> {
	
	private final TypeInformation<?>[] types;
	
	
	public TupleTypeInfo(TypeInformation<?>... types) {
		if (types == null || types.length == 0 || types.length >= Tuple.MAX_ARITY)
			throw new IllegalArgumentException();
		
		this.types = types;
	}

	
	@Override
	public boolean isBasicType() {
		return false;
	}

	@Override
	public boolean isTupleType() {
		return true;
	}

	@Override
	public int getArity() {
		return types.length;
	}

	@Override
	public Class<T> getTypeClass() {
		@SuppressWarnings("unchecked")
		Class<T> tc = (Class<T>) CLASSES[getArity() - 1];
		return tc;
	}

	
	public <X> TypeInformation<X> getTypeAt(int pos) {
		if (pos < 0 || pos >= this.types.length)
			throw new IndexOutOfBoundsException();

		@SuppressWarnings("unchecked")
		TypeInformation<X> typed = (TypeInformation<X>) this.types[pos];
		return typed;
	}
	
	@Override
	public Serializer<T> createSerializer() {
		Serializer<?>[] fieldSerializers = new Serializer<?>[getArity()];
		for (int i = 0; i < types.length; i++) {
			fieldSerializers[i] = types[i].createSerializer();
		}
		
		Class<T> tupleClass = getTypeClass();
		
		return new TupleSerializer<T>(tupleClass, fieldSerializers);
	}
	
	
	@Override
	public String toString() {
		StringBuilder bld = new StringBuilder("Tuple");
		bld.append(types.length).append('<');
		bld.append(types[0]);
		
		for (int i = 1; i < types.length; i++) {
			bld.append(", ").append(types[i]);
		}
		
		bld.append('>');
		return bld.toString();
	}
	
	// --------------------------------------------------------------------------------------------
	
	public static <X extends Tuple> TupleTypeInfo<X> getBasicTupleTypeInfo(Class<?>... basicTypes) {
		if (basicTypes == null || basicTypes.length == 0)
			throw new IllegalArgumentException();
		
		TypeInformation<?>[] infos = new TypeInformation<?>[basicTypes.length];
		for (int i = 0; i < infos.length; i++) {
			Class<?> type = basicTypes[i];
			if (type == null) {
				throw new IllegalArgumentException("Type at position " + i + " is null.");
			}
			
			TypeInformation<?> info = BasicTypeInfo.getInfoFor(type);
			if (info == null) {
				throw new IllegalArgumentException("Type at position " + i + " is not a basic type.");
			}
			infos[i] = info;
		}
		
		@SuppressWarnings("unchecked")
		TupleTypeInfo<X> tupleInfo = (TupleTypeInfo<X>) new TupleTypeInfo<Tuple>(infos);
		return tupleInfo;
	}
	
	// --------------------------------------------------------------------------------------------
	
	private static final Class<?>[] CLASSES = new Class<?>[] {
		Tuple1.class, Tuple2.class, Tuple3.class, Tuple4.class, Tuple5.class,
		Tuple6.class, Tuple7.class, Tuple8.class, Tuple9.class, Tuple10.class,
		Tuple11.class, Tuple12.class, Tuple13.class, Tuple14.class, Tuple15.class,
		Tuple16.class, Tuple17.class, Tuple18.class, Tuple19.class, Tuple20.class,
		Tuple21.class, Tuple22.class
	};
}