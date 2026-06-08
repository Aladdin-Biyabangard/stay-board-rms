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

    MENU_CATEGORY_IMAGE_UPLOAD("/v1/rms/menu-categories/*/images", HttpMethod.POST, MANAGER, ADMIN, DIRECTOR),
    MENU_CATEGORY_IMAGE_DELETE("/v1/rms/menu-categories/*/images", HttpMethod.DELETE, MANAGER, ADMIN, DIRECTOR),
    MENU_CATEGORY_SET_MAIN_IMAGE("/v1/rms/menu-categories/*/images/main", HttpMethod.PUT, MANAGER, ADMIN, DIRECTOR),

    MENU_CATEGORIES_READ("/v1/rms/menu-categories/**", HttpMethod.GET, restaurantReadRoles()),
    MENU_CATEGORIES_WRITE("/v1/rms/menu-categories/**", HttpMethod.POST, MANAGER, ADMIN, DIRECTOR),
    MENU_CATEGORIES_UPDATE("/v1/rms/menu-categories/**", HttpMethod.PUT, MANAGER, ADMIN, DIRECTOR),
    MENU_CATEGORIES_PATCH("/v1/rms/menu-categories/**", HttpMethod.PATCH, MANAGER, ADMIN, DIRECTOR),
    MENU_CATEGORIES_DELETE("/v1/rms/menu-categories/**", HttpMethod.DELETE, MANAGER, ADMIN, DIRECTOR),

    MENU_ITEM_IMAGE_UPLOAD("/v1/rms/menu-items/*/images", HttpMethod.POST, MANAGER, ADMIN, DIRECTOR),
    MENU_ITEM_IMAGE_DELETE("/v1/rms/menu-items/*/images", HttpMethod.DELETE, MANAGER, ADMIN, DIRECTOR),
    MENU_ITEM_SET_MAIN_IMAGE("/v1/rms/menu-items/*/images/main", HttpMethod.PUT, MANAGER, ADMIN, DIRECTOR),

    MENU_ITEMS_READ("/v1/rms/menu-items/**", HttpMethod.GET, restaurantReadRoles()),
    MENU_ITEMS_WRITE("/v1/rms/menu-items/**", HttpMethod.POST, MANAGER, ADMIN, DIRECTOR),
    MENU_ITEMS_UPDATE("/v1/rms/menu-items/**", HttpMethod.PUT, MANAGER, ADMIN, DIRECTOR),
    MENU_ITEMS_PATCH("/v1/rms/menu-items/**", HttpMethod.PATCH, MANAGER, ADMIN, DIRECTOR),
    MENU_ITEMS_DELETE("/v1/rms/menu-items/**", HttpMethod.DELETE, MANAGER, ADMIN, DIRECTOR),

    TABLE_MERGE_READ("/v1/rms/tables/*/merge-group", HttpMethod.GET, restaurantReadRoles()),
    TABLE_MERGE_WRITE("/v1/rms/tables/merge", HttpMethod.POST, occupancyWriteRoles()),
    TABLE_MERGE_DELETE("/v1/rms/tables/*/merge", HttpMethod.DELETE, occupancyWriteRoles()),

    TABLES_READ("/v1/rms/tables/**", HttpMethod.GET, restaurantReadRoles()),
    TABLES_WRITE("/v1/rms/tables/**", HttpMethod.POST, MANAGER, ADMIN, DIRECTOR),
    TABLES_UPDATE("/v1/rms/tables/**", HttpMethod.PUT, MANAGER, ADMIN, DIRECTOR),
    TABLES_PATCH("/v1/rms/tables/**", HttpMethod.PATCH, MANAGER, ADMIN, DIRECTOR),
    TABLES_DELETE("/v1/rms/tables/**", HttpMethod.DELETE, MANAGER, ADMIN, DIRECTOR),

    ORDERS_READ("/v1/rms/orders/**", HttpMethod.GET, restaurantReadRoles()),
    ORDERS_WRITE("/v1/rms/orders/**", HttpMethod.POST, guestOrderWriteRoles()),
    ORDERS_UPDATE("/v1/rms/orders/**", HttpMethod.PUT, guestOrderWriteRoles()),
    ORDERS_PATCH("/v1/rms/orders/**", HttpMethod.PATCH, guestOrderWriteRoles()),
    ORDERS_DELETE("/v1/rms/orders/**", HttpMethod.DELETE, MANAGER, ADMIN, DIRECTOR),

    ORDER_ITEMS_READ("/v1/rms/order-items/**", HttpMethod.GET, restaurantReadRoles()),
    ORDER_ITEMS_WRITE("/v1/rms/order-items/**", HttpMethod.POST, guestOrderWriteRoles()),
    ORDER_ITEMS_UPDATE("/v1/rms/order-items/**", HttpMethod.PUT, guestOrderWriteRoles()),
    ORDER_ITEMS_PATCH("/v1/rms/order-items/**", HttpMethod.PATCH, guestOrderWriteRoles()),
    ORDER_ITEMS_DELETE("/v1/rms/order-items/**", HttpMethod.DELETE, guestOrderWriteRoles()),

    KITCHEN_TICKETS_READ("/v1/rms/kitchen/tickets/**", HttpMethod.GET, kitchenRoles()),
    KITCHEN_TICKETS_UPDATE("/v1/rms/kitchen/tickets/*/status", HttpMethod.PATCH, kitchenRoles()),

    INVENTORY_STOCK_ADJUST("/v1/rms/inventory-items/*/stock", HttpMethod.POST, MANAGER, ADMIN, DIRECTOR),

    INVENTORY_ITEMS_READ("/v1/rms/inventory-items/**", HttpMethod.GET, restaurantReadRoles()),
    INVENTORY_ITEMS_WRITE("/v1/rms/inventory-items/**", HttpMethod.POST, MANAGER, ADMIN, DIRECTOR),
    INVENTORY_ITEMS_UPDATE("/v1/rms/inventory-items/**", HttpMethod.PUT, MANAGER, ADMIN, DIRECTOR),
    INVENTORY_ITEMS_PATCH("/v1/rms/inventory-items/**", HttpMethod.PATCH, MANAGER, ADMIN, DIRECTOR),
    INVENTORY_ITEMS_DELETE("/v1/rms/inventory-items/**", HttpMethod.DELETE, MANAGER, ADMIN, DIRECTOR),

    RECIPES_READ("/v1/rms/recipes/**", HttpMethod.GET, restaurantReadRoles()),
    RECIPES_WRITE("/v1/rms/recipes/**", HttpMethod.POST, MANAGER, ADMIN, DIRECTOR),
    RECIPES_UPDATE("/v1/rms/recipes/**", HttpMethod.PUT, MANAGER, ADMIN, DIRECTOR),
    RECIPES_DELETE("/v1/rms/recipes/**", HttpMethod.DELETE, MANAGER, ADMIN, DIRECTOR),

    TABLE_OCCUPANCY_READ("/v1/rms/table-occupancies/**", HttpMethod.GET, restaurantReadRoles()),
    TABLE_OCCUPANCY_WRITE("/v1/rms/table-occupancies/**", HttpMethod.POST, guestOccupancyWriteRoles()),
    TABLE_OCCUPANCY_UPDATE("/v1/rms/table-occupancies/**", HttpMethod.PUT, occupancyWriteRoles()),
    TABLE_OCCUPANCY_PATCH("/v1/rms/table-occupancies/**", HttpMethod.PATCH, occupancyWriteRoles()),
    TABLE_OCCUPANCY_DELETE("/v1/rms/table-occupancies/**", HttpMethod.DELETE, guestOccupancyWriteRoles());

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

    public static ApiSecurityLevel[] restaurantReadRoles() {
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
}
