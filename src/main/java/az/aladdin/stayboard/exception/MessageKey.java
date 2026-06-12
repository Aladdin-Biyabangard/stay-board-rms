package az.aladdin.stayboard.exception;

public final class MessageKey {

    public static final String NOT_FOUND = "error.not_found";
    public static final String VALIDATION = "error.validation";
    public static final String VALIDATION_FIELD = "error.validation.field";
    public static final String UNAUTHORIZED = "error.unauthorized";
    public static final String FORBIDDEN = "error.forbidden";
    public static final String FORBIDDEN_GUEST_ORDER_MODIFICATION_WINDOW_EXPIRED =
            "error.forbidden.guest_order_modification_window_expired";
    public static final String FORBIDDEN_GUEST_ORDER_MODIFICATION_NOT_ALLOWED =
            "error.forbidden.guest_order_modification_not_allowed";
    public static final String FORBIDDEN_GUEST_TABLE_RESERVATION_NOT_ALLOWED =
            "error.forbidden.guest_table_reservation_not_allowed";
    public static final String FORBIDDEN_GUEST_WAITLIST_NOT_ALLOWED =
            "error.forbidden.guest_waitlist_not_allowed";
    public static final String HOTEL_CONTEXT_NOT_SET = "error.hotel_context_not_set";

    public static final String CONFLICT_MENU_CATEGORY_HAS_ITEMS = "error.conflict.menu_category_has_items";
    public static final String CONFLICT_TABLE_MERGED = "error.conflict.table_merged";
    public static final String CONFLICT_TABLE_HAS_ORDERS = "error.conflict.table_has_orders";
    public static final String CONFLICT_ORDER_HAS_ITEMS = "error.conflict.order_has_items";
    public static final String CONFLICT_TABLE_NOT_MERGEABLE = "error.conflict.table_not_mergeable";
    public static final String CONFLICT_TABLE_NOT_IN_MERGE_GROUP = "error.conflict.table_not_in_merge_group";

    public static final String BAD_REQUEST_PRIMARY_TABLE_NOT_IN_GROUP = "error.bad_request.primary_table_not_in_group";
    public static final String BAD_REQUEST_TABLES_NOT_FOUND = "error.bad_request.tables_not_found";
    public static final String BAD_REQUEST_WEIGHT_QUANTITY_REQUIRED = "error.bad_request.weight_quantity_required";
    public static final String BAD_REQUEST_QUANTITY_REQUIRED = "error.bad_request.quantity_required";
    public static final String BAD_REQUEST_INVALID_ORDER_ITEM_STATUS_TRANSITION = "error.bad_request.invalid_order_item_status_transition";
    public static final String BAD_REQUEST_INVALID_STOCK_QUANTITY = "error.bad_request.invalid_stock_quantity";
    public static final String BAD_REQUEST_COUNT_MUST_BE_WHOLE = "error.bad_request.count_must_be_whole";
    public static final String BAD_REQUEST_INVALID_STOCK_TRANSACTION_TYPE = "error.bad_request.invalid_stock_transaction_type";
    public static final String BAD_REQUEST_IMAGE_FILE_MIME_INVALID = "error.bad_request.image_file_mime_invalid";
    public static final String BAD_REQUEST_FILE_UPLOAD_FAILED = "error.bad_request.file_upload_failed";
    public static final String BAD_REQUEST_FOLIO_CHARGE_SYNC_FAILED = "error.bad_request.folio_charge_sync_failed";
    public static final String BAD_REQUEST_INVALID_TABLE_OCCUPANCY_WINDOW = "error.bad_request.invalid_table_occupancy_window";
    public static final String BAD_REQUEST_TABLE_RESERVATION_IN_PAST = "error.bad_request.table_reservation_in_past";
    public static final String BAD_REQUEST_TABLE_OCCUPANCY_SOURCE_REQUIRED = "error.bad_request.table_occupancy_source_required";
    public static final String BAD_REQUEST_TABLE_RESERVATION_BEYOND_STAY = "error.bad_request.table_reservation_beyond_stay";
    public static final String BAD_REQUEST_INVALID_WAITLIST_STATUS_TRANSITION =
            "error.bad_request.invalid_waitlist_status_transition";
    public static final String BAD_REQUEST_WAITLIST_ENTRY_NOT_ACTIVE =
            "error.bad_request.waitlist_entry_not_active";

    public static final String NOT_FOUND_IMAGE_NOT_IN_GALLERY = "error.not_found.image_not_in_gallery";

    public static final String CONFLICT_INSUFFICIENT_STOCK = "error.conflict.insufficient_stock";
    public static final String CONFLICT_INVENTORY_ITEM_HAS_RECIPES = "error.conflict.inventory_item_has_recipes";
    public static final String CONFLICT_RECIPE_ALREADY_EXISTS = "error.conflict.recipe_already_exists";
    public static final String CONFLICT_TABLE_NOT_AVAILABLE = "error.conflict.table_not_available";
    public static final String CONFLICT_WAITLIST_ALREADY_ACTIVE = "error.conflict.waitlist_already_active";
    public static final String CONFLICT_MODIFIER_GROUP_IN_USE = "error.conflict.modifier_group_in_use";

    public static final String BAD_REQUEST_INVALID_MODIFIER_OPTION = "error.bad_request.invalid_modifier_option";
    public static final String BAD_REQUEST_INVALID_MODIFIER_GROUP = "error.bad_request.invalid_modifier_group";
    public static final String BAD_REQUEST_MODIFIER_NOT_ALLOWED = "error.bad_request.modifier_not_allowed";
    public static final String BAD_REQUEST_MODIFIER_SELECTION_REQUIRED = "error.bad_request.modifier_selection_required";
    public static final String BAD_REQUEST_MODIFIER_SELECTION_MIN = "error.bad_request.modifier_selection_min";
    public static final String BAD_REQUEST_MODIFIER_SELECTION_MAX = "error.bad_request.modifier_selection_max";

    private MessageKey() {
    }
}
