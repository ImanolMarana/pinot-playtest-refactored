/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.pinot.spi.stream;

import java.io.Closeable;
import java.util.concurrent.TimeoutException;
import javax.annotation.Nullable;


/**
 * Consumer interface for consuming from a partition group of a stream
 */
public interface PartitionGroupConsumer extends Closeable {
  /**
   * Starts a stream consumer
   *
   * This is useful in cases where starting the consumer involves preparing / initializing the source.
   * A typical example is that of an asynchronous / non-poll based consumption model where this method will be used to
   * setup or initialize the consumer to fetch messages from the source stream.
   *
   * Poll-based consumers can optionally use this to prefetch metadata from the source.
   *
   * This method should be invoked by the caller before trying to invoke
   * {@link #fetchMessages(StreamPartitionMsgOffset, int)}.
   *
   * @param startOffset Offset (inclusive) at which the consumption should begin
   */
  default void start(StreamPartitionMsgOffset startOffset) {
  }

  /**
   * Fetches messages from the stream partition from the given start offset within the specified timeout.
   *
   * This method should return within the timeout. If there is no message available before time runs out, an empty
   * message batch should be returned.
   *
   * @param startOffset The offset of the first message desired, inclusive
   * @param timeoutMs Timeout in milliseconds
   * @throws TimeoutException If the operation could not be completed within timeout
   * @return A batch of messages from the stream partition group
   */
  default MessageBatch fetchMessages(StreamPartitionMsgOffset startOffset, int timeoutMs)
      throws TimeoutException {
    return fetchMessages(startOffset, null, timeoutMs);
  }

  // Deprecated because the offset is not always monotonically increasing
  @Deprecated
  default MessageBatch fetchMessages(StreamPartitionMsgOffset startOffset, @Nullable StreamPartitionMsgOffset endOffset,
      int timeoutMs)
      throws TimeoutException {
    throw new UnsupportedOperationException();
  }

  /**
   * Checkpoints the consumption state of the stream partition group in the source
   *
   * This is useful in streaming systems that require preserving consumption state on the source in order to resume or
   * replay consumption of data. The consumer implementation is responsible for managing this state.
   *
   * The offset returned will be used for offset comparisons within the local server (say, for catching up) and also,
   * persisted to the ZK segment metadata. Hence, the returned value should be same or equivalent to the lastOffset
   * provided as input (that is, compareTo of the input and returned offset should be 0).
   *
   * @param lastOffset checkpoint the stream at this offset (exclusive)
   * @return Returns the offset that should be used as the next upcoming offset for the stream partition group
   */
  default StreamPartitionMsgOffset checkpoint(StreamPartitionMsgOffset lastOffset) {
    return lastOffset;
  }
}
