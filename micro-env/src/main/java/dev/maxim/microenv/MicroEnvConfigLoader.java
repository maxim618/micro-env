package dev.maxim.microenv;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MicroEnvConfigLoader {

    private final MicroEnvConfigSourceValidator sourceValidator;
    private final MicroEnvConfigFileReader fileReader;
    private final MicroEnvConfigMerger merger;

    public MicroEnvConfigLoader() {
        this(
                new MicroEnvConfigSourceValidator(),
                new MicroEnvConfigFileReader(),
                new MicroEnvConfigMerger()
        );
    }

    MicroEnvConfigLoader(
            MicroEnvConfigSourceValidator sourceValidator,
            MicroEnvConfigFileReader fileReader,
            MicroEnvConfigMerger merger
    ) {
        this.sourceValidator = sourceValidator;
        this.fileReader = fileReader;
        this.merger = merger;
    }

    public Map<String, String> load(List<Path> paths) throws IOException {
        sourceValidator.validate(paths);

        List<MicroEnvConfigFile> files = new ArrayList<>(paths.size());
        for (Path path : paths) {
            files.add(fileReader.read(path));
        }

        return merger.merge(files);
    }
}
