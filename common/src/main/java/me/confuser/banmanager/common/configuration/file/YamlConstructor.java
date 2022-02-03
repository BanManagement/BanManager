package me.confuser.banmanager.common.configuration.file;

import me.confuser.banmanager.common.configuration.serialization.ConfigurationSerialization;
import me.confuser.banmanager.common.snakeyaml.constructor.SafeConstructor;
import me.confuser.banmanager.common.snakeyaml.error.YAMLException;
import me.confuser.banmanager.common.snakeyaml.nodes.Node;
import me.confuser.banmanager.common.snakeyaml.nodes.Tag;

import java.util.LinkedHashMap;
import java.util.Map;

public class YamlConstructor extends SafeConstructor {

    YamlConstructor() {
        yamlConstructors.put(Tag.MAP, new ConstructCustomObject());
    }

    private class ConstructCustomObject extends ConstructYamlMap {
        @Override public Object construct(final Node node) {
            if (node.isTwoStepsConstruction()) {
                throw new YAMLException("Unexpected referential mapping structure. Node: " + node);
            }

            final Map<?, ?> raw = (Map<?, ?>) super.construct(node);

            if (raw.containsKey(ConfigurationSerialization.SERIALIZED_TYPE_KEY)) {
                final Map<String, Object> typed = new LinkedHashMap<>(raw.size());
                for (final Map.Entry<?, ?> entry : raw.entrySet()) {
                    typed.put(entry.getKey().toString(), entry.getValue());
                }

                try {
                    return ConfigurationSerialization.deserializeObject(typed);
                } catch (final IllegalArgumentException ex) {
                    throw new YAMLException("Could not deserialize object", ex);
                }
            }

            return raw;
        }

        @Override public void construct2ndStep(final Node node, final Object object) {
            throw new YAMLException("Unexpected referential mapping structure. Node: " + node);
        }
    }
}
