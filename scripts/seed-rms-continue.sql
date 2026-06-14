-- RMS seed davam skripti
-- Problem: menu_categories insert edilmeyib, @cat_* = 1..4 movcud deyil
-- Bu skripti bir DEFA butun olaraq isledin (Adminer-de hamisini secib Run)

USE stay_board_rms;

SET @hotel_id = 1;
SET @user = 'seed-script';
SET @ts = UTC_TIMESTAMP();

-- ============================================================
-- 1. 4 KATEQORIYA (yoxdursa yarat)
-- ============================================================
INSERT INTO menu_categories (hotel_id, category_name, description, main_image_url, created_at, created_by, updated_at, updated_by)
SELECT @hotel_id, 'Sorbalar', 'Ev sorbalar', NULL, @ts, @user, @ts, @user
WHERE NOT EXISTS (
    SELECT 1 FROM menu_categories WHERE hotel_id = @hotel_id AND category_name = 'Sorbalar'
);

INSERT INTO menu_categories (hotel_id, category_name, description, main_image_url, created_at, created_by, updated_at, updated_by)
SELECT @hotel_id, 'Esas yemekler', 'Milli ve Avropa yemekleri', NULL, @ts, @user, @ts, @user
WHERE NOT EXISTS (
    SELECT 1 FROM menu_categories WHERE hotel_id = @hotel_id AND category_name = 'Esas yemekler'
);

INSERT INTO menu_categories (hotel_id, category_name, description, main_image_url, created_at, created_by, updated_at, updated_by)
SELECT @hotel_id, 'Salatlar', 'Taze salatlar', NULL, @ts, @user, @ts, @user
WHERE NOT EXISTS (
    SELECT 1 FROM menu_categories WHERE hotel_id = @hotel_id AND category_name = 'Salatlar'
);

INSERT INTO menu_categories (hotel_id, category_name, description, main_image_url, created_at, created_by, updated_at, updated_by)
SELECT @hotel_id, 'Desertler ve ickiler', 'Desertler, qehve ve soyuq ickiler', NULL, @ts, @user, @ts, @user
WHERE NOT EXISTS (
    SELECT 1 FROM menu_categories WHERE hotel_id = @hotel_id AND category_name = 'Desertler ve ickiler'
);

SELECT @cat_sorbalar := id FROM menu_categories WHERE hotel_id = @hotel_id AND category_name = 'Sorbalar' LIMIT 1;
SELECT @cat_esas    := id FROM menu_categories WHERE hotel_id = @hotel_id AND category_name = 'Esas yemekler' LIMIT 1;
SELECT @cat_salat   := id FROM menu_categories WHERE hotel_id = @hotel_id AND category_name = 'Salatlar' LIMIT 1;
SELECT @cat_desert  := id FROM menu_categories WHERE hotel_id = @hotel_id AND category_name = 'Desertler ve ickiler' LIMIT 1;

-- ============================================================
-- 2. MOVcUD ID-LƏRI DB-DEN GOTUR (evvel insert edilenler)
-- ============================================================
SELECT @alg_gluten := id FROM allergens WHERE hotel_id = @hotel_id AND allergen_name = 'Gluten' LIMIT 1;
SELECT @alg_sud    := id FROM allergens WHERE hotel_id = @hotel_id AND allergen_name = 'Sud mehsullari' LIMIT 1;
SELECT @alg_qoz    := id FROM allergens WHERE hotel_id = @hotel_id AND allergen_name = 'Qoz-findiq' LIMIT 1;

SELECT @diet_veg    := id FROM dietary_tags WHERE hotel_id = @hotel_id AND tag_name = 'Vegetarian' LIMIT 1;
SELECT @diet_vegan  := id FROM dietary_tags WHERE hotel_id = @hotel_id AND tag_name = 'Vegan' LIMIT 1;
SELECT @diet_halal  := id FROM dietary_tags WHERE hotel_id = @hotel_id AND tag_name = 'Halal' LIMIT 1;
SELECT @diet_lowcal := id FROM dietary_tags WHERE hotel_id = @hotel_id AND tag_name = 'Asagi kalori' LIMIT 1;

