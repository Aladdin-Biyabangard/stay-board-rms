package az.aladdin.stayboard.config.security;

import lombok.Getter;
import org.springframework.http.HttpMethod;

import static az.aladdin.stayboard.config.security.ApiSecurityLevel.ACCOUNTING;
import static az.aladdin.stayboard.config.security.ApiSecurityLevel.ADMIN;
import static az.aladdin.stayboard.config.security.ApiSecurityLevel.DIRECTOR;
import static az.aladdin.stayboard.config.security.ApiSecurityLevel.FRONT_DESK;
import static az.aladdin.stayboard.config.security.ApiSecurityLevel.GUEST;
import static az.aladdin.stayboard.config.security.ApiSecurityLevel.HOUSEKEEPING;
import static az.aladdin.stayboard.config.security.ApiSecurityLevel.MANAGER;
import static az.aladdin.stayboard.config.security.ApiSecurityLevel.PUBLIC;

@Getter
public enum ApiEndpoint {

    ERROR_ENDPOINT("/error", null, PUBLIC),

    INTERNAL_DB_IMPORT("/v1/rms/internal/db/import", HttpMethod.POST, PUBLIC),
    INTERNAL_DB_EXPORT("/v1/rms/internal/db/export", HttpMethod.POST, PUBLIC),

    MENU_CATEGORY_IMAGE_UPLOAD("/v1/rms/menu-categories/*/images", HttpMethod.POST, MANAGER, ADMIN, DIRECTOR),
    MENU_CATEGORY_IMAGE_DELETE("/v1/rms/menu-categories/*/images", HttpMethod.DELETE, MANAGER, ADMIN, DIRECTOR),
    MENU_CATEGORY_SET_MAIN_IMAGE("/v1/rms/menu-categories/*/images/main", HttpMethod.PUT, MANAGER, ADMIN, DIRECTOR),

    MENU_CATEGORIES_READ("/v1/rms/menu-categories/**", HttpMethod.GET, guestPortalReadRoles()),
    MENU_CATEGORIES_WRITE("/v1/rms/menu-categories/**", HttpMethod.POST, MANAGER, ADMIN, DIRECTOR),
    MENU_CATEGORIES_UPDATE("/v1/rms/menu-categories/**", HttpMethod.PUT, MANAGER, ADMIN, DIRECTOR),
    MENU_CATEGORIES_PATCH("/v1/rms/menu-categories/**", HttpMethod.PATCH, MANAGER, ADMIN, DIRECTOR),
    MENU_CATEGORIES_DELETE("/v1/rms/menu-categories/**", HttpMethod.DELETE, MANAGER, ADMIN, DIRECTOR),

    MENU_ITEM_IMAGE_UPLOAD("/v1/rms/menu-items/*/images", HttpMethod.POST, MANAGER, ADMIN, DIRECTOR),
    MENU_ITEM_IMAGE_DELETE("/v1/rms/menu-items/*/images", HttpMethod.DELETE, MANAGER, ADMIN, DIRECTOR),
    MENU_ITEM_SET_MAIN_IMAGE("/v1/rms/menu-items/*/images/main", HttpMethod.PUT, MANAGER, ADMIN, DIRECTOR),

    MENU_ITEMS_READ("/v1/rms/menu-items/**", HttpMethod.GET, guestPortalReadRoles()),
    MENU_ITEMS_WRITE("/v1/rms/menu-items/**", HttpMethod.POST, MANAGER, ADMIN, DIRECTOR),
    MENU_ITEMS_UPDATE("/v1/rms/menu-items/**", HttpMethod.PUT, MANAGER, ADMIN, DIRECTOR),
    MENU_ITEMS_PATCH("/v1/rms/menu-items/**", HttpMethod.PATCH, MANAGER, ADMIN, DIRECTOR),
    MENU_ITEMS_DELETE("/v1/rms/menu-items/**", HttpMethod.DELETE, MANAGER, ADMIN, DIRECTOR),

    TABLE_MERGE_READ("/v1/rms/tables/*/merge-group", HttpMethod.GET, staffRestaurantReadRoles()),
    TABLE_MERGE_WRITE("/v1/rms/tables/merge", HttpMethod.POST, occupancyWriteRoles()),
    TABLE_MERGE_DELETE("/v1/rms/tables/*/merge", HttpMethod.DELETE, occupancyWriteRoles()),

