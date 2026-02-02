package com.scrapx.api.dto;

public record OFACResults (
    String name,
    String address,
    String type,
    String program,
    String list,
    String score
) {}
