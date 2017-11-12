package com.pxs.reaper.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.couchbase.core.mapping.Document;

/**
 * Contains compilation time for the Jit compiler, probably not very highly correlated to performance, but could be.
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
public class Compilation {

    private long compilationTime;

}
