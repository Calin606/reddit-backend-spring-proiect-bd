package com.example.demoRedditFinal.Exceptions;

public class PostNotFoundException extends RuntimeException{

    public PostNotFoundException(String exMessage, Exception exception){
        super(exMessage,exception);
    }

    public PostNotFoundException(String exMessage){
        super(exMessage);
    }

}
