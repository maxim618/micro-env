package dev.maxim.microenv;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class MicroEnvConfigLoader {

    private final MicroEnvConfigSourceValidator sourceValidator;
    private final MicroEnvConfigFileReader fileReader;
    private final MicroEnvConfigParser parser;

    public MicroEnvConfigLoader() {
        this(
                new MicroEnvConfigSourceValidator(),
                new MicroEnvConfigFileReader(),
                new MicroEnvConfigParser()
        );
    }

    MicroEnvConfigLoader(
            MicroEnvConfigSourceValidator sourceValidator,
            MicroEnvConfigFileReader fileReader,
            MicroEnvConfigParser parser
    ) {
        this.sourceValidator = sourceValidator;
        this.fileReader = fileReader;
        this.parser = parser;
    }

    public List<MicroEnvConfig> load(List<Path> paths) throws IOException {
        sourceValidator.validate(paths);

        List<MicroEnvConfigFile> files = new ArrayList<>(paths.size());
        for (Path path : paths) {
            files.add(fileReader.read(path));
        }

        List<MicroEnvConfig> configs = new ArrayList<>(files.size());
        for (MicroEnvConfigFile file : files) {
            configs.add(new MicroEnvConfig(
                    file.path(),
                    parser.parse(file.content())
            ));
        }

        return List.copyOf(configs);
    }
}
