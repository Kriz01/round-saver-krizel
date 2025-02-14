package com.starling.roundup.exception;

public class FeedNotFoundException extends RuntimeException{
    public FeedNotFoundException(String message) {
        super(message);
    }
}
