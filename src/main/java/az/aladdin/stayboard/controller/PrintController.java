package az.aladdin.stayboard.controller;

import az.aladdin.stayboard.model.response.KitchenTicketPrintResponse;
import az.aladdin.stayboard.model.response.OrderReceiptResponse;
import az.aladdin.stayboard.service.print.KitchenTicketPrintService;
import az.aladdin.stayboard.service.print.OrderReceiptService;
import az.aladdin.stayboard.service.print.PrintDocumentRenderer;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/rms")
public class PrintController {

    private final OrderReceiptService orderReceiptService;
    private final KitchenTicketPrintService kitchenTicketPrintService;
    private final PrintDocumentRenderer printDocumentRenderer;

    @GetMapping("/orders/{orderId}/receipt")
    public OrderReceiptResponse getOrderReceipt(@PathVariable Long orderId) {
        return orderReceiptService.generateReceipt(orderId);
    }

    @GetMapping(value = "/orders/{orderId}/receipt/html", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> getOrderReceiptHtml(@PathVariable Long orderId) {
        OrderReceiptResponse receipt = orderReceiptService.generateReceipt(orderId);
        return ResponseEntity.ok(printDocumentRenderer.renderOrderReceiptHtml(receipt));
    }

    @GetMapping(value = "/orders/{orderId}/receipt/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> getOrderReceiptPdf(@PathVariable Long orderId) {
        OrderReceiptResponse receipt = orderReceiptService.generateReceipt(orderId);
        String html = printDocumentRenderer.renderOrderReceiptHtml(receipt);
        byte[] pdf = printDocumentRenderer.renderPdf(html);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"receipt-" + receipt.orderNumber() + ".pdf\"")
                .body(pdf);
    }

    @GetMapping("/orders/{orderId}/kitchen-ticket")
    public KitchenTicketPrintResponse getOrderKitchenTicket(@PathVariable Long orderId) {
        return kitchenTicketPrintService.generateForOrder(orderId);
    }

    @GetMapping(value = "/orders/{orderId}/kitchen-ticket/html", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> getOrderKitchenTicketHtml(@PathVariable Long orderId) {
        KitchenTicketPrintResponse ticket = kitchenTicketPrintService.generateForOrder(orderId);
        return ResponseEntity.ok(printDocumentRenderer.renderKitchenTicketHtml(ticket));
    }

    @GetMapping(value = "/orders/{orderId}/kitchen-ticket/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> getOrderKitchenTicketPdf(@PathVariable Long orderId) {
        KitchenTicketPrintResponse ticket = kitchenTicketPrintService.generateForOrder(orderId);
        String html = printDocumentRenderer.renderKitchenTicketHtml(ticket);
        byte[] pdf = printDocumentRenderer.renderPdf(html);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"kitchen-ticket-" + ticket.orderNumber() + ".pdf\"")
                .body(pdf);
    }

    @GetMapping("/kitchen/tickets/{orderItemId}/print")
    public KitchenTicketPrintResponse getKitchenTicketForItem(@PathVariable Long orderItemId) {
        return kitchenTicketPrintService.generateForOrderItem(orderItemId);
    }

    @GetMapping(value = "/kitchen/tickets/{orderItemId}/print/html", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> getKitchenTicketForItemHtml(@PathVariable Long orderItemId) {
        KitchenTicketPrintResponse ticket = kitchenTicketPrintService.generateForOrderItem(orderItemId);
        return ResponseEntity.ok(printDocumentRenderer.renderKitchenTicketHtml(ticket));
    }

    @GetMapping(value = "/kitchen/tickets/{orderItemId}/print/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> getKitchenTicketForItemPdf(@PathVariable Long orderItemId) {
        KitchenTicketPrintResponse ticket = kitchenTicketPrintService.generateForOrderItem(orderItemId);
        String html = printDocumentRenderer.renderKitchenTicketHtml(ticket);
        byte[] pdf = printDocumentRenderer.renderPdf(html);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"kitchen-ticket-item-" + orderItemId + ".pdf\"")
                .body(pdf);
    }
}
