package net.eulerframework.cache.inMemoryCache;

@SuppressWarnings("serial")
public class DataNotFoundException extends Exception {

    public DataNotFoundException(){
        super("The data does not exist or has expired.");
    }
}
