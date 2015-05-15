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

import com.google.cloud.dataflow.sdk.coders.Proto2CoderTestMessages.MessageA;
import com.google.cloud.dataflow.sdk.coders.Proto2CoderTestMessages.MessageB;
import com.google.cloud.dataflow.sdk.coders.Proto2CoderTestMessages.MessageC;
import com.google.cloud.dataflow.sdk.testing.CoderProperties;
import com.google.common.collect.ImmutableList;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for Proto2Coder.
 */
@Ignore("Enable when we fix interal build process")
@RunWith(JUnit4.class)
public class Proto2CoderTest {

  @Test
  public void testCoderEncodeDecodeEqual() throws Exception {
    MessageA value = MessageA.newBuilder()
        .setField1("hello")
        .addField2(MessageB.newBuilder()
            .setField1(true).build())
        .addField2(MessageB.newBuilder()
            .setField1(false).build())
        .build();
    CoderProperties.coderDecodeEncodeEqual(Proto2Coder.of(MessageA.class), value);
  }

  @Test
  public void testCoderEncodeDecodeEqualNestedContext() throws Exception {
    MessageA value1 = MessageA.newBuilder()
        .setField1("hello")
        .addField2(MessageB.newBuilder()
            .setField1(true).build())
        .addField2(MessageB.newBuilder()
            .setField1(false).build())
        .build();
    MessageA value2 = MessageA.newBuilder()
        .setField1("world")
        .addField2(MessageB.newBuilder()
            .setField1(false).build())
        .addField2(MessageB.newBuilder()
            .setField1(true).build())
        .build();
    CoderProperties.coderDecodeEncodeEqual(ListCoder.of(Proto2Coder.of(MessageA.class)),
        ImmutableList.of(value1, value2));
  }

  @Test
  public void testCoderEncodeDecodeExtensionsEqual() throws Exception {
    MessageC value = MessageC.newBuilder()
        .setExtension(Proto2CoderTestMessages.field1,
            MessageA.newBuilder()
            .setField1("hello")
            .addField2(MessageB.newBuilder()
                .setField1(true)
                .build())
            .build())
        .setExtension(Proto2CoderTestMessages.field2,
            MessageB.newBuilder()
            .setField1(false)
            .build())
        .build();
    CoderProperties.coderDecodeEncodeEqual(
        Proto2Coder.of(MessageC.class)
        .addExtensionsFrom(Proto2CoderTestMessages.class), value);
  }
}
