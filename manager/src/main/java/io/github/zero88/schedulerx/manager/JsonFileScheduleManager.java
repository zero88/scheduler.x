package io.github.zero88.schedulerx.manager;

import io.github.zero88.schedulerx.SchedulingMonitor;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.FileSystem;
import io.vertx.core.json.JsonObject;

/**
 * A concrete implementation of Manager that loads scheduler configurations from JSON files.
 * The file path should be specified in the setup configuration under the "filePath" key.
 */
public class JsonFileScheduleManager implements SchedulingLoader {

    private static final String FILE_PATH_KEY = "filePath";
    private static final String DEFAULT_FILE_PATH = "schedulers.json";


    /**
     * Creates a new JsonFileManager instance with a default monitor.
     *
     * @param vertx          the Vertx instance
     * @param defaultMonitor the default scheduling monitor
     */
    public JsonFileScheduleManager(Vertx vertx, SchedulingMonitor<Object> defaultMonitor) {
        super(vertx, defaultMonitor);
    }

    @Override
    protected Future<JsonObject> loadData() {
        String filePath = config.getString(FILE_PATH_KEY, DEFAULT_FILE_PATH);
        LOGGER.info("Loading scheduler data from file: " + filePath);

        FileSystem fs = vertx.fileSystem();

        return fs.exists(filePath)
            .compose(exists -> {
                if (!exists) {
                    LOGGER.warn("Scheduler configuration file does not exist: " + filePath);
                    return Future.succeededFuture(Buffer.buffer());
                }
                return fs.readFile(filePath);
            })
            .compose(buffer -> {
                try {
                    JsonObject data = buffer.toJsonObject();
                    LOGGER.info("Successfully loaded scheduler data from file: " + filePath);
                    return Future.succeededFuture(data);
                } catch (Exception e) {
                    LOGGER.error("Failed to parse JSON from file: " + filePath, e);
                    return Future.failedFuture(new IllegalArgumentException("Invalid JSON in file: " + filePath, e));
                }
            })
            .onFailure(t -> LOGGER.error("Failed to load data from file: " + filePath, t));
    }

    /**
     * Saves the current scheduler configurations to the configured file.
     * This is a utility method for persisting configurations.
     *
     * @param data the data to save
     * @return a Future indicating completion
     */
    public Future<Void> saveData(JsonObject data) {
        String filePath = config.getString(FILE_PATH_KEY, DEFAULT_FILE_PATH);
        LOGGER.info("Saving scheduler data to file: " + filePath);

        return vertx.fileSystem()
            .writeFile(filePath, data.toBuffer())
            .onSuccess(v -> LOGGER.info("Successfully saved scheduler data to file: " + filePath))
            .onFailure(t -> LOGGER.error("Failed to save data to file: " + filePath, t));
    }

    /**
     * Gets the configured file path.
     *
     * @return the file path
     */
    public String getFilePath() {
        return config.getString(FILE_PATH_KEY, DEFAULT_FILE_PATH);
    }

    @Override
    public Future<SchedulingLoader> load() {
        return null;
    }

    @Override
    public JsonObject next() {
        return null;
    }

}
