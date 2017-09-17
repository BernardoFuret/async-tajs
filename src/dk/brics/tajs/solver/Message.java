/*
 * Copyright 2009-2017 Aarhus University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dk.brics.tajs.solver;

import dk.brics.tajs.flowgraph.AbstractNode;
import org.apache.log4j.Logger;

import javax.annotation.Nonnull;

/**
 * Message generated by the analysis.
 */
public class Message implements Comparable<Message> {

    private static Logger log = Logger.getLogger(Message.class);

    /**
     * Message kind.
     */
    public enum Status {

        /**
         * The situation <i>definitely</i> occurs in all executions of the instruction.
         */
        CERTAIN("definite"),

        /**
         * The situation <i>may</i> occur in some executions of the instruction.
         */
        MAYBE("maybe"),

        /**
         * Informational.
         */
        INFO("info"),

        /**
         * The situation <i>does not</i> occur in any execution of the instruction.
         */
        NONE("none");

        private final String presentation;

        Status(String presentation) {
            this.presentation = presentation;
        }

        @Override
        public String toString() {
            return presentation;
        }
    }

    /**
     * Severity level.
     */
    public enum Severity { // order of declarations in this enum determines their sorting order

        /**
         * Error or missing feature in TAJS that does not require the analysis to throw an exception immediately.
         */
        TAJS_ERROR,

        /**
         * Important, an runtime error is generated if this situation occurs.
         */
        HIGH,

        /**
         * Likely an error if this situation occurs, but only report if status is 'certain'.
         */
        MEDIUM_IF_CERTAIN_NONE_OTHERWISE,

        /**
         * Likely an error if this situation occurs.
         */
        MEDIUM,

        /**
         * Probably not an error if this situation occurs.
         */
        LOW,

        /**
         * Information about TAJS internal behavior.
         */
        TAJS_META,

        /**
         * Information about known use of unsoundness.
         */
        TAJS_UNSOUNDNESS
    }

    private AbstractNode node;

    private String msg;

    private String key;

    private Status status;

    private Severity severity;

    private boolean use_source_location; // if true, use source location instead of node in equals/hashCode

    /**
     * Constructs a new message.
     *
     * @param node     flow graph node
     * @param status   message status
     * @param key      message key for comparisons
     * @param msg      the message
     * @param severity severity level
     */
    public Message(AbstractNode node, Status status, String key, String msg, Severity severity) {
        this(node, status, key, msg, severity, false);
    }

    /**
     * Constructs a new message.
     *
     * @param node                flow graph node
     * @param status              message status
     * @param msg                 the message, also used as key
     * @param severity            severity level
     * @param use_source_location if set, use node source location instead of node identity in equals/hashCode
     */
    public Message(AbstractNode node, Status status, String msg, Severity severity, boolean use_source_location) {
        this(node, status, msg, msg, severity, use_source_location);
    }

    /**
     * Constructs a new message.
     *
     * @param node                flow graph node
     * @param status              message status
     * @param key                 message key for comparisons
     * @param msg                 the message
     * @param severity            severity level
     * @param use_source_location if set, use node source location instead of node identity in equals/hashCode
     */
    public Message(AbstractNode node, Status status, String key, String msg, Severity severity, boolean use_source_location) {
        this.node = node;
        this.key = key;
        this.msg = msg;
        this.status = status;
        this.severity = severity;
        this.use_source_location = use_source_location;
    }

    /**
     * Returns a hash code for this message.
     * Does not consider the status.
     */
    @Override
    public int hashCode() {
        return (use_source_location ? node.getSourceLocation().hashCode() : node.getIndex()) * 7 + key.hashCode() * 5;
    }

    /**
     * Checks whether this message is equal to the given.
     * Does not consider the status.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Message other = (Message) obj;
        if (use_source_location ? (!node.getSourceLocation().equals(other.node.getSourceLocation())) : (node != other.node))
            return false;
        if (!key.equals(other.key))
            return false;
        return severity == other.severity;
    }

    /**
     * Returns the node associated with this message.
     */
    public AbstractNode getNode() {
        return node;
    }

    /**
     * Returns the message text.
     */
    public String getMessage() {
        return msg;
    }

    /**
     * Returns the message status.
     */
    public Status getStatus() {
        return status;
    }

    /**
     * Returns the message severity level.
     */
    public Severity getSeverity() {
        return severity;
    }

    /**
     * Returns a string description of this message.
     */
    @Override
    public String toString() {
        String bracketText;
        if (severity == Severity.TAJS_UNSOUNDNESS) {
            bracketText = "unsound";
        } else {
            bracketText = status + "";
        }
        return String.format("%s: [%s] %s", node.getSourceLocation(), bracketText, msg);
    }

    /**
     * Joins the given message into this message.
     */
    public void join(Message other) {
        Status other_status = other.getStatus();
        if (other_status == Status.MAYBE
                || (other_status == Status.CERTAIN && status == Status.NONE)
                || (other_status == Status.NONE && status == Status.CERTAIN))
            status = Status.MAYBE;
        else if (status == Status.INFO)
            status = other_status;
        String other_msg = other.getMessage();
        if (other_msg.length() > msg.length()) // messages are the same in most cases (not for uncaught exception messages)
            msg = other_msg; // pick the longer one
    }

    /**
     * Compares this and the given message.
     * The status (CERTAIN, MAYBE, INFO, NONE) is used as primary key,
     * the severity level (HIGH, MEDIUM, LOW) is secondary,
     * then source location, and finally the message text.
     */
    @Override
    public int compareTo(@Nonnull Message e) {
        int c = status.ordinal() - e.status.ordinal();
        if (c != 0)
            return c;
        c = severity.ordinal() - e.severity.ordinal();
        if (c != 0)
            return c;
        c = node.getSourceLocation().compareTo(e.node.getSourceLocation());
        if (c != 0)
            return c;
        return key.compareTo(e.key);
    }

    /**
     * Prints this message to the log.
     */
    public void emit() {
        log.info(this);
    }
}
