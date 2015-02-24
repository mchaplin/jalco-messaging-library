/*
 * Copyright 2014 matthieu.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.sfr.tv.messaging.impl;

/**
 * Encapsulate a concrete messaging listener/handler.
 * 
 * @author matthieu
 * @param <T>
 */
public class ConsumerWrapper<T> {
    
    protected final T wrapped;
    
    public ConsumerWrapper(final T wrapped) {
        this.wrapped = wrapped;
    }
    
    public T getWrapped() {
        return wrapped;
    }
    

    public void release() {}
}