    TABLES_READ("/v1/rms/tables/**", HttpMethod.GET, guestPortalReadRoles()),
    TABLES_WRITE("/v1/rms/tables/**", HttpMethod.POST, MANAGER, ADMIN, DIRECTOR),
    TABLES_UPDATE("/v1/rms/tables/**", HttpMethod.PUT, MANAGER, ADMIN, DIRECTOR),
    TABLES_PATCH("/v1/rms/tables/**", HttpMethod.PATCH, MANAGER, ADMIN, DIRECTOR),
    TABLES_DELETE("/v1/rms/tables/**", HttpMethod.DELETE, MANAGER, ADMIN, DIRECTOR),

    ORDER_RECEIPT_READ("/v1/rms/orders/*/receipt", HttpMethod.GET, guestPortalReadRoles()),
    ORDER_RECEIPT_HTML("/v1/rms/orders/*/receipt/html", HttpMethod.GET, guestPortalReadRoles()),
    ORDER_RECEIPT_PDF("/v1/rms/orders/*/receipt/pdf", HttpMethod.GET, guestPortalReadRoles()),
    ORDER_KITCHEN_TICKET_READ("/v1/rms/orders/*/kitchen-ticket", HttpMethod.GET, kitchenRoles()),
    ORDER_KITCHEN_TICKET_HTML("/v1/rms/orders/*/kitchen-ticket/html", HttpMethod.GET, kitchenRoles()),
    ORDER_KITCHEN_TICKET_PDF("/v1/rms/orders/*/kitchen-ticket/pdf", HttpMethod.GET, kitchenRoles()),

    ORDERS_READ("/v1/rms/orders/**", HttpMethod.GET, guestPortalReadRoles()),
    ORDERS_WRITE("/v1/rms/orders/**", HttpMethod.POST, guestOrderWriteRoles()),
    ORDERS_UPDATE("/v1/rms/orders/**", HttpMethod.PUT, guestOrderWriteRoles()),
    ORDERS_PATCH("/v1/rms/orders/**", HttpMethod.PATCH, guestOrderWriteRoles()),
    ORDERS_DELETE("/v1/rms/orders/**", HttpMethod.DELETE, MANAGER, ADMIN, DIRECTOR),

    ORDER_ITEMS_READ("/v1/rms/order-items/**", HttpMethod.GET, guestPortalReadRoles()),
    ORDER_ITEMS_WRITE("/v1/rms/order-items/**", HttpMethod.POST, guestOrderWriteRoles()),
    ORDER_ITEMS_UPDATE("/v1/rms/order-items/**", HttpMethod.PUT, guestOrderWriteRoles()),
    ORDER_ITEMS_PATCH("/v1/rms/order-items/**", HttpMethod.PATCH, guestOrderWriteRoles()),
    ORDER_ITEMS_DELETE("/v1/rms/order-items/**", HttpMethod.DELETE, guestOrderWriteRoles()),

    KITCHEN_TICKET_PRINT_READ("/v1/rms/kitchen/tickets/*/print", HttpMethod.GET, kitchenRoles()),
    KITCHEN_TICKET_PRINT_HTML("/v1/rms/kitchen/tickets/*/print/html", HttpMethod.GET, kitchenRoles()),
    KITCHEN_TICKET_PRINT_PDF("/v1/rms/kitchen/tickets/*/print/pdf", HttpMethod.GET, kitchenRoles()),

    KITCHEN_TICKETS_READ("/v1/rms/kitchen/tickets/**", HttpMethod.GET, kitchenRoles()),
    KITCHEN_TICKETS_UPDATE("/v1/rms/kitchen/tickets/*/status", HttpMethod.PATCH, kitchenRoles()),

    INVENTORY_STOCK_ADJUST("/v1/rms/inventory-items/*/stock", HttpMethod.POST, MANAGER, ADMIN, DIRECTOR),

