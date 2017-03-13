package io.nya.powerlyrics.model;

/**
 * Created by nene on 3/13/17.
 */

public class LyricNotFoundException extends Exception {
    public LyricNotFoundException() {
    }

    public LyricNotFoundException(String detailMessage) {
        super(detailMessage);
    }

    public LyricNotFoundException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public LyricNotFoundException(Throwable throwable) {
        super(throwable);
    }

}
