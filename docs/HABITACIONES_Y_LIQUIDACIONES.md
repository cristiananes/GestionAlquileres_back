# Habitaciones, ocupación y liquidaciones

## Alcance
- Catálogo de habitaciones por inmueble, con número estable, nombre y archivo recuperable.
- Contratos de piso completo, por habitación o antiguos sin asignar. Estancia inclusiva dentro del contrato. Control de solapamientos bajo bloqueo del inmueble, también al restaurar contratos. La información anterior no se clasifica automáticamente.
- Importación al recibo desde la habitación y estancia real del contrato; el reparto confirmado mantiene su instantánea.
- Un destinatario propietario por inmueble en esta versión. Condiciones de gestión: porcentaje sobre rentas realmente cobradas o cuota fija por liquidación, impuesto configurable y quién cobra la renta. No hay valores fiscales asumidos ni generación de facturas fiscales.
- Borrador revisable por periodo con selección explícita de ingresos y gastos realmente registrados. Fianzas excluidas. Solo deducir gastos adelantados por el gestor. No usar este resumen como conciliación bancaria automática.
- Si cobra el gestor: saldo = ingresos seleccionados - gastos adelantados seleccionados - honorarios e impuesto. Si cobra el propietario: saldo = -gastos adelantados - honorarios e impuesto; no se vuelve a pagar al propietario una renta que ya cobró.
- Honorario porcentual calculado solo sobre ingresos RENT seleccionados; nunca sobre fianzas o reembolsos. Cuota fija por liquidación, sin prorrateo automático.
- Confirmar congela el desglose y bloquea movimientos usados y periodos solapados; un token de revisión detecta cambios entre vista previa y confirmación. PDF privado como resumen interno, no factura fiscal.
- Marcar saldada es un registro manual, no una orden bancaria. El usuario confirma que los honorarios se cobraron; se registra una única comisión como gasto de la propiedad. La transferencia al propietario no es un gasto de rentabilidad ni se crea como tal.
- Anular conserva el historial, libera movimientos y archiva la comisión generada, sin mover dinero ni emitir abonos fiscales. Refleja una corrección del registro, no una devolución bancaria.

## Despliegue
Backend antes que frontend. No requiere nuevas variables. Datos aditivos. La corrección de TiDB emplea SELECT FOR UPDATE sin alias para mantener los bloqueos en la base real. Columna read existente citada para evitar palabra reservada.

## Pendientes fuera de esta fase
Copropietarios con cuotas, facturación fiscal de honorarios, pagos bancarios, conciliación, permisos por agencia y compraventa. Antes de comercializar: verificar el flujo con clientes piloto y el tratamiento de datos.
