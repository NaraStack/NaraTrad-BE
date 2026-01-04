# Setup Supabase untuk NaraTrad Backend

## Daftar Isi
1. [Persiapan Akun Supabase](#1-persiapan-akun-supabase)
2. [Konfigurasi Project](#2-konfigurasi-project)
3. [Cara Menggunakan](#3-cara-menggunakan)
4. [Troubleshooting](#4-troubleshooting)

---

## 1. Persiapan Akun Supabase

### 1.1 Buat Akun Supabase
1. Buka [https://supabase.com](https://supabase.com)
2. Klik **"Start your project"** atau **"Sign Up"**
3. Login menggunakan GitHub account (recommended) atau email

### 1.2 Buat Project Baru
1. Setelah login, klik **"New Project"**
2. Isi informasi project:
   - **Name**: `naratrad` (atau nama sesuai keinginan)
   - **Database Password**: Buat password yang kuat (SIMPAN password ini!)
   - **Region**: Pilih region terdekat (contoh: `Southeast Asia (Singapore)`)
   - **Pricing Plan**: Pilih **Free** untuk development
3. Klik **"Create new project"**
4. Tunggu beberapa menit sampai project selesai di-setup

### 1.3 Dapatkan Database Credentials
1. Setelah project selesai, buka **Settings** (icon gear di sidebar)
2. Pilih **Database** dari menu
3. Scroll ke bagian **Connection String**
4. Salin informasi berikut:
   - **Host**: `db.<project-ref>.supabase.co`
   - **Database name**: `postgres`
   - **Port**: `5432`
   - **User**: `postgres`
   - **Password**: Password yang Anda buat saat setup project

Atau bisa langsung copy **URI** format:
```
postgresql://postgres:[YOUR-PASSWORD]@db.<project-ref>.supabase.co:5432/postgres
```

---

## 2. Konfigurasi Project

### 2.1 Setup Environment Variables

1. Copy file `.env.example` menjadi `.env`:
   ```bash
   cd naratrad
   cp .env.example .env
   ```

2. Edit file `.env` dan isi dengan credentials Supabase Anda:
   ```properties
   # Supabase Database Configuration
   SUPABASE_DB_URL=jdbc:postgresql://db.<your-project-ref>.supabase.co:5432/postgres
   SUPABASE_DB_USERNAME=postgres
   SUPABASE_DB_PASSWORD=<password-yang-anda-buat>

   # Finnhub API (sudah ada sebelumnya)
   FINNHUB_API_KEY=d518qp9r01qjia5c34agd518qp9r01qjia5c34b0

   # Application Port (optional)
   SERVER_PORT=8080
   ```

   **Contoh lengkap:**
   ```properties
   SUPABASE_DB_URL=jdbc:postgresql://db.abcdefghijklmn.supabase.co:5432/postgres
   SUPABASE_DB_USERNAME=postgres
   SUPABASE_DB_PASSWORD=MyS3cur3P@ssw0rd!
   FINNHUB_API_KEY=d518qp9r01qjia5c34agd518qp9r01qjia5c34b0
   SERVER_PORT=8080
   ```

### 2.2 Verifikasi Dependencies

File `pom.xml` sudah diupdate dengan PostgreSQL driver:

```xml
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
```

Jalankan Maven untuk download dependencies:
```bash
./mvnw clean install
```

---

## 3. Cara Menggunakan

### Mode Development (H2 Database)
Untuk development lokal dengan H2 in-memory database:

```bash
./mvnw spring-boot:run
```

Atau set profile secara explicit:
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

Akses H2 Console: http://localhost:8080/h2-console

### Mode Production (Supabase PostgreSQL)

1. Pastikan file `.env` sudah terisi dengan benar
2. Export environment variables (Linux/Mac):
   ```bash
   export $(cat .env | xargs)
   ```

   Untuk Windows PowerShell:
   ```powershell
   Get-Content .env | ForEach-Object {
       $name, $value = $_.split('=')
       Set-Item -Path env:$name -Value $value
   }
   ```

3. Jalankan dengan profile production:
   ```bash
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
   ```

   Atau set environment variable:
   ```bash
   export SPRING_PROFILE=prod
   ./mvnw spring-boot:run
   ```

### Verifikasi Koneksi

Setelah aplikasi berjalan, cek log untuk memastikan koneksi berhasil:

```
HikariPool-1 - Starting...
HikariPool-1 - Start completed.
```

Cek juga Swagger UI untuk test API: http://localhost:8080/swagger-ui.html

---

## 4. Troubleshooting

### Error: "Connection refused"
**Penyebab**: Supabase credentials salah atau network issue

**Solusi**:
1. Cek kembali `SUPABASE_DB_URL`, username, dan password
2. Pastikan format URL benar: `jdbc:postgresql://db.<project-ref>.supabase.co:5432/postgres`
3. Cek internet connection dan firewall

### Error: "password authentication failed"
**Penyebab**: Password salah

**Solusi**:
1. Reset database password di Supabase Dashboard → Settings → Database
2. Update file `.env` dengan password baru
3. Restart aplikasi

### Error: "SSL connection required"
**Penyebab**: Supabase memerlukan SSL connection

**Solusi**: Update URL dengan SSL parameter di `application-prod.properties`:
```properties
spring.datasource.url=${SUPABASE_DB_URL}?sslmode=require
```

### Table tidak terbuat otomatis
**Penyebab**: Hibernate ddl-auto mungkin perlu disesuaikan

**Solusi**: Cek `application.properties`, pastikan:
```properties
spring.jpa.hibernate.ddl-auto=update
```

Atau gunakan `create` untuk pertama kali (akan drop existing tables):
```properties
spring.jpa.hibernate.ddl-auto=create
```

### Melihat data di Supabase

1. Buka Supabase Dashboard
2. Pilih **Table Editor** di sidebar
3. Anda akan melihat semua tables yang dibuat oleh JPA
4. Bisa langsung query/edit data dari sini

---

## 5. Best Practices

### Development Workflow
- Gunakan **H2 (dev profile)** untuk development lokal
- Gunakan **Supabase (prod profile)** untuk testing production atau deployment

### Security
- **JANGAN** commit file `.env` ke Git
- **JANGAN** hardcode password di `application.properties`
- Gunakan environment variables untuk semua credentials
- Rotate database password secara berkala

### Database Management
- Gunakan migration tools seperti **Flyway** atau **Liquibase** untuk production
- Backup database secara rutin di Supabase Dashboard
- Monitor usage di Supabase Dashboard untuk menghindari free tier limit

---

## 6. Resources

- [Supabase Documentation](https://supabase.com/docs)
- [Spring Boot + PostgreSQL Guide](https://spring.io/guides/gs/accessing-data-jpa/)
- [Supabase Database Connection](https://supabase.com/docs/guides/database/connecting-to-postgres)

---

## Support

Jika ada masalah, cek:
1. Supabase project status di dashboard
2. Application logs untuk error details
3. Supabase logs di Dashboard → Logs

Happy coding! 🚀
