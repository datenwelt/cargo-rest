/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.datenwelt.cargo.rest.response;

import io.datenwelt.cargo.rest.serialization.Json;

/**
 *
 * @author job
 */
public class APIError {
    
    private final int code;
    private final String reason;

    public APIError(int code, String reason) {
        this.code = code;
        this.reason = reason;
    }

    public int getCode() {
        return code;
    }

    public String getReason() {
        return reason;
    }

    @Override
    public String toString() {
        return Json.toJson(this);
    }
    
}
