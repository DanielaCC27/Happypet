package com.uq.happypet.config;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Aligns legacy {@code pedidos} tables with the current JPA model.
 * Replaces SQL init scripts with DO blocks that break the PostgreSQL JDBC driver.
 * Also widens {@code productos} columns: Hibernate ddl-auto=update does not expand varchar(255) in PostgreSQL.
 */
@Component
@Order(0)
public class PedidosSchemaMigrationRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(PedidosSchemaMigrationRunner.class);

    private final JdbcTemplate jdbcTemplate;

    public PedidosSchemaMigrationRunner(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        migrateProductosColumnsIfPostgres();

        if (!pedidosTableExists()) {
            return;
        }
        try {
            jdbcTemplate.execute("ALTER TABLE public.pedidos ADD COLUMN IF NOT EXISTS direccion_envio VARCHAR(500)");
            jdbcTemplate.execute("ALTER TABLE public.pedidos ADD COLUMN IF NOT EXISTS metodo_pago VARCHAR(24)");
            jdbcTemplate.execute("ALTER TABLE public.pedidos ADD COLUMN IF NOT EXISTS fecha_entrega_preferida DATE");
            jdbcTemplate.execute("ALTER TABLE public.pedidos ADD COLUMN IF NOT EXISTS horario_entrega VARCHAR(24)");
            jdbcTemplate.execute("ALTER TABLE public.pedidos ADD COLUMN IF NOT EXISTS ventana_entrega_desde DATE");
            jdbcTemplate.execute("ALTER TABLE public.pedidos ADD COLUMN IF NOT EXISTS ventana_entrega_hasta DATE");
            jdbcTemplate.execute("ALTER TABLE public.pedidos ADD COLUMN IF NOT EXISTS facturacion_nombre VARCHAR(200)");
            jdbcTemplate.execute("ALTER TABLE public.pedidos ADD COLUMN IF NOT EXISTS facturacion_apellidos VARCHAR(200)");
            jdbcTemplate.execute("ALTER TABLE public.pedidos ADD COLUMN IF NOT EXISTS facturacion_tipo_documento VARCHAR(32)");
            jdbcTemplate.execute("ALTER TABLE public.pedidos ADD COLUMN IF NOT EXISTS facturacion_documento VARCHAR(32)");
            jdbcTemplate.execute("ALTER TABLE public.pedidos ADD COLUMN IF NOT EXISTS facturacion_direccion VARCHAR(500)");
            jdbcTemplate.execute("ALTER TABLE public.pedidos ADD COLUMN IF NOT EXISTS facturacion_email VARCHAR(255)");

            jdbcTemplate.execute("ALTER TABLE public.pedidos ALTER COLUMN direccion_envio TYPE VARCHAR(1000)");
            jdbcTemplate.execute("ALTER TABLE public.pedidos ALTER COLUMN facturacion_direccion TYPE VARCHAR(1000)");

            jdbcTemplate.execute(
                    "UPDATE public.pedidos SET "
                            + "direccion_envio = COALESCE(NULLIF(direccion_envio, ''), 'Por definir'), "
                            + "metodo_pago = COALESCE(NULLIF(metodo_pago, ''), 'EFECTIVO'), "
                            + "fecha_entrega_preferida = COALESCE(fecha_entrega_preferida, CURRENT_DATE + 4), "
                            + "horario_entrega = COALESCE(NULLIF(horario_entrega, ''), 'JORNADA_8_18'), "
                            + "ventana_entrega_desde = COALESCE(ventana_entrega_desde, CURRENT_DATE + 4), "
                            + "ventana_entrega_hasta = COALESCE(ventana_entrega_hasta, CURRENT_DATE + 6), "
                            + "facturacion_nombre = COALESCE(NULLIF(facturacion_nombre, ''), 'Sin nombre'), "
                            + "facturacion_documento = COALESCE(NULLIF(facturacion_documento, ''), 'NA'), "
                            + "facturacion_direccion = COALESCE(NULLIF(facturacion_direccion, ''), "
                            + "COALESCE(NULLIF(direccion_envio, ''), 'Por definir')), "
                            + "facturacion_email = COALESCE(NULLIF(facturacion_email, ''), 'sin-correo@happypet.local')");

            jdbcTemplate.execute("ALTER TABLE public.pedidos ALTER COLUMN direccion_envio SET NOT NULL");
            jdbcTemplate.execute("ALTER TABLE public.pedidos ALTER COLUMN metodo_pago SET NOT NULL");
            jdbcTemplate.execute("ALTER TABLE public.pedidos ALTER COLUMN fecha_entrega_preferida SET NOT NULL");
            jdbcTemplate.execute("ALTER TABLE public.pedidos ALTER COLUMN horario_entrega SET NOT NULL");
            jdbcTemplate.execute("ALTER TABLE public.pedidos ALTER COLUMN ventana_entrega_desde SET NOT NULL");
            jdbcTemplate.execute("ALTER TABLE public.pedidos ALTER COLUMN ventana_entrega_hasta SET NOT NULL");
            jdbcTemplate.execute("ALTER TABLE public.pedidos ALTER COLUMN facturacion_nombre SET NOT NULL");
            jdbcTemplate.execute("ALTER TABLE public.pedidos ALTER COLUMN facturacion_documento SET NOT NULL");
            jdbcTemplate.execute("ALTER TABLE public.pedidos ALTER COLUMN facturacion_direccion SET NOT NULL");
            jdbcTemplate.execute("ALTER TABLE public.pedidos ALTER COLUMN facturacion_email SET NOT NULL");
        } catch (Exception e) {
            log.warn("pedidos schema migration skipped or partial: {}", e.getMessage());
        }
    }

    private boolean pedidosTableExists() {
        Integer n = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables "
                        + "WHERE table_schema = 'public' AND table_name = 'pedidos'",
                Integer.class);
        return n != null && n > 0;
    }

    private void migrateProductosColumnsIfPostgres() {
        if (!isPostgres()) {
            return;
        }
        Integer n = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables "
                        + "WHERE table_schema = 'public' AND table_name = 'productos'",
                Integer.class);
        if (n == null || n == 0) {
            return;
        }
        try {
            jdbcTemplate.execute("ALTER TABLE public.productos ALTER COLUMN nombre TYPE VARCHAR(500)");
            jdbcTemplate.execute("ALTER TABLE public.productos ALTER COLUMN descripcion TYPE TEXT");
            jdbcTemplate.execute("ALTER TABLE public.productos ALTER COLUMN imagen_url TYPE TEXT");
            jdbcTemplate.execute("ALTER TABLE public.productos ALTER COLUMN categoria TYPE VARCHAR(100)");
            log.info("productos: columnas alineadas (nombre, descripcion, imagen_url, categoria)");
        } catch (Exception e) {
            log.warn("productos schema migration skipped or partial: {}", e.getMessage());
        }
    }

    private boolean isPostgres() {
        DataSource ds = jdbcTemplate.getDataSource();
        if (ds == null) {
            return false;
        }
        try (Connection c = ds.getConnection()) {
            String url = c.getMetaData().getURL();
            return url != null && url.startsWith("jdbc:postgresql:");
        } catch (SQLException e) {
            return false;
        }
    }
}