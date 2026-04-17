package br.com.matheushramos.tucc_monolito.converter;

import com.opensymphony.xwork2.conversion.impl.DefaultTypeConverter;

import java.math.BigDecimal;
import java.util.Map;

public class BigDecimalConverter extends DefaultTypeConverter {

    @Override
    @SuppressWarnings("rawtypes")
    public Object convertValue(Map<String, Object> context, Object value, Class toType) {
        if (toType == BigDecimal.class) {
            if (value == null) return null;
            String raw = value instanceof String[] ? ((String[]) value)[0] : value.toString();
            raw = raw.trim();
            if (raw.isEmpty()) return null;
            // Aceita vírgula como separador decimal (pt_BR: "5.200,97" → "5200.97")
            // e ponto como separador decimal (en_US: "5200.97") — normaliza para ponto
            if (raw.contains(",")) {
                raw = raw.replace(".", "").replace(",", ".");
            }
            return new BigDecimal(raw);
        }
        if (toType == String.class && value instanceof BigDecimal) {
            return ((BigDecimal) value).toPlainString();
        }
        return super.convertValue(context, value, toType);
    }
}
