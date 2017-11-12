package com.pxs.reaper.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.couchbase.core.mapping.Document;

/**
 * Contains class loading metrics, not particularly interesting.
 *
 * @author Michael Couck
 * @version 1.0
 * @since 20-10-2017
 */
@Getter
@Setter
@ToString
@Document
@NoArgsConstructor
public class Classloading {

    private long loadedClassCount;
    private long totalLoadedClassCount;
    private long unLoadedClassCount;

}
