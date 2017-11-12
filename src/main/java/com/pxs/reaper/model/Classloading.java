package com.pxs.reaper.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Classloading {

    private long loadedClassCount;
    private long totalLoadedClassCount;
    private long unLoadedClassCount;

}
