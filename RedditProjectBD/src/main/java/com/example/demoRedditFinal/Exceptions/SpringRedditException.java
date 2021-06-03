package com.example.demoRedditFinal.Exceptions;

public class SpringRedditException extends RuntimeException{

    public SpringRedditException(String exMessage,Exception exception){
        super(exMessage,exception);
    }

    public SpringRedditException(String exMessage){
        super(exMessage);
    }

}