SELECT @mg_pisirme     := id FROM modifier_groups WHERE hotel_id = @hotel_id AND group_name = 'Pisirme derecesi' LIMIT 1;
SELECT @mg_garnir      := id FROM modifier_groups WHERE hotel_id = @hotel_id AND group_name = 'Garnir' LIMIT 1;
SELECT @mg_aciliq      := id FROM modifier_groups WHERE hotel_id = @hotel_id AND group_name = 'Aciliq' LIMIT 1;
SELECT @mg_sous        := id FROM modifier_groups WHERE hotel_id = @hotel_id AND group_name = 'Sous secimi' LIMIT 1;
SELECT @mg_pendir      := id FROM modifier_groups WHERE hotel_id = @hotel_id AND group_name = 'Elave pendir' LIMIT 1;
SELECT @mg_salat_sous  := id FROM modifier_groups WHERE hotel_id = @hotel_id AND group_name = 'Salat sousu' LIMIT 1;
SELECT @mg_sud         := id FROM modifier_groups WHERE hotel_id = @hotel_id AND group_name = 'Sud novu' LIMIT 1;
SELECT @mg_seker       := id FROM modifier_groups WHERE hotel_id = @hotel_id AND group_name = 'Seker miqdari' LIMIT 1;

SELECT @inv_duyu       := id FROM inventory_items WHERE hotel_id = @hotel_id AND sku = 'INV-001' LIMIT 1;
SELECT @inv_mal        := id FROM inventory_items WHERE hotel_id = @hotel_id AND sku = 'INV-002' LIMIT 1;
SELECT @inv_toyuq      := id FROM inventory_items WHERE hotel_id = @hotel_id AND sku = 'INV-003' LIMIT 1;
SELECT @inv_balig      := id FROM inventory_items WHERE hotel_id = @hotel_id AND sku = 'INV-004' LIMIT 1;
SELECT @inv_un         := id FROM inventory_items WHERE hotel_id = @hotel_id AND sku = 'INV-005' LIMIT 1;
SELECT @inv_yag        := id FROM inventory_items WHERE hotel_id = @hotel_id AND sku = 'INV-006' LIMIT 1;
SELECT @inv_sud        := id FROM inventory_items WHERE hotel_id = @hotel_id AND sku = 'INV-007' LIMIT 1;
SELECT @inv_pendir     := id FROM inventory_items WHERE hotel_id = @hotel_id AND sku = 'INV-008' LIMIT 1;
SELECT @inv_pomidor    := id FROM inventory_items WHERE hotel_id = @hotel_id AND sku = 'INV-009' LIMIT 1;
SELECT @inv_xiyar      := id FROM inventory_items WHERE hotel_id = @hotel_id AND sku = 'INV-010' LIMIT 1;
SELECT @inv_goyerti    := id FROM inventory_items WHERE hotel_id = @hotel_id AND sku = 'INV-011' LIMIT 1;
SELECT @inv_mercimek   := id FROM inventory_items WHERE hotel_id = @hotel_id AND sku = 'INV-012' LIMIT 1;
SELECT @inv_yogurt     := id FROM inventory_items WHERE hotel_id = @hotel_id AND sku = 'INV-013' LIMIT 1;
SELECT @inv_qehve      := id FROM inventory_items WHERE hotel_id = @hotel_id AND sku = 'INV-014' LIMIT 1;
SELECT @inv_sut_ayran  := id FROM inventory_items WHERE hotel_id = @hotel_id AND sku = 'INV-015' LIMIT 1;
SELECT @inv_limon      := id FROM inventory_items WHERE hotel_id = @hotel_id AND sku = 'INV-016' LIMIT 1;
SELECT @inv_seker      := id FROM inventory_items WHERE hotel_id = @hotel_id AND sku = 'INV-017' LIMIT 1;
SELECT @inv_yumurta    := id FROM inventory_items WHERE hotel_id = @hotel_id AND sku = 'INV-018' LIMIT 1;
SELECT @inv_findiq     := id FROM inventory_items WHERE hotel_id = @hotel_id AND sku = 'INV-019' LIMIT 1;
SELECT @inv_bal        := id FROM inventory_items WHERE hotel_id = @hotel_id AND sku = 'INV-020' LIMIT 1;

