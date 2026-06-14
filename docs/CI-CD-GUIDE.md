# CI/CD bələdçisi (GitHub Actions)

Bu sənəd StayBoard ekosistemində avtomatik build, push və deploy-un necə işlədiyini izah edir.

**Bu layihə:** `stay-board-rms`  
**Workflow:** `.github/workflows/deploy-rms.yml` — **Build and Deploy RMS**

---

## Qısa icmal: nə baş verir?

1. Siz **tag** push edirsiniz (məs. `v1.0.0.9`) və ya Actions-dan manual run edirsiniz.
2. GitHub runner **Docker image** build edir və **Docker Hub**-a push edir.
3. Runner **SSH** ilə serverə qoşulur (`/root/hotel`).
4. Serverdə `.env` faylında **`RMS_IMAGE`** yenilənir.
5. `docker compose pull rms` + `docker compose up -d rms` işləyir.

**Sizin manual addımlarınız artıq lazım deyil:** lokal build, `.env` edit, SSH, `docker compose up`.

---

## Layihələr və deploy hədəfləri

| Layihə | GitHub repo | Image adı | `.env` açarı | Compose servisi |
|--------|-------------|-----------|--------------|-----------------|
| PMS Backend | `stay-board` | `hotel-pms-backend` | `BACKEND_IMAGE` | `backend` |
| PMS Frontend | `stay-board-ui` | `hotel-pms-frontend` | `FRONTEND_IMAGE` | `frontend` |
| Landing | `stay-board-landing` | `hotel-pms-landing` | `LANDING_IMAGE` | `landing` |
| RMS | `stay-board-rms` | `stay-board-rms` | `RMS_IMAGE` | `rms` |

Tam image formatı: `ingressgroup/<image-adı>:<tag>`  
Məsələn: `ingressgroup/stay-board-rms:v1.0.0.9`

---

## GitHub Secrets (hər 4 repo-da)

**Settings → Secrets and variables → Actions**

| Secret | Nədir | Nümunə |
|--------|-------|--------|
| `DOCKERHUB_USERNAME` | Docker Hub user | `ingressgroup` |
| `DOCKERHUB_TOKEN` | Docker Hub access token | (token) |
| `DEPLOY_HOST` | Server IP / host | `65.21.51.215` |
| `DEPLOY_PORT` | SSH port | `2222` |
| `DEPLOY_USER` | SSH user | `test` |
| `DEPLOY_PASSWORD` | SSH parol | (parol) |
| `DEPLOY_PATH` | Serverdə compose + `.env` qovluğu | `/root/hotel` |

**Yalnız `stay-board-ui` üçün əlavə (opsional):**

| Secret | Default |
|--------|---------|
| `FRONTEND_API_URL` | `http://65.21.51.215:8020/v1` |
| `FRONTEND_RMS_API_URL` | `http://65.21.51.215:8050/v1/rms` |

---

## Deploy necə edilir?

### Tag ilə (tövsiyə)

```bash
git tag v1.0.0.9
git push origin v1.0.0.9    # branch: master və ya main
```

Tag **`v` ilə başlamalıdır** (məs. `v1.0.0.9`).

### Manual (Actions)

GitHub → **Actions** → workflow adı → **Run workflow** → tag yazın.

### Prosesi izləmək

GitHub → **Actions** → son run → addım logları.

Serverdə yoxlama:

```bash
ssh -p 2222 test@65.21.51.215 'grep RMS_IMAGE /root/hotel/.env && docker ps --filter name=stay-board-rms'
```

---

## Server dəyişəndə nə etməlisiniz?

Yeni server, IP, port, user və ya deploy qovluğu olanda:

### 1. GitHub Secrets (hər 4 repo-da yeniləyin)

| Dəyişən | Secret |
|---------|--------|
| Yeni IP | `DEPLOY_HOST` |
| Yeni SSH port | `DEPLOY_PORT` |
| Yeni SSH user | `DEPLOY_USER` |
| Yeni parol | `DEPLOY_PASSWORD` |
| Yeni qovluq (`.env` haradadırsa) | `DEPLOY_PATH` |

**Kod dəyişməyə ehtiyac yoxdur** — yalnız Secrets kifayətdir.

### 2. Yeni serverdə bir dəfəlik hazırlıq

- Docker + Docker Compose quraşdırın
- `docker-compose.prod.yml` və `.env` köçürün (məs. `/root/hotel`)
- Deploy user (`test`) üçün:
  - `docker` qrupuna əlavə
  - `/root/hotel` (və ya `DEPLOY_PATH`) üçün icazə (`setfacl`)
- `docker network create hotel-pms-network` (əgər yoxdursa)

### 3. Frontend API URL-ləri dəyişirsə

`stay-board-ui` repo-da Secrets:

- `FRONTEND_API_URL` — backend API
- `FRONTEND_RMS_API_URL` — RMS API

Sonra frontend-i yenidən tag ilə deploy edin (build zamanı URL-lər image-ə yazılır).

---

## Image adı və ya Docker Hub repo dəyişəndə

Məsələn: `ingressgroup/hotel-pms-backend` → `myorg/stayboard-api`

### Hər layihədə (müvafiq repo)

| # | Fayl | Nə dəyişir |
|---|------|------------|
| 1 | `.github/workflows/deploy-*.yml` | `env: DOCKER_REPO` və `IMAGE_NAME` |
| 2 | `scripts/build-and-push.sh` | Image adı sətri (`.../hotel-pms-backend:...`) |

**Bu layihə (`stay-board-rms`) üçün:**

- Workflow: `IMAGE_NAME: stay-board-rms`
- Skript: `stay-board-rms:${TAG}`
- `.env` açarı: `RMS_IMAGE` (workflow-da `ENV_KEY`)

### Serverdə

`/root/hotel/.env` — pipeline avtomatik yeniləyir; əl ilə dəyişməyə ehtiyac yoxdur.

### `stay-board` repo (opsional default)

`docker-compose.prod.yml` — default image sətirləri (`.env` olmasa fallback):

```yaml
image: ${RMS_IMAGE:-ingressgroup/stay-board-rms:v1.0.0}
```

Prod-da `.env` istifadə olunur; compose default-ları da uyğunlaşdırmaq yaxşıdır.

---

## Docker Hub hesabı dəyişəndə

Hər 4 repo-da Secrets:

- `DOCKERHUB_USERNAME`
- `DOCKERHUB_TOKEN`

Workflow build addımında `gradle.properties`-ə yazır — repoda parol saxlamayın.

---

## Tez-tez rast gəlinən xətalar

| Xəta | Həll |
|------|------|
| `Permission denied` `/root/hotel` | Serverdə `test` user icazəsi (`setfacl`) |
| `docker compose` icazə | `usermod -aG docker test` |
| `DEPLOY_PATH not found` | Secret: `/root/hotel` |
| Tag işləmir | Tag `v` ilə başlamalıdır |
| Frontend köhnə API | `FRONTEND_API_URL` secret + yenidən deploy |

---

## Bu layihəyə xas fayllar

| Fayl | Məqsəd |
|------|--------|
| `.github/workflows/deploy-rms.yml` | CI/CD workflow |
| `scripts/build-and-push.sh` | CI-də image build/push |
| `Dockerfile` | Image quruluşu |

Workflow adı (Actions-da): **Build and Deploy RMS**
