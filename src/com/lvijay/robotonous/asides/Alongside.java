package com.lvijay.robotonous.asides;

/**
 * Executes while other robotonous actions are occurring.
 */
public interface Alongside {
    void execute(String arg) throws Exception;
}
