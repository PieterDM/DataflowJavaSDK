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

package com.google.cloud.dataflow.sdk.transforms.windowing;

import org.joda.time.Instant;

import java.util.Arrays;

/**
 * Repeat a trigger, either until some condition is met or forever.
 *
 * <p>For example, to fire after the end of the window, and every time late data arrives:
 * <pre> {@code
 *     Repeatedly.forever(AfterWatermark.isPastEndOfWindow());
 * } </pre>
 *
 * <p>{@code Repeatedly.forever(someTrigger)} behaves like the infinite
 * {@code SequenceOf(someTrigger, someTrigger, someTrigger, ...)}.
 *
 * @param <W> {@link BoundedWindow} subclass used to represent the windows used by this
 *            {@code Trigger}
 */
public class Repeatedly<W extends BoundedWindow> extends Trigger<W> {

  private static final long serialVersionUID = 0L;

  private static final int REPEATED = 0;

  /**
   * Create a composite trigger that repeatedly executes the trigger {@code toRepeat}, firing each
   * time it fires and ignoring any indications to finish.
   *
   * <p>Unless used with {@link Trigger#orFinally} the composite trigger will never finish.
   *
   * @param repeated the trigger to execute repeatedly.
   */
  public static <W extends BoundedWindow> Repeatedly<W> forever(Trigger<W> repeated) {
    return new Repeatedly<W>(repeated);
  }

  private Repeatedly(Trigger<W> repeated) {
    super(Arrays.asList(repeated));
  }


  @Override
  public TriggerResult onElement(TriggerContext<W> c, OnElementEvent<W> e)
      throws Exception {
    TriggerResult result = c.subTrigger(REPEATED).invokeElement(c, e);
    if (result.isFinish()) {
      c.forTrigger(c.subTrigger(REPEATED)).resetTree(e.window());
    }
    return result.isFire() ? TriggerResult.FIRE : TriggerResult.CONTINUE;
  }

  @Override
  public MergeResult onMerge(TriggerContext<W> c, OnMergeEvent<W> e) throws Exception {
    MergeResult mergeResult = c.subTrigger(REPEATED).invokeMerge(c, e);
    if (mergeResult.isFinish()) {
      c.forTrigger(c.subTrigger(REPEATED)).resetTree(e.newWindow());
    }
    return mergeResult.isFire() ? MergeResult.FIRE : MergeResult.CONTINUE;
  }

  @Override
  public TriggerResult onTimer(TriggerContext<W> c, OnTimerEvent<W> e) throws Exception {
    TriggerResult result = c.subTrigger(REPEATED).invokeTimer(c, e);
    if (result.isFinish()) {
      c.forTrigger(c.subTrigger(REPEATED)).resetTree(e.window());
    }
    return result.isFire() ? TriggerResult.FIRE : TriggerResult.CONTINUE;
  }

  @Override
  public Instant getWatermarkCutoff(W window) {
    // This trigger fires once the repeated trigger fires.
    return subTriggers.get(REPEATED).getWatermarkCutoff(window);
  }
}