-- ============================================================
-- 3. 20 MENU ITEM (yalniz yoxdursa)
-- ============================================================
INSERT INTO menu_items (hotel_id, item_name, item_description, active, price, tax_rate, tax_type, sale_unit_type, menu_category_id, main_image_url, created_at, created_by, updated_at, updated_by)
SELECT @hotel_id, 'Duyu sorbasi', 'Ev duyu sorbasi', 1, 8.00, 18.00, 'INCLUDE', 'PORTION', @cat_sorbalar, NULL, @ts, @user, @ts, @user
WHERE NOT EXISTS (SELECT 1 FROM menu_items WHERE hotel_id = @hotel_id AND item_name = 'Duyu sorbasi');

INSERT INTO menu_items (hotel_id, item_name, item_description, active, price, tax_rate, tax_type, sale_unit_type, menu_category_id, main_image_url, created_at, created_by, updated_at, updated_by)
SELECT @hotel_id, 'Yayla corbasi', 'Yogurtlu corba', 1, 9.50, 18.00, 'INCLUDE', 'PORTION', @cat_sorbalar, NULL, @ts, @user, @ts, @user
WHERE NOT EXISTS (SELECT 1 FROM menu_items WHERE hotel_id = @hotel_id AND item_name = 'Yayla corbasi');

INSERT INTO menu_items (hotel_id, item_name, item_description, active, price, tax_rate, tax_type, sale_unit_type, menu_category_id, main_image_url, created_at, created_by, updated_at, updated_by)
SELECT @hotel_id, 'Mercimek corbasi', 'Qirmizi mercimek', 1, 7.00, 18.00, 'INCLUDE', 'PORTION', @cat_sorbalar, NULL, @ts, @user, @ts, @user
WHERE NOT EXISTS (SELECT 1 FROM menu_items WHERE hotel_id = @hotel_id AND item_name = 'Mercimek corbasi');

INSERT INTO menu_items (hotel_id, item_name, item_description, active, price, tax_rate, tax_type, sale_unit_type, menu_category_id, main_image_url, created_at, created_by, updated_at, updated_by)
SELECT @hotel_id, 'Toyuq bouillon', 'Toyuq sorbasi', 1, 8.50, 18.00, 'INCLUDE', 'PORTION', @cat_sorbalar, NULL, @ts, @user, @ts, @user
WHERE NOT EXISTS (SELECT 1 FROM menu_items WHERE hotel_id = @hotel_id AND item_name = 'Toyuq bouillon');

INSERT INTO menu_items (hotel_id, item_name, item_description, active, price, tax_rate, tax_type, sale_unit_type, menu_category_id, main_image_url, created_at, created_by, updated_at, updated_by)
SELECT @hotel_id, 'Dovga', 'Yogurtlu dovga', 1, 7.50, 18.00, 'INCLUDE', 'PORTION', @cat_sorbalar, NULL, @ts, @user, @ts, @user
WHERE NOT EXISTS (SELECT 1 FROM menu_items WHERE hotel_id = @hotel_id AND item_name = 'Dovga');

INSERT INTO menu_items (hotel_id, item_name, item_description, active, price, tax_rate, tax_type, sale_unit_type, menu_category_id, main_image_url, created_at, created_by, updated_at, updated_by)
SELECT @hotel_id, 'Plov', 'Azarbaycan plowu', 1, 18.00, 18.00, 'INCLUDE', 'PORTION', @cat_esas, NULL, @ts, @user, @ts, @user
WHERE NOT EXISTS (SELECT 1 FROM menu_items WHERE hotel_id = @hotel_id AND item_name = 'Plov');

