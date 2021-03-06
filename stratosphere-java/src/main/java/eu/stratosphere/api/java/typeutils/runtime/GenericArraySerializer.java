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
package eu.stratosphere.api.java.typeutils.runtime;

import java.io.IOException;
import java.lang.reflect.Array;

import eu.stratosphere.api.common.typeutils.Serializer;
import eu.stratosphere.core.memory.DataInputView;
import eu.stratosphere.core.memory.DataOutputView;


/**
 * @param <T> The array type
 * @param <T> The component type
 */
public class GenericArraySerializer<C> extends Serializer<C[]> {

	private static final long serialVersionUID = 1L;

	private final Class<C> componentClass;
	
	private final Serializer<C> componentSerializer;
	
	
	
	public GenericArraySerializer(Class<C> componentClass, Serializer<C> componentSerializer) {
		this.componentClass = componentClass;
		this.componentSerializer = componentSerializer;
	}

	
	@Override
	public C[] createInstance() {
		return create(0);
	}

	@Override
	public C[] copy(C[] from, C[] reuse) {
		if (reuse.length != from.length) {
			reuse = create(from.length);
		}
		
		System.arraycopy(from, 0, reuse, 0, from.length);
		return reuse;
	}

	@Override
	public int getLength() {
		return -1;
	}

	@Override
	public void serialize(C[] value, DataOutputView target) throws IOException {
		target.writeInt(value.length);
		for (int i = 0; i < value.length; i++) {
			C val = value[i];
			if (val == null) {
				target.writeBoolean(false);
			} else {
				target.writeBoolean(true);
				componentSerializer.serialize(val, target);
			}
		}
	}

	@Override
	public C[] deserialize(C[] reuse, DataInputView source) throws IOException {
		int len = source.readInt();
		
		if (reuse.length != len) {
			reuse = create(len);
		}
		
		for (int i = 0; i < len; i++) {
			boolean isNonNull = source.readBoolean();
			if (isNonNull) {
				reuse[i] = componentSerializer.deserialize(componentSerializer.createInstance(), source);
			} else {
				reuse[i] = null;
			}
		}
		
		return reuse;
	}

	@Override
	public void copy(DataInputView source, DataOutputView target) throws IOException {
		int len = source.readInt();
		target.writeInt(len);
		
		for (int i = 0; i < len; i++) {
			boolean isNonNull = source.readBoolean();
			target.writeBoolean(isNonNull);
			
			if (isNonNull) {
				componentSerializer.copy(source, target);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private final C[] create(int len) {
		return (C[]) Array.newInstance(componentClass, len);
	}
}
