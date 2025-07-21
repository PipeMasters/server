package com.pipemasters.server.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SttFragmentDto {
    private Long begin;
    private Long end;
    private String direction;
    private String text;
    private String fragment_id;

    public SttFragmentDto() {
    }

    public SttFragmentDto(Long begin, String direction, Long end, String fragment_id, String text) {
        this.begin = begin;
        this.direction = direction;
        this.end = end;
        this.fragment_id = fragment_id;
        this.text = text;
    }

    public Long getBegin() {
        return begin;
    }

    public void setBegin(Long begin) {
        this.begin = begin;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public Long getEnd() {
        return end;
    }

    public void setEnd(Long end) {
        this.end = end;
    }

    public String getFragment_id() {
        return fragment_id;
    }

    public void setFragment_id(String fragment_id) {
        this.fragment_id = fragment_id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}

