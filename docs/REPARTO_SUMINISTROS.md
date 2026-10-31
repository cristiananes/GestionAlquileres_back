# Recibos y reparto de suministros

## Plan y alcance
1. Recibos privados por propiedad: PDF/foto opcional, proveedor, referencia, suministro, importe, periodo y vencimiento.
2. Reparto revisable antes de confirmar: igualitario, porcentajes que suman 100 o días por habitación. Intervalos inclusivos; no se permiten dos ocupantes simultáneos de la misma habitación. Una estancia compartida puede representarse como un destinatario conjunto. Vacantes a cargo del propietario o redistribuidas entre habitaciones ocupadas. Si no hay ocupantes, paga el propietario.
3. Importes en céntimos: método de restos mayores, desempate por orden de destinatarios; suma exacta de todas las partes incluyendo propietario.
4. Confirmación inmutable y pagos parciales. Registrar un gasto pagado o vincular uno existente, y registrar/vincular cada reembolso cobrado. Solo movimientos reales afectan al dashboard. Reembolsos etiquetados separadamente del alquiler, incluidos en caja y resultado operativo junto al gasto para obtener el coste neto.
5. Mensaje individual editable/copiable para WhatsApp/correo; no se envían mensajes automáticamente ni se publica el archivo. Compartir manualmente el justificante descargado evita enlaces públicos a datos privados.

## Decisiones
- Primera versión manual: no OCR, buzón de recepción automática ni envío de emails. No requiere nuevas variables o proveedores.
- Los contratos existentes permiten precargar datos; la factura guarda una instantánea. Revisar habitaciones y fechas al importar.
- Repartir no presupone derecho contractual a repercutir el coste: el propietario revisa lo pactado antes de confirmar.
- No se crean alquileres recurrentes ni cobros al subir archivos.
- Los recibos no se editan tras confirmarse. Para corregirlos, archivar primero sus movimientos vinculados y luego el recibo, y crear el correcto. Se pueden restaurar movimientos si no superan el pendiente.
- Desplegar backend antes que frontend. La función requiere la capacidad utility-bills. Conservar JWT_SECRET ya configurada; ninguna configuración nueva es necesaria. Nuevas tablas y columnas aditivas mediante la configuración JPA existente.

## Verificación
Reparto con céntimos, porcentajes, cambios de ocupante, huecos, vacantes, aislamiento entre usuarios, pagos parciales, reintentos, bloqueo frente a sobrepago y doble enlace, coherencia al archivar/restaurar ingresos, archivos privados y compilación de ambos proyectos.
