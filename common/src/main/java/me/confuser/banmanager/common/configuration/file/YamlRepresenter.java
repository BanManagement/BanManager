package me.confuser.banmanager.common.configuration.file;

import me.confuser.banmanager.common.configuration.ConfigurationSection;
import me.confuser.banmanager.common.configuration.serialization.ConfigurationSerializable;
import me.confuser.banmanager.common.configuration.serialization.ConfigurationSerialization;
import me.confuser.banmanager.common.snakeyaml.nodes.Node;
import me.confuser.banmanager.common.snakeyaml.representer.Representer;

import java.util.LinkedHashMap;
import java.util.Map;

class YamlRepresenter extends Representer {

    YamlRepresenter() {
        this.multiRepresenters.put(ConfigurationSection.class, new RepresentConfigurationSection());
        this.multiRepresenters
            .put(ConfigurationSerializable.class, new RepresentConfigurationSerializable());
    }

    private class RepresentConfigurationSection extends RepresentMap {

        @Override public Node representData(Object data) {
            return super.representData(((ConfigurationSection) data).getValues(false));
        }
    }


    private class RepresentConfigurationSerializable extends RepresentMap {

        @Override public Node representData(Object data) {
            ConfigurationSerializable serializable = (ConfigurationSerializable) data;
            Map<String, Object> values = new LinkedHashMap<>();
            values.put(ConfigurationSerialization.SERIALIZED_TYPE_KEY,
                ConfigurationSerialization.getAlias(serializable.getClass()));
            values.putAll(serializable.serialize());

            return super.representData(values);
        }
    }
}
