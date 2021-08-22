/**
 * MIT License
 * 
 * Copyright (c) 2021 Jeff Sember
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * 
 **/
package js.data;

import static js.base.Tools.*;

import java.lang.reflect.Field;

import js.base.BaseObject;
import js.json.JSMap;

public final class DataAccessor extends BaseObject implements Accessor {

  DataAccessor(AbstractData abstractBuilder, String fieldName) {
    mFieldName = fieldName;
    mAbstractData = abstractBuilder;
    String internalName = "m" + DataUtil.convertUnderscoresToCamelCase(fieldName);

    try {
      // We only support accessors for builders, to maintain the rule that non-builder objects
      // are immutable.
      // TODO: We could allow mutation of immutable data objects, as long as we clear the cached 
      //  hash code with every write 
      Class immutableClass = abstractBuilder.getClass().getSuperclass();
      mField = immutableClass.getDeclaredField(internalName);
      mField.setAccessible(true);
    } catch (NoSuchFieldException e) {
      throw badArgWithCause(e, "Could not find field", internalName, "for AbstractData",
          abstractBuilder.getClass());
    } catch (Throwable t) {
      throw asRuntimeException(t);
    }
  }

  @Override
  protected String supplyName() {
    return fieldName();
  }

  @Override
  public JSMap toJson() {
    return map().put("name", fieldName()).putUnsafe("value", get());
  }

  public String fieldName() {
    return mFieldName;
  }

  public Class dataType() {
    return mField.getType();
  }

  public AbstractData state() {
    return mAbstractData;
  }

  // ------------------------------------------------------------------
  // AbstractAccessor implementation
  // ------------------------------------------------------------------

  @Override
  public Object get() {
    try {
      Object result = mField.get(mAbstractData);
      log("get:", result);
      return result;
    } catch (Throwable t) {
      throw asRuntimeException(t);
    }
  }

  @Override
  public void set(Object value) {
    log("set to:", value);
    try {
      mField.set(mAbstractData, value);
    } catch (Throwable t) {
      throw asRuntimeException(t);
    }
  }

  //------------------------------------------------------------------

  private final Field mField;
  private final AbstractData mAbstractData;
  private final String mFieldName;
}
