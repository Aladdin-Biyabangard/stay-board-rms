package az.aladdin.stayboard.controller;

import az.aladdin.stayboard.model.response.report.RmsDailyStatisticsResponse;
import az.aladdin.stayboard.model.response.report.RmsSalesByCategoryReportResponse;
import az.aladdin.stayboard.model.response.report.RmsSalesSummaryReportResponse;
import az.aladdin.stayboard.model.response.report.RmsTopItemsReportResponse;
import az.aladdin.stayboard.service.report.RmsReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/rms/reports")
public class RmsReportController {

    private final RmsReportService rmsReportService;

    @GetMapping("/daily-statistics")
    public RmsDailyStatisticsResponse getDailyStatistics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return rmsReportService.getDailyStatistics(date);
    }

    @GetMapping("/sales-summary")
    public RmsSalesSummaryReportResponse getSalesSummary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        return rmsReportService.getSalesSummary(fromDate, toDate);
    }

    @GetMapping("/top-items")
    public RmsTopItemsReportResponse getTopItems(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(defaultValue = "10") int limit) {
        return rmsReportService.getTopItems(fromDate, toDate, limit);
    }

    @GetMapping("/sales-by-category")
    public RmsSalesByCategoryReportResponse getSalesByCategory(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        return rmsReportService.getSalesByCategory(fromDate, toDate);
    }
}
