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

package com.google.cloud.dataflow.sdk.values;

import com.google.cloud.dataflow.sdk.Pipeline;
import com.google.cloud.dataflow.sdk.transforms.AppliedPTransform;
import com.google.cloud.dataflow.sdk.util.StringUtils;

import java.util.Collection;
import java.util.Collections;

/**
 * A {@code PValueBase} is an abstract base class that provides
 * sensible default implementations for methods of {@link PValue}.
 * In particular, this includes functionality for getting/setting:
 *
 * <ul>
 * <li> The {@code Pipeline} that the {@code PValue} is
 * part of.
 * <li> Whether the {@code PValue} has bee finalized (as an input
 * or an output), after which its properties can
 * no longer be changed.
 * </ul>
 *
 * <p> For internal use.
 */
public abstract class PValueBase extends POutputValueBase implements PValue {
  /**
   * Returns the name of this {@code PValueBase}.
   *
   * <p> By default, the name of a {@code PValueBase} is based on the
   * name of the {@code PTransform} that produces it.  It can be
   * specified explicitly by calling {@link #setName}.
   *
   * @throws IllegalStateException if the name hasn't been set yet
   */
  @Override
  public String getName() {
    if (name == null) {
      throw new IllegalStateException("name not set");
    }
    return name;
  }

  /**
   * Sets the name of this {@code PValueBase}.  Returns {@code this}.
   *
   * @throws IllegalStateException if this {@code PValueBase} has
   * already been finalized and is no longer settable, e.g., by having
   * {@code apply()} called on it
   */
  public PValueBase setName(String name) {
    if (finishedSpecifying) {
      throw new IllegalStateException(
          "cannot change the name of " + this + " once it's been used");
    }
    this.name = name;
    return this;
  }

  /////////////////////////////////////////////////////////////////////////////

  protected PValueBase(Pipeline pipeline) {
    super(pipeline);
  }

  /**
   * No-arg constructor for Java serialization only.
   * The resulting {@code PValueBase} is unlikely to be
   * valid.
   */
  protected PValueBase() {
    super();
  }

  /**
   * The name of this {@code PValueBase}, or null if not yet set.
   */
  private String name;

  /**
   * Whether this {@code PValueBase} has been finalized, and its core
   * properties, e.g., name, can no longer be changed.
   */
  private boolean finishedSpecifying = false;

  @Override
  public void recordAsOutput(AppliedPTransform<?, ?, ?> transform) {
    recordAsOutput(transform, "out");
  }

  /**
   * Records that this {@code POutputValueBase} is an output with the
   * given name of the given {@code AppliedPTransform} in the given
   * {@code Pipeline}.
   *
   * <p> To be invoked only by {@link POutput#recordAsOutput}
   * implementations.  Not to be invoked directly by user code.
   */
  protected void recordAsOutput(AppliedPTransform<?, ?, ?> transform,
                                String outName) {
    super.recordAsOutput(transform);
    if (name == null) {
      name = transform.getFullName() + "." + outName;
    }
  }

  /**
   * Returns whether this {@code PValueBase} has been finalized, and
   * its core properties, e.g., name, can no longer be changed.
   *
   * <p> For internal use only.
   */
  public boolean isFinishedSpecifyingInternal() {
    return finishedSpecifying;
  }

  @Override
  public Collection<? extends PValue> expand() {
    return Collections.singletonList(this);
  }

  @Override
  public void finishSpecifying() {
    finishSpecifyingOutput();
    finishedSpecifying = true;
  }

  @Override
  public String toString() {
    return (name == null ? "<unnamed>" : getName())
        + " [" + getKindString() + "]";
  }

  /**
   * Returns a {@code String} capturing the kind of this
   * {@code PValueBase}.
   *
   * <p> By default, uses the base name of this {@code PValueBase}'s
   * class as its kind string.
   */
  protected String getKindString() {
    return StringUtils.approximateSimpleName(getClass());
  }
}
