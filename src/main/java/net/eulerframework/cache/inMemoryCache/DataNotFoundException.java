package net.eulerframework.cache.inMemoryCache;

public class DataNotFoundException extends Exception {

    public DataNotFoundException(){
        super("The data does not exist or has expired.");
    }
}
