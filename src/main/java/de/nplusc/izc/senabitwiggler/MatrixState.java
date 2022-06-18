package de.nplusc.izc.senabitwiggler;

public enum MatrixState {
    /**
     * used for guessed versions. Version will be renotified if its released
     */
    SENT_PRELIM,
    /**
     * released version, notified on matrix
     */
    SENT,
    /**
     * used for fake versions.
     */
    SKIPPED,
    /**
     * Not yet published
     */
    TODO
}
