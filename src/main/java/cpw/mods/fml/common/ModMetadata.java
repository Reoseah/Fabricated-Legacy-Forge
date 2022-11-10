package cpw.mods.fml.common;

import argo.jdom.JsonNode;
import argo.jdom.JsonNodeBuilders;
import argo.jdom.JsonStringNode;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import cpw.mods.fml.common.functions.ModNameFunction;
import cpw.mods.fml.common.versioning.ArtifactVersion;
import cpw.mods.fml.common.versioning.VersionParser;

import java.util.*;
import java.util.logging.Level;

public class ModMetadata {
    public String modId;
    public String name;
    public String description;
    public String url = "";
    public String updateUrl = "";
    public String logoFile = "";
    public String version = "";
    public List<String> authorList = Lists.newArrayList();
    public String credits = "";
    public String parent = "";
    public String[] screenshots;
    public ModContainer parentMod;
    public List<ModContainer> childMods = Lists.newArrayList();
    public boolean useDependencyInformation;
    public Set<ArtifactVersion> requiredMods;
    public List<ArtifactVersion> dependencies;
    public List<ArtifactVersion> dependants;
    public boolean autogenerated;

    public ModMetadata(JsonNode node) {
        Map<JsonStringNode, Object> processedFields = Maps.transformValues(node.getFields(), new ModMetadata.JsonStringConverter());
        this.modId = (String)processedFields.get(JsonNodeBuilders.aStringBuilder("modid"));
        if (Strings.isNullOrEmpty(this.modId)) {
            FMLLog.log(Level.SEVERE, "Found an invalid mod metadata file - missing modid", new Object[0]);
            throw new LoaderException();
        } else {
            this.name = Strings.nullToEmpty((String)processedFields.get(JsonNodeBuilders.aStringBuilder("name")));
            this.description = Strings.nullToEmpty((String)processedFields.get(JsonNodeBuilders.aStringBuilder("description")));
            this.url = Strings.nullToEmpty((String)processedFields.get(JsonNodeBuilders.aStringBuilder("url")));
            this.updateUrl = Strings.nullToEmpty((String)processedFields.get(JsonNodeBuilders.aStringBuilder("updateUrl")));
            this.logoFile = Strings.nullToEmpty((String)processedFields.get(JsonNodeBuilders.aStringBuilder("logoFile")));
            this.version = Strings.nullToEmpty((String)processedFields.get(JsonNodeBuilders.aStringBuilder("version")));
            this.credits = Strings.nullToEmpty((String)processedFields.get(JsonNodeBuilders.aStringBuilder("credits")));
            this.parent = Strings.nullToEmpty((String)processedFields.get(JsonNodeBuilders.aStringBuilder("parent")));
            this.authorList = (List)Objects.firstNonNull(
                    (List)processedFields.get(JsonNodeBuilders.aStringBuilder("authors")),
                    Objects.firstNonNull((List)processedFields.get(JsonNodeBuilders.aStringBuilder("authorList")), this.authorList)
            );
            this.requiredMods = this.processReferences(processedFields.get(JsonNodeBuilders.aStringBuilder("requiredMods")), HashSet.class);
            this.dependencies = this.processReferences(processedFields.get(JsonNodeBuilders.aStringBuilder("dependencies")), ArrayList.class);
            this.dependants = this.processReferences(processedFields.get(JsonNodeBuilders.aStringBuilder("dependants")), ArrayList.class);
            this.useDependencyInformation = Boolean.parseBoolean(
                    Strings.nullToEmpty((String)processedFields.get(JsonNodeBuilders.aStringBuilder("useDependencyInformation")))
            );
        }
    }

    public ModMetadata() {
    }

    private <T extends Collection<ArtifactVersion>> T processReferences(Object refs, Class<? extends T> retType) {
        T res = null;

        try {
            res = (T)retType.newInstance();
        } catch (Exception var6) {
        }

        if (refs == null) {
            return res;
        }
        for (String ref : ((List<String>)refs))
        {
            res.add(VersionParser.parseVersionReference(ref));
        }
        return res;
    }

    public String getChildModCountString() {
        return String.format("%d child mod%s", this.childMods.size(), this.childMods.size() != 1 ? "s" : "");
    }

    public String getAuthorList() {
        return Joiner.on(", ").join(this.authorList);
    }

    public String getChildModList() {
        return Joiner.on(", ").join(Lists.transform(this.childMods, new ModNameFunction()));
    }

    public String printableSortingRules() {
        return "";
    }

    private static final class JsonArrayConverter implements Function<JsonNode, String> {
        private JsonArrayConverter() {
        }

        public String apply(JsonNode arg0) {
            return arg0.getText();
        }
    }

    private static final class JsonStringConverter implements Function<JsonNode, Object> {
        private JsonStringConverter() {
        }

        public Object apply(JsonNode arg0) {
            return arg0.hasElements() ? Lists.transform(arg0.getElements(), new ModMetadata.JsonArrayConverter()) : arg0.getText();
        }
    }
}
