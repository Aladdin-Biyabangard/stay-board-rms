package az.aladdin.stayboard.service.print;

import az.aladdin.stayboard.model.response.KitchenTicketPrintLine;
import az.aladdin.stayboard.model.response.KitchenTicketPrintResponse;
import az.aladdin.stayboard.model.response.OrderReceiptLineItem;
import az.aladdin.stayboard.model.response.OrderReceiptResponse;
import az.aladdin.stayboard.util.PrintSupport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PrintDocumentRenderer {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
    private static final DateTimeFormatter DATE_ONLY_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy");

    public String renderOrderReceiptHtml(OrderReceiptResponse receipt) {
        StringBuilder html = new StringBuilder();
        appendHtmlHead(html, buildOrderReceiptStyles());
        html.append("<body>\n");
        html.append("<div class=\"receipt-container\">\n");

        html.append("<div class=\"header\">\n");
        html.append("<h2>RESTAURANT RECEIPT</h2>\n");
        html.append("<p>Receipt Number: ").append(PrintSupport.escapeHtml(receipt.receiptNumber())).append("</p>\n");
        html.append("<p>Date: ").append(formatDateTime(receipt.receiptDate())).append("</p>\n");
        html.append("</div>\n");

        html.append("<div class=\"guest-info\">\n");
        appendInfoItem(html, "Guest Name:", receipt.guestName());
        appendInfoItem(html, "Order Number:", receipt.orderNumber());
        appendInfoItem(html, "Table:", receipt.tableNumber());
        appendInfoItem(html, "Room:", receipt.roomNumber());
        appendInfoItem(html, "Status:", receipt.orderStatus() != null ? receipt.orderStatus().name() : null);
        html.append("</div>\n");

        html.append("<table>\n");
        html.append("<thead><tr>\n");
        html.append("<th>Date</th><th>Item</th><th>Status</th>");
        html.append("<th style=\"text-align:right;\">Qty</th>");
        html.append("<th style=\"text-align:right;\">Net</th>");
        html.append("<th style=\"text-align:right;\">Tax</th>");
        html.append("<th style=\"text-align:right;\">Total</th>");
        html.append("</tr></thead>\n<tbody>\n");

        if (receipt.items() == null || receipt.items().isEmpty()) {
            html.append("<tr><td colspan=\"7\" class=\"empty\">No items</td></tr>\n");
        } else {
            Map<String, List<OrderReceiptLineItem>> groupedItems = receipt.items().stream()
                    .collect(Collectors.groupingBy(
                            item -> dateKey(item.createdAt()),
                            LinkedHashMap::new,
                            Collectors.toList()
                    ));

            for (List<OrderReceiptLineItem> group : groupedItems.values()) {
                if (group.isEmpty()) {
                    continue;
                }
                html.append("<tr class=\"date-group\"><td colspan=\"7\">")
                        .append(formatDateOnly(group.getFirst().createdAt()))
                        .append("</td></tr>\n");
                for (OrderReceiptLineItem item : group) {
                    html.append("<tr>\n");
                    html.append("<td>").append(formatDateTime(item.createdAt())).append("</td>\n");
                    html.append("<td>").append(PrintSupport.escapeHtml(item.menuItemName())).append("</td>\n");
                    html.append("<td><span class=\"status\">")
                            .append(PrintSupport.escapeHtml(item.orderItemStatus() != null ? item.orderItemStatus().name() : "—"))
                            .append("</span></td>\n");
                    html.append("<td style=\"text-align:right;\">").append(PrintSupport.escapeHtml(item.quantityLabel())).append("</td>\n");
                    html.append("<td style=\"text-align:right;\">").append(formatMoney(item.netAmount(), receipt.currencyCode())).append("</td>\n");
                    html.append("<td style=\"text-align:right;\">").append(formatMoney(item.taxAmount(), receipt.currencyCode())).append("</td>\n");
                    html.append("<td style=\"text-align:right;font-weight:bold;\">").append(formatMoney(item.grossAmount(), receipt.currencyCode())).append("</td>\n");
                    html.append("</tr>\n");
                }
            }
        }

        html.append("</tbody></table>\n");

        html.append("<div class=\"totals\">\n");
        appendTotalRow(html, "Subtotal (net):", receipt.subtotalNet(), receipt.currencyCode(), false);
        appendTotalRow(html, "Tax:", receipt.totalTax(), receipt.currencyCode(), false);
        appendTotalRow(html, "Total:", receipt.totalGross(), receipt.currencyCode(), true);
        if (receipt.orderTotal() != null && receipt.orderTotal().compareTo(receipt.totalGross()) != 0) {
            appendTotalRow(html, "Order Total:", receipt.orderTotal(), receipt.currencyCode(), true);
        }
        html.append("</div>\n");

        html.append("<div class=\"footer\">\n");
        html.append("<p>Thank you for dining with us!</p>\n");
        html.append("<p>This is a system-generated receipt</p>\n");
        html.append("</div>\n");

        html.append("</div>\n</body>\n</html>\n");
        return html.toString();
    }

    public String renderKitchenTicketHtml(KitchenTicketPrintResponse ticket) {
        StringBuilder html = new StringBuilder();
        appendHtmlHead(html, buildKitchenTicketStyles());
        html.append("<body>\n");
        html.append("<div class=\"ticket\">\n");

        html.append("<div class=\"ticket-header\">\n");
        html.append("<div class=\"ticket-label\">KITCHEN TICKET</div>\n");
        html.append("<div class=\"order-number\">").append(PrintSupport.escapeHtml(ticket.orderNumber())).append("</div>\n");
        html.append("<div class=\"ticket-meta\">").append(PrintSupport.escapeHtml(ticket.ticketNumber())).append("</div>\n");
        html.append("<div class=\"ticket-meta\">").append(formatDateTime(ticket.ticketDate())).append("</div>\n");
        html.append("</div>\n");

        html.append("<div class=\"location\">").append(PrintSupport.escapeHtml(ticket.serviceLocation())).append("</div>\n");

        if (ticket.items() == null || ticket.items().isEmpty()) {
            html.append("<div class=\"empty\">No active kitchen items</div>\n");
        } else {
            for (KitchenTicketPrintLine line : ticket.items()) {
                html.append("<div class=\"item\">\n");
                html.append("<div class=\"item-qty\">").append(PrintSupport.escapeHtml(line.quantityLabel())).append("</div>\n");
                html.append("<div class=\"item-body\">\n");
                html.append("<div class=\"item-name\">").append(PrintSupport.escapeHtml(line.menuItemName())).append("</div>\n");
                html.append("<div class=\"item-meta\">")
                        .append(PrintSupport.escapeHtml(line.orderItemStatus() != null ? line.orderItemStatus().name() : "—"))
                        .append(" · ")
                        .append(formatDateTime(line.createdAt()))
                        .append("</div>\n");
                html.append("</div>\n");
                html.append("</div>\n");
            }
        }

        html.append("<div class=\"ticket-footer\">\n");
        html.append("<p>*** KITCHEN COPY ***</p>\n");
        html.append("</div>\n");

        html.append("</div>\n</body>\n</html>\n");
        return html.toString();
    }

    public byte[] renderPdf(String html) {
        try {
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(html);
            renderer.layout();

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            renderer.createPDF(outputStream);
            renderer.finishPDF();
            return outputStream.toByteArray();
        } catch (Exception e) {
            log.error("Failed to generate print PDF", e);
            throw new IllegalStateException("Failed to generate print PDF", e);
        }
    }

    private static void appendHtmlHead(StringBuilder html, String styles) {
        html.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n");
        html.append("<html xmlns=\"http://www.w3.org/1999/xhtml\">\n<head>\n");
        html.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />\n");
        html.append("<style type=\"text/css\">\n");
        html.append(styles);
        html.append("</style>\n</head>\n");
    }

    private static String buildOrderReceiptStyles() {
        return """
                body { font-family: Arial, sans-serif; margin: 0; padding: 20px; color: #111827; }
                .receipt-container { max-width: 800px; margin: 0 auto; background: white; padding: 24px; }
                .header { text-align: center; margin-bottom: 28px; border-bottom: 2px solid #111827; padding-bottom: 18px; }
                .header h2 { margin: 0; font-size: 24px; font-weight: bold; letter-spacing: 0.04em; }
                .header p { margin: 6px 0 0; font-size: 13px; color: #6b7280; }
                .guest-info { margin-bottom: 24px; overflow: hidden; }
                .info-item { float: left; width: 48%; margin-right: 2%; margin-bottom: 14px; }
                .info-label { font-weight: bold; font-size: 12px; margin-bottom: 4px; color: #374151; }
                .info-value { font-size: 14px; }
                table { width: 100%; border-collapse: collapse; margin-bottom: 24px; }
                th { text-align: left; padding: 10px 8px; border-bottom: 2px solid #111827; font-size: 12px; }
                td { padding: 10px 8px; border-bottom: 1px solid #e5e7eb; font-size: 12px; vertical-align: top; }
                .date-group td { background: #f3f4f6; font-weight: bold; padding: 8px; }
                .status { color: #2563eb; font-weight: 600; }
                .empty { text-align: center; color: #6b7280; padding: 18px 0; }
                .totals { border-top: 2px solid #111827; padding-top: 14px; }
                .total-row { display: block; overflow: hidden; margin-bottom: 8px; font-size: 14px; }
                .total-row span:first-child { float: left; }
                .total-row span:last-child { float: right; }
                .total-row.final { font-size: 18px; font-weight: bold; border-top: 1px solid #d1d5db; padding-top: 10px; margin-top: 10px; }
                .footer { text-align: center; margin-top: 36px; padding-top: 18px; border-top: 1px solid #d1d5db; font-size: 11px; color: #6b7280; }
                """;
    }

    private static String buildKitchenTicketStyles() {
        return """
                @page { size: 80mm auto; margin: 4mm; }
                body { font-family: Arial, sans-serif; margin: 0; padding: 0; color: #000; }
                .ticket { width: 72mm; margin: 0 auto; }
                .ticket-header { text-align: center; border-bottom: 2px dashed #000; padding-bottom: 8px; margin-bottom: 10px; }
                .ticket-label { font-size: 11px; letter-spacing: 0.12em; font-weight: bold; }
                .order-number { font-size: 28px; font-weight: bold; line-height: 1.1; margin-top: 4px; }
                .ticket-meta { font-size: 11px; margin-top: 3px; }
                .location { text-align: center; font-size: 22px; font-weight: bold; margin: 12px 0 16px; padding: 8px 0; border-top: 1px solid #000; border-bottom: 1px solid #000; }
                .item { display: table; width: 100%; margin-bottom: 14px; }
                .item-qty { display: table-cell; width: 18mm; font-size: 24px; font-weight: bold; vertical-align: top; }
                .item-body { display: table-cell; vertical-align: top; }
                .item-name { font-size: 18px; font-weight: bold; line-height: 1.2; }
                .item-meta { font-size: 10px; margin-top: 4px; color: #444; }
                .empty { text-align: center; font-size: 12px; padding: 16px 0; }
                .ticket-footer { text-align: center; margin-top: 18px; padding-top: 10px; border-top: 2px dashed #000; font-size: 11px; font-weight: bold; }
                """;
    }

    private static void appendInfoItem(StringBuilder html, String label, String value) {
        html.append("<div class=\"info-item\">\n");
        html.append("<div class=\"info-label\">").append(PrintSupport.escapeHtml(label)).append("</div>\n");
        html.append("<div class=\"info-value\">").append(PrintSupport.escapeHtml(value != null && !value.isBlank() ? value : "—")).append("</div>\n");
        html.append("</div>\n");
    }

    private static void appendTotalRow(StringBuilder html, String label, BigDecimal amount, String currencyCode, boolean finalRow) {
        html.append("<div class=\"total-row");
        if (finalRow) {
            html.append(" final");
        }
        html.append("\">\n");
        html.append("<span>").append(PrintSupport.escapeHtml(label)).append("</span>\n");
        html.append("<span>").append(formatMoney(amount, currencyCode)).append("</span>\n");
        html.append("</div>\n");
    }

    private static String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "—";
        }
        return PrintSupport.escapeHtml(dateTime.format(DATE_TIME_FORMATTER));
    }

    private static String formatDateOnly(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "—";
        }
        return PrintSupport.escapeHtml(dateTime.format(DATE_ONLY_FORMATTER));
    }

    private static String dateKey(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "unknown";
        }
        return dateTime.toLocalDate().toString();
    }

    private static String formatMoney(BigDecimal amount, String currencyCode) {
        return PrintSupport.escapeHtml(PrintSupport.formatMoney(amount, currencyCode));
    }
}
