package com.pxs.reaper.agent.toolkit;

import java.util.function.Function;

/**
 * TODO: Weave this class into an annotation, for dynamic application...
 * <p>
 * TODO: Document me...
 *
 * @author Michael Couck
 * @version 1.0
 * @since 01-11-2017
 */
@FunctionalInterface
public interface Retry {

    /**
     * TODO: Document me...
     *
     * @param functionToRetry          ...
     * @param input                    ...
     * @param numberOfRetries          ...
     * @param finalDelayBetweenRetries ...
     * @param <I>                      ...
     * @param <O>                      ...
     * @return ...
     */
    <I, O> O retry(final Function<I, O> functionToRetry, final I input, final int numberOfRetries, final long finalDelayBetweenRetries);

}
