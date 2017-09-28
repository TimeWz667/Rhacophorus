package mcore;

import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * Created by timewz on 28/09/17.
 */
public class ModelSelector extends HashMap<String, AbsSimModel> {


    public ModelSelector(Map<String, AbsSimModel> vs) {
        super(vs);
    }

    public ModelSelector select_all(String sel) {
        List<Predicate<AbsSimModel>> filters = parseSelector(sel);
        Stream<AbsSimModel> vs = values().stream();
        for (Predicate<AbsSimModel> f: filters) {
            vs = vs.filter(f);
        }
        return new ModelSelector(vs.collect(Collectors.toMap(AbsSimModel::getName, e->e)));
    }

    public double sum(String key) {
        return this.values().stream().mapToDouble(e-> e.get(key)).sum();
    }

    private List<Predicate<AbsSimModel>> parseSelector(String sel) {
        Map<String, String> sels = new HashMap<>();
        List<Predicate<AbsSimModel>> filters = new ArrayList<>();

        Pattern pat = Pattern.compile("PC\\s*=\\s*(\\w+)");
        Matcher mat = pat.matcher(sel);

        if (mat.find()) {
            String arg = mat.group(1);
            filters.add(e -> e.getMeta().getPC().equals(arg));
        }

        pat = Pattern.compile("DC\\s*=\\s*(\\w+)");
        mat = pat.matcher(sel);
        if (mat.find()) {
            String arg = mat.group(1);
            filters.add(e -> e.getMeta().getDC().equals(arg));
        }

        pat = Pattern.compile("MC\\s*=\\s*(\\w+)");
        mat = pat.matcher(sel);
        if (mat.find()) {
            String arg = mat.group(1);
            filters.add(e -> e.getMeta().getPrototype().equals(arg));
        }

        pat = Pattern.compile("\\.(\\w+)");
        mat = pat.matcher(sel);
        if (mat.find()) {
            String arg = mat.group(1);
            filters.add(e -> e.getMeta().getPrototype().equals(arg));
        }

        pat = Pattern.compile("#(\\w+)");
        mat = pat.matcher(sel);
        if (mat.find()) {
            String arg = mat.group(1);
            filters.add(e -> e.getName().startsWith(arg));
        }

        pat = Pattern.compile("#(\\w+)");
        mat = pat.matcher(sel);
        if (mat.find()) {
            String arg = mat.group(1);
            filters.add(e -> e.getName().equals(arg));
        }

        return filters;
    }

}
