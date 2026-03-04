package com.oceanview.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Abstract base for all API servlets.
 * Provides a pre-configured Jackson ObjectMapper and a writeJson helper.
 */
public abstract class BaseApiServlet extends HttpServlet {

    protected static final ObjectMapper MAPPER;

    static {
        MAPPER = new ObjectMapper();
        MAPPER.registerModule(new JavaTimeModule());
        MAPPER.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    protected void writeJson(HttpServletResponse resp, Object data) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        resp.setHeader("Cache-Control", "no-cache");
        MAPPER.writeValue(resp.getWriter(), data);
    }

    protected void writeError(HttpServletResponse resp, int status, String message) throws IOException {
        resp.setStatus(status);
        writeJson(resp, java.util.Map.of("status", "error", "message", message));
    }

    protected void writeSuccess(HttpServletResponse resp, String message) throws IOException {
        writeJson(resp, java.util.Map.of("status", "success", "message", message));
    }
}
