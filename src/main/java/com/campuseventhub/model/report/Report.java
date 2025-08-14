// =============================================================================
// ABSTRACT REPORT BASE CLASS
// =============================================================================

package com.campuseventhub.model.report;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Abstract base class for all system reports.
 * Provides common functionality for report generation, formatting, and export.
 */
public abstract class Report {
    protected String reportId;
    protected String title;
    protected LocalDateTime generatedAt;
    protected String generatedBy;
    protected Map<String, Object> data;
    
    protected Report(String title, String generatedBy) {
        this.reportId = java.util.UUID.randomUUID().toString();
        this.title = title;
        this.generatedBy = generatedBy;
        this.generatedAt = LocalDateTime.now();
    }
    
    /**
     * Generate the report data
     */
    public abstract void generate();
    
    /**
     * Export report to specified format
     */
    public abstract String export(String format);
    
    /**
     * Get summary statistics for the report
     */
    public abstract Map<String, Object> getSummary();
    
    // Getters
    public String getReportId() { return reportId; }
    public String getTitle() { return title; }
    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public String getGeneratedBy() { return generatedBy; }
    public Map<String, Object> getData() { return data; }
}