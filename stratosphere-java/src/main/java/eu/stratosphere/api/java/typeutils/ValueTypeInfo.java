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
import eu.stratosphere.api.java.typeutils.runtime.CopyableValueSerializer;
import eu.stratosphere.types.CopyableValue;
import eu.stratosphere.types.Value;


public class ValueTypeInfo<T extends Value> extends TypeInformation<T> {

	private final Class<T> type;

	
	public ValueTypeInfo(Class<T> type) {
		this.type = type;
	}
	
	
	@Override
	public int getArity() {
		return 1;
	}


	@Override
	public Class<T> getTypeClass() {
		return this.type;
	}


	@Override
	public boolean isBasicType() {
		return false;
	}

	@Override
	public boolean isTupleType() {
		return false;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Serializer<T> createSerializer() {
		if (CopyableValue.class.isAssignableFrom(type)) {
			return createCopyableSerializer(type.asSubclass(CopyableValue.class));
		}
		else {
			throw new UnsupportedOperationException("Serialization is not yet implemented for Value types that are not CopyableValue subclasses.");
		}
	}
	
	@SuppressWarnings("unchecked")
	private static <T, X extends CopyableValue<X>> Serializer<T> createCopyableSerializer(Class<X> clazz) {
		Serializer<X> ser = new CopyableValueSerializer<X>(clazz);
		return (Serializer<T>) ser;
	}
	
	@Override
	public String toString() {
		return "ValueType<" + type.getName() + ">";
	}
	
	
	static final <X extends Value> TypeInformation<X> getValueTypeInfo(Class<X> typeClass) {
		if (Value.class.isAssignableFrom(typeClass)) {
			return new ValueTypeInfo<X>(typeClass);
		}
		else {
			throw new IllegalArgumentException("The given class is no subclass of " + Value.class.getName());
		}
	}
}
