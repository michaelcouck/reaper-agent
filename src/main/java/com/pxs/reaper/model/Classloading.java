package com.pxs.reaper.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;

/**
 * Contains class loading metrics, not particularly interesting.
 *
 * @author Michael Couck
 * @version 1.0
 * @since 20-10-2017
 */
@Getter
@Setter
@Builder
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Classloading {

    @Column
    private long loadedClassCount;
    @Column
    private long totalLoadedClassCount;
    @Column
    private long unLoadedClassCount;

}
