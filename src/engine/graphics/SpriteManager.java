package engine.graphics;

import javafx.scene.image.Image;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class SpriteManager {
    private static final SpriteManager instance = new SpriteManager();
    private final Map<String, Image> cache = new HashMap<>();

    private SpriteManager() {}

    public static SpriteManager getInstance() {
        return instance;
    }

    public Image getSprite(String path) {
        if (cache.containsKey(path)) {
            return cache.get(path);
        }

        try {
            // Путь относительно корня проекта (рабочей директории)
            InputStream stream = new FileInputStream(path);
            Image image = new Image(stream);
            if (image.isError()) {
                System.err.println("Ошибка загрузки изображения: " + path);
                return null;
            }
            cache.put(path, image);
            return image;
        } catch (Exception e) {
            System.err.println("Ресурс не найден или ошибка чтения: " + path);
            e.printStackTrace();
            return null;
        }
    }

    public void clearCache() {
        cache.clear();
    }
}