INSERT INTO menu_items (hotel_id, item_name, item_description, active, price, tax_rate, tax_type, sale_unit_type, menu_category_id, main_image_url, created_at, created_by, updated_at, updated_by)
SELECT @hotel_id, 'Lule kabab', 'Mal eti lule', 1, 22.00, 18.00, 'INCLUDE', 'PORTION', @cat_esas, NULL, @ts, @user, @ts, @user
WHERE NOT EXISTS (SELECT 1 FROM menu_items WHERE hotel_id = @hotel_id AND item_name = 'Lule kabab');

INSERT INTO menu_items (hotel_id, item_name, item_description, active, price, tax_rate, tax_type, sale_unit_type, menu_category_id, main_image_url, created_at, created_by, updated_at, updated_by)
SELECT @hotel_id, 'Qovurma', 'Toyuq qovurmasi', 1, 20.00, 18.00, 'INCLUDE', 'PORTION', @cat_esas, NULL, @ts, @user, @ts, @user
WHERE NOT EXISTS (SELECT 1 FROM menu_items WHERE hotel_id = @hotel_id AND item_name = 'Qovurma');

INSERT INTO menu_items (hotel_id, item_name, item_description, active, price, tax_rate, tax_type, sale_unit_type, menu_category_id, main_image_url, created_at, created_by, updated_at, updated_by)
SELECT @hotel_id, 'Balig filesi', 'Levrek filesi', 1, 24.00, 18.00, 'INCLUDE', 'PORTION', @cat_esas, NULL, @ts, @user, @ts, @user
WHERE NOT EXISTS (SELECT 1 FROM menu_items WHERE hotel_id = @hotel_id AND item_name = 'Balig filesi');

INSERT INTO menu_items (hotel_id, item_name, item_description, active, price, tax_rate, tax_type, sale_unit_type, menu_category_id, main_image_url, created_at, created_by, updated_at, updated_by)
SELECT @hotel_id, 'Toyuq sasin', 'Toyuq shashlik', 1, 16.00, 18.00, 'INCLUDE', 'PORTION', @cat_esas, NULL, @ts, @user, @ts, @user
WHERE NOT EXISTS (SELECT 1 FROM menu_items WHERE hotel_id = @hotel_id AND item_name = 'Toyuq sasin');

INSERT INTO menu_items (hotel_id, item_name, item_description, active, price, tax_rate, tax_type, sale_unit_type, menu_category_id, main_image_url, created_at, created_by, updated_at, updated_by)
SELECT @hotel_id, 'Dolma', 'Yarpaq dolmasi', 1, 14.00, 18.00, 'INCLUDE', 'PORTION', @cat_esas, NULL, @ts, @user, @ts, @user
WHERE NOT EXISTS (SELECT 1 FROM menu_items WHERE hotel_id = @hotel_id AND item_name = 'Dolma');

INSERT INTO menu_items (hotel_id, item_name, item_description, active, price, tax_rate, tax_type, sale_unit_type, menu_category_id, main_image_url, created_at, created_by, updated_at, updated_by)
SELECT @hotel_id, 'Coban salati', 'Pomidor, xiyar, soan', 1, 10.00, 18.00, 'INCLUDE', 'PORTION', @cat_salat, NULL, @ts, @user, @ts, @user
WHERE NOT EXISTS (SELECT 1 FROM menu_items WHERE hotel_id = @hotel_id AND item_name = 'Coban salati');

INSERT INTO menu_items (hotel_id, item_name, item_description, active, price, tax_rate, tax_type, sale_unit_type, menu_category_id, main_image_url, created_at, created_by, updated_at, updated_by)
SELECT @hotel_id, 'Sezar salati', 'Toyuq sezar', 1, 14.00, 18.00, 'INCLUDE', 'PORTION', @cat_salat, NULL, @ts, @user, @ts, @user
WHERE NOT EXISTS (SELECT 1 FROM menu_items WHERE hotel_id = @hotel_id AND item_name = 'Sezar salati');

