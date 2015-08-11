/*
 * Copyright (C) 2015 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.cloud.dataflow.sdk.coders;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;

import com.google.api.client.util.Preconditions;
import com.google.cloud.dataflow.sdk.values.TypeDescriptor;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.Serializable;
import java.util.List;

/**
 * Tests of Coder defaults.
 */
@RunWith(JUnit4.class)
@SuppressWarnings("serial")
public class DefaultCoderTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  public CoderRegistry registry = new CoderRegistry();

  @Before
  public void registerStandardCoders() {
    registry.registerStandardCoders();
  }

  @DefaultCoder(AvroCoder.class)
  private static class AvroRecord {
  }

  private static class SerializableBase implements Serializable {
  }

  @DefaultCoder(SerializableCoder.class)
  private static class SerializableRecord extends SerializableBase {
  }

  @DefaultCoder(CustomSerializableCoder.class)
  private static class CustomRecord extends SerializableBase {
  }

  private static class Unknown {
  }

  private static class CustomSerializableCoder extends SerializableCoder<CustomRecord> {
    // Extending SerializableCoder isn't trivial, but it can be done.
    @SuppressWarnings("unchecked")
    public static <T extends Serializable> SerializableCoder<T> of(Class<T> recordType) {
       Preconditions.checkArgument(
           CustomRecord.class.isAssignableFrom(recordType));
       return (SerializableCoder<T>) new CustomSerializableCoder();
    }

    protected CustomSerializableCoder() {
      super(CustomRecord.class);
    }
  }

  @Test
  public void testDefaultCoderClasses() throws Exception {
    assertThat(registry.getDefaultCoder(AvroRecord.class), instanceOf(AvroCoder.class));
    assertThat(registry.getDefaultCoder(SerializableBase.class),
        instanceOf(SerializableCoder.class));
    assertThat(registry.getDefaultCoder(SerializableRecord.class),
        instanceOf(SerializableCoder.class));
    assertThat(registry.getDefaultCoder(CustomRecord.class),
        instanceOf(CustomSerializableCoder.class));
  }

  @Test
  public void testDefaultCoderInCollection() throws Exception {
    assertThat(registry.getDefaultCoder(new TypeDescriptor<List<AvroRecord>>(){}),
        instanceOf(ListCoder.class));
    assertThat(registry.getDefaultCoder(new TypeDescriptor<List<SerializableRecord>>(){}),
        equalTo((Coder<List<SerializableRecord>>)
            ListCoder.of(SerializableCoder.of(SerializableRecord.class))));
  }

  @Test
  public void testUnknown() throws Exception {
    thrown.expect(CannotProvideCoderException.class);
    registry.getDefaultCoder(Unknown.class);
  }
}
