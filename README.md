# StayBoard RMS

A restaurant operations backend for StayBoard — so a hotel can run the dining room from the same stay as the front desk, not from a separate system.

## Overview

StayBoard RMS is the restaurant module of [StayBoard](https://stayboard.app), a hospitality platform for properties that still keep reservations, restaurant tickets and guest charges in different tools.

This service is for hotel and restaurant staff: waiters, kitchen, managers and the front desk. It takes a table, an order and a guest stay and treats them as one operational fact. When a guest orders from the restaurant, the charge can post to the same folio the front desk already has open.

It exists because a kitchen ticket that never becomes a folio line is how hotels lose money and arguments at night audit.

## Key Features

- **Tables and seating** — availability, occupancy, table merge and a waitlist
- **Menu** — categories, items, modifiers, allergens and dietary tags
- **Orders** — order and item lifecycle, pricing, tax and status for the floor and kitchen
- **Kitchen** — kitchen tickets and print support
- **Inventory and recipes** — stock items, consumption and recipe links
- **Guest folio sync** — restaurant charges posted to (and voided on) the StayBoard PMS folio by room
- **Reporting** — daily statistics, sales summary, sales by category and top items
- **Hotel-aware access** — JWT auth, role-based endpoints (director, admin, front desk, housekeeping, manager, accounting, guest) and per-hotel context
- **Multilingual API messages** — Azerbaijani, English, Russian and Georgian
- **Operations** — rate limiting, database backup/restore helpers and Docker deploy

## How It Works

```text
Guest / waiter
        ↓
StayBoard operator screens
        ↓
stay-board-rms  (this service)
        ↓
MySQL  +  StayBoard PMS (rooms, guests, folio)
```

A seating or an in-room order becomes an RMS order. The kitchen sees the ticket. Where the guest is in-house, the charge can write to the PMS folio instead of a second bill.

## My Role

I designed and built this backend as part of StayBoard: the restaurant data model, the APIs staff workflows call, and the integration with the hotel PMS so restaurant activity and the stay stay on one record.

## Technical Details

| Area | Choice |
|------|--------|
| Language | Java 25 |
| Framework | Spring Boot 4, Spring Web MVC, Spring Security, Spring Data JPA |
| Integration | OpenFeign to StayBoard PMS (hotel, reservation, guest stay, folio) |
| Database | MySQL |
| Auth | JWT, hotel context filter, role-based route security |
| Files | AWS S3 |
| Other | Validation, mail, Bucket4j rate limits, OpenPDF / Flying Saucer for receipts |
| Delivery | Gradle, Docker, GitHub Actions → Docker Hub → server compose |

Architecture in short: hotel-scoped repositories, a kitchen/order/menu/inventory domain, and a PMS port layer (`FolioPort` and Feign clients) so this service does not own guest and room records.

## Getting Started

This is a backend API. It expects MySQL and a reachable StayBoard PMS URL.

```bash
git clone https://github.com/Aladdin-Alizade/stay-board-rms.git
cd stay-board-rms
./gradlew bootRun
```

Configure database, security and `STAY_BOARD_URL` through the Spring config files and environment (see `src/main/resources/`). Default HTTP port in Docker is `8080`.

```bash
docker build -t stay-board-rms .
```

Production deploy is tag-driven (`v…`) via `.github/workflows/deploy-rms.yml`. Details are in [`docs/CI-CD-GUIDE.md`](docs/CI-CD-GUIDE.md).

## Project Status

In production as the restaurant module of StayBoard. This repository is the RMS service only — not the PMS UI, landing site or guest portal.
