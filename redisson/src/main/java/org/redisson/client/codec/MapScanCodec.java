/**
 * Copyright 2018 Nikita Koksharov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.redisson.client.codec;

import java.io.IOException;

import org.redisson.client.handler.State;
import org.redisson.client.protocol.Decoder;
import org.redisson.client.protocol.Encoder;
import org.redisson.client.protocol.decoder.ScanObjectEntry;
import org.redisson.misc.Hash;
import org.redisson.misc.HashValue;

import io.netty.buffer.ByteBuf;

/**
 * 
 * @author Nikita Koksharov
 *
 */
public class MapScanCodec implements Codec {

    private final Codec delegate;
    private final Codec mapValueCodec;

    public MapScanCodec(Codec delegate) {
        this(delegate, null);
    }

    public MapScanCodec(Codec delegate, Codec mapValueCodec) {
        this.delegate = delegate;
        this.mapValueCodec = mapValueCodec;
    }

    @Override
    public Decoder<Object> getValueDecoder() {
        return delegate.getValueDecoder();
    }

    @Override
    public Encoder getValueEncoder() {
        return delegate.getValueEncoder();
    }

    @Override
    public Decoder<Object> getMapValueDecoder() {
        return new Decoder<Object>() {
            @Override
            public Object decode(ByteBuf buf, State state) throws IOException {
                buf.markReaderIndex();
                long[] hash = Hash.hash128(buf);
                buf.resetReaderIndex();
                Codec c = delegate;
                if (mapValueCodec != null) {
                    c = mapValueCodec;
                }
                Object val = c.getMapValueDecoder().decode(buf, state);
                return new ScanObjectEntry(new HashValue(hash), val);
            }
        };
    }

    @Override
    public Encoder getMapValueEncoder() {
        Codec c = delegate;
        if (mapValueCodec != null) {
            c = mapValueCodec;
        }

        return c.getMapValueEncoder();
    }

    @Override
    public Decoder<Object> getMapKeyDecoder() {
        return new Decoder<Object>() {
            @Override
            public Object decode(ByteBuf buf, State state) throws IOException {
                buf.markReaderIndex();
                long[] hash = Hash.hash128(buf);
                buf.resetReaderIndex();
                Object val = delegate.getMapKeyDecoder().decode(buf, state);
                return new ScanObjectEntry(new HashValue(hash), val);
            }
        };
    }

    @Override
    public Encoder getMapKeyEncoder() {
        return delegate.getMapKeyEncoder();
    }

    @Override
    public ClassLoader getClassLoader() {
        return getClass().getClassLoader();
    }

}