INSERT INTO menu_items (hotel_id, item_name, item_description, active, price, tax_rate, tax_type, sale_unit_type, menu_category_id, main_image_url, created_at, created_by, updated_at, updated_by)
SELECT @hotel_id, 'Goyerti salati', 'Taze goyerti', 1, 9.00, 18.00, 'INCLUDE', 'PORTION', @cat_salat, NULL, @ts, @user, @ts, @user
WHERE NOT EXISTS (SELECT 1 FROM menu_items WHERE hotel_id = @hotel_id AND item_name = 'Goyerti salati');

INSERT INTO menu_items (hotel_id, item_name, item_description, active, price, tax_rate, tax_type, sale_unit_type, menu_category_id, main_image_url, created_at, created_by, updated_at, updated_by)
SELECT @hotel_id, 'Yunan salati', 'Feta pendirli', 1, 12.00, 18.00, 'INCLUDE', 'PORTION', @cat_salat, NULL, @ts, @user, @ts, @user
WHERE NOT EXISTS (SELECT 1 FROM menu_items WHERE hotel_id = @hotel_id AND item_name = 'Yunan salati');

INSERT INTO menu_items (hotel_id, item_name, item_description, active, price, tax_rate, tax_type, sale_unit_type, menu_category_id, main_image_url, created_at, created_by, updated_at, updated_by)
SELECT @hotel_id, 'Paxlava', 'Findiqli paxlava', 1, 6.00, 18.00, 'INCLUDE', 'PIECE', @cat_desert, NULL, @ts, @user, @ts, @user
WHERE NOT EXISTS (SELECT 1 FROM menu_items WHERE hotel_id = @hotel_id AND item_name = 'Paxlava');

INSERT INTO menu_items (hotel_id, item_name, item_description, active, price, tax_rate, tax_type, sale_unit_type, menu_category_id, main_image_url, created_at, created_by, updated_at, updated_by)
SELECT @hotel_id, 'Sekerbura', 'Badamli sekerbura', 1, 4.00, 18.00, 'INCLUDE', 'PIECE', @cat_desert, NULL, @ts, @user, @ts, @user
WHERE NOT EXISTS (SELECT 1 FROM menu_items WHERE hotel_id = @hotel_id AND item_name = 'Sekerbura');

INSERT INTO menu_items (hotel_id, item_name, item_description, active, price, tax_rate, tax_type, sale_unit_type, menu_category_id, main_image_url, created_at, created_by, updated_at, updated_by)
SELECT @hotel_id, 'Cappuccino', 'Espresso cappuccino', 1, 5.00, 18.00, 'INCLUDE', 'PIECE', @cat_desert, NULL, @ts, @user, @ts, @user
WHERE NOT EXISTS (SELECT 1 FROM menu_items WHERE hotel_id = @hotel_id AND item_name = 'Cappuccino');

INSERT INTO menu_items (hotel_id, item_name, item_description, active, price, tax_rate, tax_type, sale_unit_type, menu_category_id, main_image_url, created_at, created_by, updated_at, updated_by)
SELECT @hotel_id, 'Ayran', 'Ev ayrani', 1, 3.00, 18.00, 'INCLUDE', 'PIECE', @cat_desert, NULL, @ts, @user, @ts, @user
WHERE NOT EXISTS (SELECT 1 FROM menu_items WHERE hotel_id = @hotel_id AND item_name = 'Ayran');

INSERT INTO menu_items (hotel_id, item_name, item_description, active, price, tax_rate, tax_type, sale_unit_type, menu_category_id, main_image_url, created_at, created_by, updated_at, updated_by)
SELECT @hotel_id, 'Limonad', 'Taze limonad', 1, 4.50, 18.00, 'INCLUDE', 'PIECE', @cat_desert, NULL, @ts, @user, @ts, @user
WHERE NOT EXISTS (SELECT 1 FROM menu_items WHERE hotel_id = @hotel_id AND item_name = 'Limonad');

