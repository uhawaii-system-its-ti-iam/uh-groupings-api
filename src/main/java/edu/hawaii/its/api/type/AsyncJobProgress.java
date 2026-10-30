package edu.hawaii.its.api.type;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * How far an async job, such as a large import, has gotten: the phase it is in, and how many of that phase's UH
 * identifiers Grouper has answered for so far. The UI shows it while it polls the job. A phase's batches can finish
 * on several threads at once, so the counts are updated and read under a lock.
 */
public class AsyncJobProgress {

    public enum Phase {
        /** Looking the identifiers up in Grouper. */
        VALIDATING,
        /** Removing members from the opposite list (Include or Exclude). */
        REMOVING,
        /** Adding members to the list. */
        ADDING
    }

    private Phase phase;
    private int done;
    private int total;

    /**
     * Start phase, in which total identifiers are to be sent to Grouper.
     */
    public synchronized void start(Phase phase, int total) {
        this.phase = phase;
        this.done = 0;
        this.total = total;
    }

    /**
     * Count count more identifiers to be sent to Grouper in the current phase, e.g. identifiers that are looked up a
     * second time.
     */
    public synchronized void addTotal(int count) {
        total += count;
    }

    /**
     * Count count more identifiers of the current phase as answered by Grouper.
     */
    public synchronized void addDone(int count) {
        done += count;
    }

    /**
     * The progress at this moment, or null before the first phase has started.
     */
    public synchronized Snapshot snapshot() {
        return phase == null ? null : new Snapshot(phase, done, total);
    }

    /**
     * The progress of a job at one moment, as returned with a job that is in progress.
     */
    public static final class Snapshot {
        private final Phase phase;
        private final int done;
        private final int total;

        // Annotated so Jackson can read a job's progress back from its JSON, as the tests do.
        @JsonCreator
        public Snapshot(@JsonProperty("phase") Phase phase, @JsonProperty("done") int done,
                @JsonProperty("total") int total) {
            this.phase = phase;
            this.done = done;
            this.total = total;
        }

        public Phase getPhase() {
            return phase;
        }

        public int getDone() {
            return done;
        }

        public int getTotal() {
            return total;
        }
    }
}
