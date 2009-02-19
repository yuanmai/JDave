/*
 * Copyright 2006 the original author or authors.
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
package jdave.containment;

import java.util.Collection;
import java.util.Iterator;
import jdave.IContainment;
import jdave.util.Collections;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

/**
 * @author Joni Freeman
 */
public abstract class CollectionContainment<T> extends BaseMatcher<Collection<T>> implements
        IContainment<T> {
    protected final Collection<T> elements;
    private Collection<T> actual;

    public CollectionContainment(final Collection<T> elements) {
        this.elements = elements;
    }

    public CollectionContainment(final Iterator<T> elements) {
        this(Collections.list(elements));
    }

    public CollectionContainment(final Iterable<T> elements) {
        this(elements.iterator());
    }

    public boolean matches(final Object item) {
        if (item instanceof Collection<?>) {
            return matches((Collection<T>) item);
        }
        return false;
    }

    public boolean matches(final Collection<T> actual) {
        if (actual == null) {
            return false;
        }
        this.actual = actual;
        return nullSafeMatches(actual);
    }

    protected abstract boolean nullSafeMatches(Collection<T> actual);

    public void describeTo(final Description description) {
        description.appendText(error(actual));
    }

    @Override
    public String toString() {
        return elements.toString();
    }

    public String error(final Collection<T> actual) {
        return "The specified collection '" + actual + "' does not contain '" + this + "'";
    }
}
