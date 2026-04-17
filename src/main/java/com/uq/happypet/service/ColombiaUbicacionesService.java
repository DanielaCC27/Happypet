package com.uq.happypet.service;

import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Colombian departments and municipalities (JSON: static/data/colombia.min.json).
 * Uses Jackson 3 (tools.jackson) as required by Spring Boot 4.
 */
@Service
public class ColombiaUbicacionesService {

    private final JsonMapper jsonMapper = JsonMapper.builder().build();
    private final Map<String, Set<String>> deptoMunicipios = new LinkedHashMap<>();
    private List<String> departamentosOrdenados = List.of();
    /** Misma fuente que parseamos en servidor, para incrustar en la vista sin fetch al cliente. */
    private String jsonUtf8 = "[]";

    @PostConstruct
    public void cargar() throws IOException {
        ClassPathResource res = new ClassPathResource("static/data/colombia.min.json");
        byte[] raw = res.getContentAsByteArray();
        jsonUtf8 = new String(raw, StandardCharsets.UTF_8);
        JsonNode root = jsonMapper.readTree(raw);
        if (!root.isArray()) {
            throw new IllegalStateException("colombia.min.json must be a JSON array");
        }
        Collator es = Collator.getInstance(new Locale("es", "CO"));
        es.setStrength(Collator.PRIMARY);
        for (JsonNode dep : root) {
            String nombreDep = dep.get("departamento").asText();
            TreeSet<String> munis = new TreeSet<>(es);
            for (JsonNode c : dep.get("ciudades")) {
                munis.add(c.asText());
            }
            deptoMunicipios.put(nombreDep, Collections.unmodifiableSet(munis));
        }
        ArrayList<String> deps = new ArrayList<>(deptoMunicipios.keySet());
        deps.sort(es);
        departamentosOrdenados = Collections.unmodifiableList(deps);
    }

    /** JSON completo para un {@code script type="application/json"} en checkout-facturacion. */
    public String getJsonIncrustado() {
        return jsonUtf8;
    }

    public List<String> listarDepartamentos() {
        return departamentosOrdenados;
    }

    public boolean esParValido(String departamento, String municipio) {
        if (departamento == null || municipio == null) {
            return false;
        }
        String d = departamento.trim();
        String m = municipio.trim();
        Set<String> munis = deptoMunicipios.get(d);
        return munis != null && munis.contains(m);
    }

    public String construirDireccionCompleta(String departamento, String municipio, String detalle) {
        String dt = departamento != null ? departamento.trim() : "";
        String mn = municipio != null ? municipio.trim() : "";
        String t = detalle != null ? detalle.trim() : "";
        return dt + ", " + mn + ". " + t;
    }
}
