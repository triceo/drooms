package org.drooms.impl.util.cli;

/**
 * Common interface for the command-line processors.
 * 
 * @param <T>
 *            Return type of the command-line processor.
 */
interface CommonCLI<T> {

    /**
     * Print the message on how to use the command line interface.
     */
    public abstract void printHelp();

    /**
     * Process the command line arguments and return the result.
     * 
     * @param args
     *            Command-line argument.
     * @return Result.
     */
    public abstract T process(String[] args);

}
