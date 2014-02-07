/***********************************************************************************************************************
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
 **********************************************************************************************************************/

package eu.stratosphere.api.common.io;

import java.io.IOException;
import java.util.ArrayList;

import eu.stratosphere.core.fs.FileInputSplit;
import eu.stratosphere.core.fs.Path;
import eu.stratosphere.types.parser.FieldParser;
import eu.stratosphere.util.InstantiationUtil;


public abstract class GenericCsvInputFormat<OT> extends DelimitedInputFormat<OT> {
	
	private static final long serialVersionUID = 1L;
	
	private static final Class<?>[] EMPTY_TYPES = new Class[0];
	
	private static final boolean[] EMPTY_INCLUDED = new boolean[0];
	
	private static final char DEFAULT_FIELD_DELIMITER = ',';
	
	
	// --------------------------------------------------------------------------------------------
	//  Variables for internal operation.
	//  They are all transient, because we do not want them so be serialized 
	// --------------------------------------------------------------------------------------------

	private transient FieldParser<Object>[] fieldParsers;
	
	
	// --------------------------------------------------------------------------------------------
	//  The configuration parameters. Configured on the instance and serialized to be shipped.
	// --------------------------------------------------------------------------------------------
	
	private Class<?>[] fieldTypes = EMPTY_TYPES;
	
	private boolean[] fieldIncluded = EMPTY_INCLUDED;
		
	private char fieldDelim = DEFAULT_FIELD_DELIMITER;
	
	private boolean lenient = false;
	
	
	// --------------------------------------------------------------------------------------------
	//  Constructors and getters/setters for the configurable parameters
	// --------------------------------------------------------------------------------------------
	
	protected GenericCsvInputFormat() {
		super();
	}
	
	protected GenericCsvInputFormat(Path filePath) {
		super(filePath);
	}
	
	// --------------------------------------------------------------------------------------------

	public int getNumberOfFieldsTotal() {
		return this.fieldIncluded.length;
	}
	
	public int getNumberOfNonNullFields() {
		return this.fieldTypes.length;
	}

	public char getFieldDelim() {
		return fieldDelim;
	}

	public void setFieldDelim(char fieldDelim) {
		if (fieldDelim > Byte.MAX_VALUE)
			throw new IllegalArgumentException("The field delimiter must be an ASCII character.");
		
		this.fieldDelim = fieldDelim;
	}

	public boolean isLenient() {
		return lenient;
	}

	public void setLenient(boolean lenient) {
		this.lenient = lenient;
	}
	
	// --------------------------------------------------------------------------------------------
	
	protected FieldParser<?>[] getFieldParsers() {
		return this.fieldParsers;
	}
	
	protected Class<?>[] getGenericFieldTypes() {
		// check if we are dense, i.e., we read all fields
		if (this.fieldIncluded.length == this.fieldTypes.length) {
			return this.fieldTypes;
		}
		else {
			// sparse type array which we made dense for internal book keeping.
			// create a sparse copy to return
			Class<?>[] types = new Class<?>[this.fieldIncluded.length];
			
			for (int i = 0, k = 0; i < this.fieldIncluded.length; i++) {
				if (this.fieldIncluded[i]) {
					types[i] = this.fieldTypes[k++];
				}
			}
			
			return types;
		}
	}
	
	
	protected void setFieldTypes(Class<?> ... fieldTypes) {
		if (fieldTypes == null)
			throw new IllegalArgumentException("Field types must not be null.");
		
		this.fieldIncluded = new boolean[fieldTypes.length];
		ArrayList<Class<?>> types = new ArrayList<Class<?>>();
		
		// check if we support parsers for these types
		for (int i = 0; i < fieldTypes.length; i++) {
			Class<?> type = fieldTypes[i];
			
			if (type != null) {
				if (FieldParser.getParserForType(type) == null) {
					throw new IllegalArgumentException("The type '" + type.getName() + "' is not supported for the CSV input format.");
				}
				types.add(type);
				fieldIncluded[i] = true;
			}
		}
		
		Class<?>[] denseTypeArray = (Class<?>[]) types.toArray(new Class[types.size()]);
		this.fieldTypes = denseTypeArray;
	}

	// --------------------------------------------------------------------------------------------
	//  Runtime methods
	// --------------------------------------------------------------------------------------------
	
	@Override
	public void open(FileInputSplit split) throws IOException {
		super.open(split);
		
		// instantiate the parsers
		@SuppressWarnings("unchecked")
		FieldParser<Object>[] parsers = new FieldParser[fieldTypes.length];
		
		for (int i = 0; i < fieldTypes.length; i++) {
			if (fieldTypes[i] != null) {
				Class<? extends FieldParser<?>> parserType = FieldParser.getParserForType(fieldTypes[i]);
				if (parserType == null) {
					throw new RuntimeException("No parser available for type '" + fieldTypes[i].getName() + "'.");
				}
				

				@SuppressWarnings("unchecked")
				FieldParser<Object> p = (FieldParser<Object>) InstantiationUtil.instantiate(parserType, FieldParser.class);
				parsers[i] = p;
			}
		}
		this.fieldParsers = parsers;
	}
	
	protected boolean parseRecord(Object[] holders, byte[] bytes, int offset, int numBytes) throws ParseException {
		
		boolean[] fieldIncluded = this.fieldIncluded;
		
		int startPos = offset;
		final int limit = offset + numBytes;
		
		for (int field = 0, output = 0; field < fieldIncluded.length; field++) {
			
			// check valid start position
			if (startPos >= limit) {
				if (lenient) {
					return false;
				} else {
					throw new ParseException("Row too short: " + new String(bytes, offset, numBytes));
				}
			}
			
			if (fieldIncluded[field]) {
				// parse field
				FieldParser<Object> parser = (FieldParser<Object>) this.fieldParsers[output];
				Object reuse = holders[output];
				startPos = parser.parseField(bytes, startPos, limit, this.fieldDelim, reuse);
				holders[output] = parser.getLastResult();
				
				// check parse result
				if (startPos < 0) {
					// no good
					if (lenient) {
						return false;
					} else {
						String lineAsString = new String(bytes, offset, numBytes);
						throw new ParseException("Line could not be parsed: " + lineAsString);
					}
				}
				output++;
			}
			else {
				// skip field
				startPos = skipFields(bytes, startPos, limit, fieldDelim);
				if (startPos < 0) {
					String lineAsString = new String(bytes, offset, numBytes);
					throw new ParseException("Line could not be parsed: " + lineAsString);
				}
			}
		}
		return true;
	}
	
	protected int skipFields(byte[] bytes, int startPos, int limit, char delim) {
		int i = startPos;
		
		final byte delByte = (byte) delim;
		byte current;
		
		// skip over initial whitespace lines
		while (i < limit && ((current = bytes[i]) == ' ' || current == '\t')) {
			i++;
		}
		
		// first none whitespace character
		if (i < limit && bytes[i] == '"') {
			// quoted string
			i++; // the quote
			
			while (i < limit && bytes[i] != '"') {
				i++;
			}
			
			if (i < limit) {
				// end of the quoted field
				i++; // the quote
				
				// skip trailing whitespace characters 
				while (i < limit && (current = bytes[i]) != delByte) {
					if (current == ' ' || current == '\t')
						i++;
					else
						return -1;	// illegal case of non-whitespace characters trailing
				}
				
				return (i == limit ? limit : i+1);
			} else {
				// exited due to line end without quote termination
				return -1;
			}
		}
		else {
			// unquoted field
			while (i < limit && bytes[i] != delByte) {
				i++;
			}
			return (i == limit ? limit : i+1);
		}
	}
}