-- Menu item ID-ləri
SELECT @mi_duyu_sorbasi  := id FROM menu_items WHERE hotel_id = @hotel_id AND item_name = 'Duyu sorbasi' LIMIT 1;
SELECT @mi_yayla         := id FROM menu_items WHERE hotel_id = @hotel_id AND item_name = 'Yayla corbasi' LIMIT 1;
SELECT @mi_mercimek      := id FROM menu_items WHERE hotel_id = @hotel_id AND item_name = 'Mercimek corbasi' LIMIT 1;
SELECT @mi_bouillon      := id FROM menu_items WHERE hotel_id = @hotel_id AND item_name = 'Toyuq bouillon' LIMIT 1;
SELECT @mi_dovga         := id FROM menu_items WHERE hotel_id = @hotel_id AND item_name = 'Dovga' LIMIT 1;
SELECT @mi_plov          := id FROM menu_items WHERE hotel_id = @hotel_id AND item_name = 'Plov' LIMIT 1;
SELECT @mi_lule          := id FROM menu_items WHERE hotel_id = @hotel_id AND item_name = 'Lule kabab' LIMIT 1;
SELECT @mi_qovurma       := id FROM menu_items WHERE hotel_id = @hotel_id AND item_name = 'Qovurma' LIMIT 1;
SELECT @mi_balig         := id FROM menu_items WHERE hotel_id = @hotel_id AND item_name = 'Balig filesi' LIMIT 1;
SELECT @mi_toyuq_sasin   := id FROM menu_items WHERE hotel_id = @hotel_id AND item_name = 'Toyuq sasin' LIMIT 1;
SELECT @mi_dolma         := id FROM menu_items WHERE hotel_id = @hotel_id AND item_name = 'Dolma' LIMIT 1;
SELECT @mi_coban         := id FROM menu_items WHERE hotel_id = @hotel_id AND item_name = 'Coban salati' LIMIT 1;
SELECT @mi_sezar         := id FROM menu_items WHERE hotel_id = @hotel_id AND item_name = 'Sezar salati' LIMIT 1;
SELECT @mi_goyerti       := id FROM menu_items WHERE hotel_id = @hotel_id AND item_name = 'Goyerti salati' LIMIT 1;
SELECT @mi_yunan         := id FROM menu_items WHERE hotel_id = @hotel_id AND item_name = 'Yunan salati' LIMIT 1;
SELECT @mi_paxlava       := id FROM menu_items WHERE hotel_id = @hotel_id AND item_name = 'Paxlava' LIMIT 1;
SELECT @mi_sekerbura     := id FROM menu_items WHERE hotel_id = @hotel_id AND item_name = 'Sekerbura' LIMIT 1;
SELECT @mi_cappuccino    := id FROM menu_items WHERE hotel_id = @hotel_id AND item_name = 'Cappuccino' LIMIT 1;
SELECT @mi_ayran         := id FROM menu_items WHERE hotel_id = @hotel_id AND item_name = 'Ayran' LIMIT 1;
SELECT @mi_limonad       := id FROM menu_items WHERE hotel_id = @hotel_id AND item_name = 'Limonad' LIMIT 1;

-- ============================================================
-- 4. ALLERqEN ELAQELERI
-- ============================================================
INSERT IGNORE INTO menu_item_allergens (menu_item_id, allergen_id) VALUES
(@mi_yayla, @alg_gluten),
(@mi_yayla, @alg_sud),
(@mi_bouillon, @alg_gluten),
(@mi_dovga, @alg_sud),
(@mi_lule, @alg_gluten),
(@mi_sezar, @alg_sud),
(@mi_sezar, @alg_gluten),
(@mi_yunan, @alg_sud),
(@mi_paxlava, @alg_gluten),
(@mi_paxlava, @alg_qoz),
(@mi_sekerbura, @alg_gluten),
(@mi_sekerbura, @alg_qoz),
(@mi_cappuccino, @alg_sud),
(@mi_ayran, @alg_sud);

-- ============================================================
-- 5. PEHRIZ ELAQELERI
-- ============================================================
INSERT IGNORE INTO menu_item_dietary_tags (menu_item_id, dietary_tag_id) VALUES
(@mi_duyu_sorbasi, @diet_halal),
(@mi_mercimek, @diet_veg),
(@mi_mercimek, @diet_halal),
(@mi_bouillon, @diet_halal),
(@mi_dovga, @diet_veg),
(@mi_dovga, @diet_halal),
(@mi_coban, @diet_veg),
(@mi_coban, @diet_lowcal),
(@mi_goyerti, @diet_veg),
(@mi_yunan, @diet_veg),
(@mi_cappuccino, @diet_vegan),
(@mi_ayran, @diet_halal),
(@mi_limonad, @diet_vegan);

