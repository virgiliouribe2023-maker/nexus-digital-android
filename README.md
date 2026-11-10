# NEXUS DIGITAL Admin (Android)

App nativa de Android que abre el panel de administración de NEXUS DIGITAL y recibe
notificaciones push **nativas** (Firebase Cloud Messaging) cuando llega un chat nuevo,
un formulario de contacto o un mensaje — mucho más confiables que las notificaciones web
anteriores, porque Android las entrega aunque la app esté cerrada.

El APK se compila solo, en la nube, cada vez que subes un cambio a GitHub (con GitHub
Actions), igual que en tu otro proyecto. No necesitas instalar Android Studio.

## Paso 1 — Crear el proyecto en Firebase (una sola vez)

1. Entra a https://console.firebase.google.com y crea un proyecto nuevo (el nombre no importa, por ejemplo "NexusDigital").
2. Dentro del proyecto, click en el ícono de Android (**Agregar app → Android**).
3. En "nombre del paquete Android" escribe exactamente: `com.nexusdigital.admin`
4. Descarga el archivo **`google-services.json`** que te da Firebase (NO lo subas a GitHub directamente, en el paso 3 te digo cómo).
5. En el menú del proyecto de Firebase ve a **Configuración del proyecto → Cuentas de servicio** y da clic en **"Generar nueva clave privada"**. Esto descarga otro archivo JSON distinto (la *cuenta de servicio*) — este es el que necesita tu servidor PHP para poder mandar las notificaciones.

## Paso 2 — Subir este proyecto a GitHub

1. Crea un repositorio nuevo en GitHub (por ejemplo `nexus-digital-android`), privado.
2. Con GitHub Desktop, igual que hiciste con tu otro proyecto: agrega esta carpeta como repositorio local, haz commit y "Publish repository" hacia el repositorio que creaste.

## Paso 3 — Dar a GitHub Actions el `google-services.json` (como secreto, no como archivo visible)

1. En GitHub, entra al repositorio → **Settings → Secrets and variables → Actions → New repository secret**.
2. Nombre del secreto: `GOOGLE_SERVICES_JSON`
3. Valor: abre el archivo `google-services.json` que descargaste en el paso 1 con el Bloc de notas, copia **todo** su contenido y pégalo aquí.
4. Guarda.

Con esto, cada vez que GitHub Actions compile la app, va a escribir ese archivo automáticamente antes de compilar — así nunca queda visible en el código del repositorio.

## Paso 4 — Compilar y descargar el APK

1. En GitHub, pestaña **Actions** del repositorio → deberías ver que ya corrió (o dale clic a "Run workflow" si no corrió solo).
2. Cuando termine (ícono verde ✅), entra a la pestaña **Releases** del repositorio (a la derecha, en la página principal) — ahí vas a encontrar el archivo `app-debug.apk` listo para descargar directo desde el celular.
3. En el celular Android: abre ese enlace, descarga el `.apk`, y si Android pregunta, permite "instalar apps de fuentes desconocidas" solo para esta descarga. Instala.

Cada vez que quieras una versión nueva (por ejemplo si cambio algo del código), solo subes los archivos nuevos por GitHub Desktop otra vez y repites el paso 4.

## Paso 5 — Subir la cuenta de servicio al servidor (para que el servidor pueda enviar las notificaciones)

Este archivo es distinto al `google-services.json` — es el que descargaste en el paso 1.5 (Cuentas de servicio).

1. Renómbralo a exactamente: `fcm_service_account.json`
2. Por cPanel / Administrador de archivos, súbelo a la carpeta: `online/includes/fcm_service_account.json`
3. Listo — no hace falta tocar nada más del servidor. Ya subí ahí mismo un `.htaccess` que bloquea que ese archivo se pueda abrir desde el navegador, para que nadie más pueda descargarlo.

**Importante:** no mandes este archivo por correo, WhatsApp ni lo subas a GitHub — tiene una llave privada de tu proyecto de Firebase. Súbelo solo por cPanel directamente al servidor.

## ¿Qué cambia respecto a las notificaciones web anteriores?

- El servidor ahora manda las notificaciones por **dos** canales a la vez: el push web de siempre (para quien abra el panel desde una computadora/Chrome) y el nuevo FCM nativo (para esta app). No se quitó nada, solo se agregó.
- Dentro de la app, al abrir cualquier pantalla del admin, se registra automáticamente el "token" de ese celular para que el servidor sepa a quién avisar.
- Si cierras sesión y la vuelves a abrir con otro usuario de staff en el mismo celular, el token se vuelve a asociar a ese usuario automáticamente.

## Archivos que ya recibiste del lado del servidor (PHP)

Ya estos ya se entregaron junto con este proyecto de Android, con su ruta exacta en el servidor:

- `online/includes/functions.php` (reemplazar — agrega las funciones de envío FCM)
- `online/includes/.htaccess` (nuevo — bloquea el acceso directo al archivo de la cuenta de servicio)
- `online/admin/fcm_registrar_token.php` (nuevo)
- `online/admin/includes/admin_header.php` (reemplazar — agrega el puente JS que registra el token)
- `online/sql/alter_fase32.sql` (nuevo — correr una sola vez en phpMyAdmin)
