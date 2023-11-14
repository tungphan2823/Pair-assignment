/*
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
package org.apache.maven.doxia.index;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.maven.doxia.sink.impl.SinkAdapter;
import org.apache.maven.doxia.util.DoxiaUtils;

/**
 * A sink implementation for index.
 *
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @author <a href="mailto:vincent.siveton@gmail.com">Vincent Siveton</a>
 */
public class IndexingSink extends SinkAdapter {
    /** Section 1. */
    private static final int TYPE_SECTION_1 = 1;

    /** Section 2. */
    private static final int TYPE_SECTION_2 = 2;

    /** Section 3. */
    private static final int TYPE_SECTION_3 = 3;

    /** Section 4. */
    private static final int TYPE_SECTION_4 = 4;

    /** Section 5. */
    private static final int TYPE_SECTION_5 = 5;

    /** Defined term. */
    private static final int TYPE_DEFINED_TERM = 6;

    /** Figure. */
    private static final int TYPE_FIGURE = 7;

    /** Table. */
    private static final int TYPE_TABLE = 8;

    /** Title. */
    private static final int TITLE = 9;

    /** The current type. */
    private int type;

    /** The current title. */
    private String title;

    /** The stack. */
    private final Stack<IndexEntry> stack;

    /** A map containing all used ids of index entries as key and how often they are used as value
     * (0-based, i.e. 0 means used 1 time). {@link AtomicInteger} is only used here as it implements
     * a mutable integer (not for its atomicity).
     */
    private final Map<String, AtomicInteger> usedIds;

    /**
     * Default constructor.
     *
     * @param sectionEntry The first index entry.
     */
    public IndexingSink(IndexEntry sectionEntry) {
        stack = new Stack<>();
        stack.push(sectionEntry);
        usedIds = new HashMap<>();
        usedIds.put(sectionEntry.getId(), new AtomicInteger());
        init();
    }

    /**
     * <p>Getter for the field <code>title</code>.</p>
     *
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    // ----------------------------------------------------------------------
    // Sink Overrides
    // ----------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    public void title() {
        this.type = TITLE;
    }

    /**
     * {@inheritDoc}
     */
    public void section1() {
        pushNewEntry();
    }

    /**
     * {@inheritDoc}
     */
    public void sectionTitle1() {
        this.type = TYPE_SECTION_1;
    }

    /**
     * {@inheritDoc}
     */
    public void title_() {
        this.type = 0;
    }

    /**
     * <p>sectionTitle1_.</p>
     */
    public void sectionTitle1_() {
        this.type = 0;
    }

    /**
     * {@inheritDoc}
     */
    public void section1_() {
        pop();
    }

    /**
     * {@inheritDoc}
     */
    public void section2() {
        pushNewEntry();
    }

    /**
     * {@inheritDoc}
     */
    public void sectionTitle2() {
        this.type = TYPE_SECTION_2;
    }

    /**
     * <p>sectionTitle2_.</p>
     */
    public void sectionTitle2_() {
        this.type = 0;
    }

    /**
     * {@inheritDoc}
     */
    public void section2_() {
        pop();
    }

    /**
     * {@inheritDoc}
     */
    public void section3() {
        pushNewEntry();
    }

    /**
     * {@inheritDoc}
     */
    public void sectionTitle3() {
        this.type = TYPE_SECTION_3;
    }

    /**
     * <p>sectionTitle3_.</p>
     */
    public void sectionTitle3_() {
        this.type = 0;
    }

    /**
     * {@inheritDoc}
     */
    public void section3_() {
        pop();
    }

    /**
     * {@inheritDoc}
     */
    public void section4() {
        pushNewEntry();
    }

    /**
     * {@inheritDoc}
     */
    public void sectionTitle4() {
        this.type = TYPE_SECTION_4;
    }

    /**
     * <p>sectionTitle4_.</p>
     */
    public void sectionTitle4_() {
        this.type = 0;
    }

    /**
     * {@inheritDoc}
     */
    public void section4_() {
        pop();
    }

    /**
     * {@inheritDoc}
     */
    public void section5() {
        pushNewEntry();
    }

    /**
     * {@inheritDoc}
     */
    public void sectionTitle5() {
        this.type = TYPE_SECTION_5;
    }

    /**
     * <p>sectionTitle5_.</p>
     */
    public void sectionTitle5_() {
        this.type = 0;
    }

    /**
     * {@inheritDoc}
     */
    public void section5_() {
        pop();
    }

    // public void definedTerm()
    // {
    // type = TYPE_DEFINED_TERM;
    // }
    //
    // public void figureCaption()
    // {
    // type = TYPE_FIGURE;
    // }
    //
    // public void tableCaption()
    // {
    // type = TYPE_TABLE;
    // }

    /** {@inheritDoc} */
    public void text(String text) {
        switch (this.type) {
            case TITLE:
                this.title = text;
                break;
            case TYPE_SECTION_1:
            case TYPE_SECTION_2:
            case TYPE_SECTION_3:
            case TYPE_SECTION_4:
            case TYPE_SECTION_5:
                // -----------------------------------------------------------------------
                // Sanitize the id. The most important step is to remove any blanks
                // -----------------------------------------------------------------------

                // append text to current entry
                IndexEntry entry = stack.lastElement();

                String title = entry.getTitle() + text;
                title = title.replaceAll("[\\r\\n]+", "");
                entry.setTitle(title);

                entry.setId(getUniqueId(DoxiaUtils.encodeId(title)));

                break;
                // Dunno how to handle these yet
            case TYPE_DEFINED_TERM:
            case TYPE_FIGURE:
            case TYPE_TABLE:
            default:
                break;
        }
    }

    /**
     * Converts the given id into a unique one by potentially suffixing it with an index value.
     *
     * @param id
     * @return the unique id
     */
    String getUniqueId(String id) {
        final String uniqueId;

        if (usedIds.containsKey(id)) {
            uniqueId = id + "_" + usedIds.get(id).incrementAndGet();
        } else {
            usedIds.put(id, new AtomicInteger());
            uniqueId = id;
        }
        return uniqueId;
    }

    /**
     * Creates and pushes a new IndexEntry onto the top of this stack.
     */
    public void pushNewEntry() {
        IndexEntry entry = new IndexEntry(peek(), "");

        entry.setTitle("");

        stack.push(entry);
    }

    /**
     * Pushes an IndexEntry onto the top of this stack.
     *
     * @param entry to put.
     */
    public void push(IndexEntry entry) {
        stack.push(entry);
    }

    /**
     * Removes the IndexEntry at the top of this stack.
     */
    public void pop() {
        stack.pop();
    }

    /**
     * <p>peek.</p>
     *
     * @return Looks at the IndexEntry at the top of this stack.
     */
    public IndexEntry peek() {
        return stack.peek();
    }

    /**
     * {@inheritDoc}
     */
    public void close() {
        super.close();

        init();
    }

    /**
     * {@inheritDoc}
     */
    protected void init() {
        this.type = 0;
        this.title = null;
    }
}