-- ============================================================
-- 6. MODIFIKATOR ELAQELERI
-- ============================================================
INSERT IGNORE INTO menu_item_modifier_groups (hotel_id, menu_item_id, modifier_group_id, sort_order) VALUES
(@hotel_id, @mi_lule, @mg_pisirme, 1),
(@hotel_id, @mi_lule, @mg_garnir, 2),
(@hotel_id, @mi_qovurma, @mg_aciliq, 1),
(@hotel_id, @mi_qovurma, @mg_garnir, 2),
(@hotel_id, @mi_balig, @mg_pisirme, 1),
(@hotel_id, @mi_balig, @mg_sous, 2),
(@hotel_id, @mi_toyuq_sasin, @mg_aciliq, 1),
(@hotel_id, @mi_coban, @mg_salat_sous, 1),
(@hotel_id, @mi_sezar, @mg_pendir, 1),
(@hotel_id, @mi_sezar, @mg_salat_sous, 2),
(@hotel_id, @mi_cappuccino, @mg_sud, 1),
(@hotel_id, @mi_cappuccino, @mg_seker, 2),
(@hotel_id, @mi_limonad, @mg_seker, 1);

-- ============================================================
-- 7. RESEPTLER
-- ============================================================
INSERT IGNORE INTO recipes (hotel_id, menu_item_id, inventory_item_id, quantity_per_serving, created_at, created_by, updated_at, updated_by) VALUES
(@hotel_id, @mi_duyu_sorbasi, @inv_duyu, 0.0800, @ts, @user, @ts, @user),
(@hotel_id, @mi_duyu_sorbasi, @inv_yag, 0.0100, @ts, @user, @ts, @user),
(@hotel_id, @mi_yayla, @inv_yogurt, 0.1500, @ts, @user, @ts, @user),
(@hotel_id, @mi_yayla, @inv_un, 0.0200, @ts, @user, @ts, @user),
(@hotel_id, @mi_yayla, @inv_yumurta, 1.0000, @ts, @user, @ts, @user),
(@hotel_id, @mi_mercimek, @inv_mercimek, 0.1000, @ts, @user, @ts, @user),
(@hotel_id, @mi_mercimek, @inv_yag, 0.0150, @ts, @user, @ts, @user),
(@hotel_id, @mi_bouillon, @inv_toyuq, 0.1200, @ts, @user, @ts, @user),
(@hotel_id, @mi_bouillon, @inv_goyerti, 0.0100, @ts, @user, @ts, @user),
(@hotel_id, @mi_dovga, @inv_yogurt, 0.2000, @ts, @user, @ts, @user),
(@hotel_id, @mi_dovga, @inv_goyerti, 0.0200, @ts, @user, @ts, @user),
(@hotel_id, @mi_plov, @inv_duyu, 0.1500, @ts, @user, @ts, @user),
(@hotel_id, @mi_plov, @inv_mal, 0.1000, @ts, @user, @ts, @user),
(@hotel_id, @mi_plov, @inv_yag, 0.0200, @ts, @user, @ts, @user),
(@hotel_id, @mi_lule, @inv_mal, 0.1800, @ts, @user, @ts, @user),
(@hotel_id, @mi_lule, @inv_yag, 0.0100, @ts, @user, @ts, @user),
(@hotel_id, @mi_qovurma, @inv_toyuq, 0.2000, @ts, @user, @ts, @user),
(@hotel_id, @mi_qovurma, @inv_yag, 0.0150, @ts, @user, @ts, @user),
(@hotel_id, @mi_balig, @inv_balig, 0.2200, @ts, @user, @ts, @user),
(@hotel_id, @mi_balig, @inv_yag, 0.0100, @ts, @user, @ts, @user),
(@hotel_id, @mi_toyuq_sasin, @inv_toyuq, 0.1800, @ts, @user, @ts, @user),
(@hotel_id, @mi_toyuq_sasin, @inv_yag, 0.0100, @ts, @user, @ts, @user),
(@hotel_id, @mi_dolma, @inv_mal, 0.0800, @ts, @user, @ts, @user),
(@hotel_id, @mi_dolma, @inv_yogurt, 0.0500, @ts, @user, @ts, @user),
(@hotel_id, @mi_coban, @inv_pomidor, 0.1000, @ts, @user, @ts, @user),
(@hotel_id, @mi_coban, @inv_xiyar, 0.0800, @ts, @user, @ts, @user),
(@hotel_id, @mi_coban, @inv_yag, 0.0050, @ts, @user, @ts, @user),
(@hotel_id, @mi_sezar, @inv_toyuq, 0.1000, @ts, @user, @ts, @user),
(@hotel_id, @mi_sezar, @inv_pendir, 0.0300, @ts, @user, @ts, @user),
(@hotel_id, @mi_sezar, @inv_yumurta, 0.5000, @ts, @user, @ts, @user),
(@hotel_id, @mi_goyerti, @inv_goyerti, 0.1200, @ts, @user, @ts, @user),
(@hotel_id, @mi_goyerti, @inv_yag, 0.0050, @ts, @user, @ts, @user),
(@hotel_id, @mi_yunan, @inv_pomidor, 0.0800, @ts, @user, @ts, @user),
(@hotel_id, @mi_yunan, @inv_pendir, 0.0400, @ts, @user, @ts, @user),
(@hotel_id, @mi_yunan, @inv_yag, 0.0100, @ts, @user, @ts, @user),
(@hotel_id, @mi_paxlava, @inv_un, 0.0500, @ts, @user, @ts, @user),
(@hotel_id, @mi_paxlava, @inv_findiq, 0.0300, @ts, @user, @ts, @user),
(@hotel_id, @mi_paxlava, @inv_bal, 0.0200, @ts, @user, @ts, @user),
(@hotel_id, @mi_sekerbura, @inv_un, 0.0400, @ts, @user, @ts, @user),
(@hotel_id, @mi_sekerbura, @inv_findiq, 0.0200, @ts, @user, @ts, @user),
(@hotel_id, @mi_cappuccino, @inv_qehve, 0.0180, @ts, @user, @ts, @user),
(@hotel_id, @mi_cappuccino, @inv_sud, 0.1500, @ts, @user, @ts, @user),
(@hotel_id, @mi_ayran, @inv_sut_ayran, 0.2500, @ts, @user, @ts, @user),
(@hotel_id, @mi_ayran, @inv_yogurt, 0.0500, @ts, @user, @ts, @user),
(@hotel_id, @mi_limonad, @inv_limon, 1.0000, @ts, @user, @ts, @user),
(@hotel_id, @mi_limonad, @inv_seker, 0.0300, @ts, @user, @ts, @user);

-- ============================================================
-- 8. YOXLAMA
-- ============================================================
SELECT 'menu_categories' AS cedvel, COUNT(*) AS say FROM menu_categories WHERE hotel_id = @hotel_id
UNION ALL SELECT 'menu_items', COUNT(*) FROM menu_items WHERE hotel_id = @hotel_id
UNION ALL SELECT 'allergens', COUNT(*) FROM allergens WHERE hotel_id = @hotel_id
UNION ALL SELECT 'dietary_tags', COUNT(*) FROM dietary_tags WHERE hotel_id = @hotel_id
UNION ALL SELECT 'modifier_groups', COUNT(*) FROM modifier_groups WHERE hotel_id = @hotel_id
UNION ALL SELECT 'inventory_items', COUNT(*) FROM inventory_items WHERE hotel_id = @hotel_id
UNION ALL SELECT 'recipes', COUNT(*) FROM recipes WHERE hotel_id = @hotel_id
UNION ALL SELECT 'tables', COUNT(*) FROM `tables` WHERE hotel_id = @hotel_id;
