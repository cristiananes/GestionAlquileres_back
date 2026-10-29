# Inversión, recurrencias y archivo

## Antes de fusionar o desplegar

1. Hacer copia de seguridad de MySQL y confirmar que puede restaurarse.
2. Configurar `JWT_SECRET` en el entorno del backend con una clave aleatoria propia de al menos 32 bytes. Por ejemplo, generar una fuera del repositorio con `openssl rand -base64 48`. No pegarla en commits, PRs o logs. Al cambiarla se cerrarán las sesiones existentes.
3. El backend ya no crea el administrador predeterminado. Los usuarios existentes permanecen: revisar y cambiar por el procedimiento administrativo las credenciales de cualquier administrador creado anteriormente con datos predeterminados. Para nuevas instalaciones se puede habilitar temporalmente `APP_BOOTSTRAP_ADMIN_ENABLED=true`, junto con `APP_BOOTSTRAP_ADMIN_EMAIL` y `APP_BOOTSTRAP_ADMIN_PASSWORD` (mínimo 16 caracteres); deshabilitarlo después.
4. Revisar las nuevas tablas (`recurring_rule`, `payment_occurrence`, `lease`, `property_document`) y columnas de inversión, archivo, idempotencia y vínculo a vencimientos. El proyecto mantiene `spring.jpa.hibernate.ddl-auto=update`: son adiciones de esquema y no se eliminan registros. En una base con restricciones de DDL, aplicar las adiciones mediante el proceso de migración del entorno antes del despliegue. Las columnas `archived` tienen default false para preservar el historial.
5. Desplegar primero el backend y después el frontend compañero. No volver a una versión antigua del backend después de archivar registros: esa versión no respeta el archivo en los totales.

## Comportamiento

- El dashboard mantiene los movimientos reales y sus barras, balance y proporción. La nueva rentabilidad está en la ficha de propiedad.
- Inversión total = compra + gastos de compra + reforma inicial + mobiliario. Si falta compra no se inventa una rentabilidad de 0 %. Los costes adicionales vacíos se consideran cero y se indica en pantalla.
- La inversión tiene un endpoint de edición independiente; editar el nombre/dirección con un cliente anterior no borra los datos financieros.
- La rentabilidad real del mes/año/intervalo utiliza únicamente movimientos confirmados y no archivados. Las fianzas se muestran aparte. `INVESTMENT` y `FINANCING` no se restan como gastos operativos; todos los gastos se restan de caja. El año actual es acumulado, no anualizado. No es una declaración fiscal ni incluye plusvalías.
- La previsión recorre las reglas activas en los próximos doce meses, respeta su inicio/fin y calcula la media mensual dividiendo entre doce. No incluye gastos puntuales futuros, vacantes, impagos ni inflación. Las cuotas de financiación afectan a la caja prevista.
- Las reglas son semanales/mensuales/trimestrales/anuales y están ancladas a la fecha original: 31/01 -> 28/02 -> 31/03. Los contratos con día 31 conservan ese día incluso si empiezan en febrero. No se calcula prorrateo del primer mes.
- No hay proceso de cron ni escrituras al consultar previsiones. Los vencimientos se calculan al consultar el intervalo; cada cobro/pago confirmado crea un movimiento real. Se permiten pagos parciales, pero no superiores a lo pendiente. Las transacciones bloquean la regla y la base impone una única ocurrencia por regla/fecha. Una clave de operación repetida no vuelve a cobrar.
- Para corregir un pago recurrente, archivarlo y confirmar el correcto desde el vencimiento. Restaurar un pago que sobrepagaría el vencimiento se rechaza. Los movimientos manuales antiguos siguen siendo puntuales; no se convierten automáticamente ni se vuelven a generar.
- Finalizar una regla impide futuros vencimientos y conserva los anteriores. Para cambiar importe/frecuencia, finalizar la anterior y crear una nueva con la fecha efectiva. Archivar la regla oculta sus previsiones y conserva los movimientos; restaurarla recupera su planificación según sus fechas originales.
- Archivar un contrato archiva su regla vinculada y su evento. Restaurarlo los recupera; revisar previamente que no se haya creado un contrato sustituto para el mismo periodo. Los contratos no se firman ni se envían a terceros desde la app.
- El PDF exporta los filtros de fechas y propiedad del listado, incluidos intervalos abiertos. No exporta archivados.
- Los justificantes se guardan en la base de datos, con control de acceso por propiedad. PDF/PNG/JPEG, máximo 5 MB, descarga autenticada como adjunto y `no-store`. No se publican bajo `/uploads`. Mantener copias de seguridad y límites de capacidad de la base.
- El archivado es recuperable para propiedades, ingresos, gastos, tareas, eventos, reglas, contratos y documentos. Los archivos de imagen anteriores conservan su comportamiento; esta versión no aplica un archivo recuperable a la eliminación de imágenes individuales.
- Los antiguos eventos duplicados no se borran automáticamente. Se bloquea el doble envío y se advierte de coincidencias en el calendario. Revisar y archivar manualmente el duplicado solo cuando se confirme que lo es.

## Verificación tras despliegue

- Login y aislamiento entre dos cuentas.
- Crear propiedad de prueba y configurar inversión; comprobar resultados sin fianzas.
- Crear regla mensual y comprobar que no modifica los totales reales hasta confirmar un cobro; confirmar parcial y resto.
- Recargar/reintentar una confirmación: debe existir un solo movimiento por operación.
- Archivar/restaurar un movimiento y comprobar balance, PDF y pendiente.
- Crear contrato con fin y regla opcional: un solo evento; no duplicar una regla de alquiler que ya existiera.
- Adjuntar y descargar un justificante; otra cuenta no puede acceder.
- Probar en móvil calendario, filtros, ficha y modales. Las pruebas automáticas no sustituyen esta comprobación de interfaz.