    INVENTORY_ITEMS_READ("/v1/rms/inventory-items/**", HttpMethod.GET, staffRestaurantReadRoles()),
    INVENTORY_ITEMS_WRITE("/v1/rms/inventory-items/**", HttpMethod.POST, MANAGER, ADMIN, DIRECTOR),
    INVENTORY_ITEMS_UPDATE("/v1/rms/inventory-items/**", HttpMethod.PUT, MANAGER, ADMIN, DIRECTOR),
    INVENTORY_ITEMS_PATCH("/v1/rms/inventory-items/**", HttpMethod.PATCH, MANAGER, ADMIN, DIRECTOR),
    INVENTORY_ITEMS_DELETE("/v1/rms/inventory-items/**", HttpMethod.DELETE, MANAGER, ADMIN, DIRECTOR),

    RECIPES_READ("/v1/rms/recipes/**", HttpMethod.GET, staffRestaurantReadRoles()),
    RECIPES_WRITE("/v1/rms/recipes/**", HttpMethod.POST, MANAGER, ADMIN, DIRECTOR),
    RECIPES_UPDATE("/v1/rms/recipes/**", HttpMethod.PUT, MANAGER, ADMIN, DIRECTOR),
    RECIPES_DELETE("/v1/rms/recipes/**", HttpMethod.DELETE, MANAGER, ADMIN, DIRECTOR),

    ALLERGENS_READ("/v1/rms/allergens/**", HttpMethod.GET, guestPortalReadRoles()),
    ALLERGENS_WRITE("/v1/rms/allergens/**", HttpMethod.POST, MANAGER, ADMIN, DIRECTOR),
    ALLERGENS_UPDATE("/v1/rms/allergens/**", HttpMethod.PUT, MANAGER, ADMIN, DIRECTOR),
    ALLERGENS_PATCH("/v1/rms/allergens/**", HttpMethod.PATCH, MANAGER, ADMIN, DIRECTOR),
    ALLERGENS_DELETE("/v1/rms/allergens/**", HttpMethod.DELETE, MANAGER, ADMIN, DIRECTOR),

    DIETARY_TAGS_READ("/v1/rms/dietary-tags/**", HttpMethod.GET, guestPortalReadRoles()),
    DIETARY_TAGS_WRITE("/v1/rms/dietary-tags/**", HttpMethod.POST, MANAGER, ADMIN, DIRECTOR),
    DIETARY_TAGS_UPDATE("/v1/rms/dietary-tags/**", HttpMethod.PUT, MANAGER, ADMIN, DIRECTOR),
    DIETARY_TAGS_PATCH("/v1/rms/dietary-tags/**", HttpMethod.PATCH, MANAGER, ADMIN, DIRECTOR),
    DIETARY_TAGS_DELETE("/v1/rms/dietary-tags/**", HttpMethod.DELETE, MANAGER, ADMIN, DIRECTOR),

    MODIFIER_GROUPS_READ("/v1/rms/modifier-groups/**", HttpMethod.GET, guestPortalReadRoles()),
    MODIFIER_GROUPS_WRITE("/v1/rms/modifier-groups/**", HttpMethod.POST, MANAGER, ADMIN, DIRECTOR),
    MODIFIER_GROUPS_UPDATE("/v1/rms/modifier-groups/**", HttpMethod.PUT, MANAGER, ADMIN, DIRECTOR),
    MODIFIER_GROUPS_PATCH("/v1/rms/modifier-groups/**", HttpMethod.PATCH, MANAGER, ADMIN, DIRECTOR),
    MODIFIER_GROUPS_DELETE("/v1/rms/modifier-groups/**", HttpMethod.DELETE, MANAGER, ADMIN, DIRECTOR),

    MODIFIER_OPTIONS_READ("/v1/rms/modifier-options/**", HttpMethod.GET, guestPortalReadRoles()),
    MODIFIER_OPTIONS_WRITE("/v1/rms/modifier-options/**", HttpMethod.POST, MANAGER, ADMIN, DIRECTOR),
    MODIFIER_OPTIONS_UPDATE("/v1/rms/modifier-options/**", HttpMethod.PUT, MANAGER, ADMIN, DIRECTOR),
    MODIFIER_OPTIONS_PATCH("/v1/rms/modifier-options/**", HttpMethod.PATCH, MANAGER, ADMIN, DIRECTOR),
    MODIFIER_OPTIONS_DELETE("/v1/rms/modifier-options/**", HttpMethod.DELETE, MANAGER, ADMIN, DIRECTOR),

