package com.ag.logviewer.restservice.representation;

import java.util.Date;

/**
 * Representation of a standard logline.
 * @author Asha
 *
 */
// logLevel should be enum, needs more work with normalization
public record LogLine(Date date, String logLevel, String message) { }