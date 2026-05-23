# DealAlert

Aplicación web orientada al seguimiento de precios de juegos en Steam. Permite a los usuarios agregar juegos a una lista de alertas y recibir una notificación mediante telegram cuando se detecta una oferta para alguno de ellos.

**App web:** https://xaviermora.github.io/DealAlert/

### Descripción
La aplicación realiza un monitoreo periódico de los juegos registrados, actualizando sus precios con cierta frecuencia. No se trata de un sistema en tiempo real, por lo que no está en completa sincronización con la plataforma Steam.

Para la obtención de información sobre los juegos se integran APIs de Steam.

### Autenticación

La autenticación se implementó mediante códigos de inicio de sesión enviados por email y sesiones basadas en tokens.
Inicialmente se incorporó protección contra CSRF, pero debido a la separación de dominios entre frontend y backend, no se incluyó en la versión final.

Como mejora futura, se podrían incorporar mecanismos adicionales de seguridad o migrar a otro esquema de autenticación.