    TABLE_OCCUPANCY_READ("/v1/rms/table-occupancies/**", HttpMethod.GET, guestPortalReadRoles()),
    TABLE_OCCUPANCY_WRITE("/v1/rms/table-occupancies/**", HttpMethod.POST, guestOccupancyWriteRoles()),
    TABLE_OCCUPANCY_UPDATE("/v1/rms/table-occupancies/**", HttpMethod.PUT, occupancyWriteRoles()),
    TABLE_OCCUPANCY_PATCH("/v1/rms/table-occupancies/**", HttpMethod.PATCH, occupancyWriteRoles()),
    TABLE_OCCUPANCY_DELETE("/v1/rms/table-occupancies/**", HttpMethod.DELETE, guestOccupancyWriteRoles()),

    WAITLIST_STATUS_UPDATE("/v1/rms/waitlist-entries/*/status", HttpMethod.PATCH, occupancyWriteRoles()),
    WAITLIST_SEAT("/v1/rms/waitlist-entries/*/seat", HttpMethod.POST, occupancyWriteRoles()),
    WAITLIST_READ("/v1/rms/waitlist-entries/**", HttpMethod.GET, guestPortalReadRoles()),
    WAITLIST_WRITE("/v1/rms/waitlist-entries", HttpMethod.POST, guestOccupancyWriteRoles()),
    WAITLIST_DELETE("/v1/rms/waitlist-entries/**", HttpMethod.DELETE, guestOccupancyWriteRoles()),

    RMS_REPORTS_READ("/v1/rms/reports/**", HttpMethod.GET, staffRestaurantReadRoles());

    private final String pathPattern;
    private final HttpMethod httpMethod;
    private final ApiSecurityLevel[] securityLevels;

    ApiEndpoint(String pathPattern, HttpMethod httpMethod, ApiSecurityLevel... securityLevels) {
        this.pathPattern = normalizePathPattern(pathPattern);
        this.httpMethod = httpMethod;
        this.securityLevels = securityLevels;
    }

    private static String normalizePathPattern(String pathPattern) {
        return pathPattern.replaceAll("\\{[^/]+}", "*");
    }

    /**
     * Staff-only RMS read access. Guest users are excluded from inventory, recipes, merge groups, etc.
     */
    public static ApiSecurityLevel[] staffRestaurantReadRoles() {
        return new ApiSecurityLevel[]{DIRECTOR, ADMIN, MANAGER, FRONT_DESK, ACCOUNTING};
    }

    /**
     * Guest portal read endpoints: menu browse, orders, tables/availability, table occupancies.
     */
    public static ApiSecurityLevel[] guestPortalReadRoles() {
        return new ApiSecurityLevel[]{DIRECTOR, ADMIN, MANAGER, FRONT_DESK, ACCOUNTING, GUEST};
    }

    public static ApiSecurityLevel[] guestOrderWriteRoles() {
        return new ApiSecurityLevel[]{DIRECTOR, ADMIN, MANAGER, FRONT_DESK, GUEST};
    }

    public static ApiSecurityLevel[] occupancyWriteRoles() {
        return new ApiSecurityLevel[]{DIRECTOR, ADMIN, MANAGER, FRONT_DESK};
    }

    public static ApiSecurityLevel[] kitchenRoles() {
        return new ApiSecurityLevel[]{DIRECTOR, ADMIN, MANAGER, FRONT_DESK, HOUSEKEEPING};
    }

    public static ApiSecurityLevel[] guestOccupancyWriteRoles() {
        return new ApiSecurityLevel[]{DIRECTOR, ADMIN, MANAGER, FRONT_DESK, GUEST};
    }

    /**
     * Staff roles for fallback authorization. {@link ApiSecurityLevel#GUEST} is intentionally excluded.
     */
    public static String[] staffRoleNames() {
        return new String[]{
                DIRECTOR.name(),
                ADMIN.name(),
                MANAGER.name(),
                FRONT_DESK.name(),
                HOUSEKEEPING.name(),
                ACCOUNTING.name()
        };
    }
}
