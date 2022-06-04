package ru.job4j.articles.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.job4j.articles.model.Article;
import ru.job4j.articles.model.Word;
import ru.job4j.articles.service.generator.ArticleGenerator;
import ru.job4j.articles.store.Store;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SimpleArticleService implements ArticleService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleArticleService.class.getSimpleName());
    private final ArticleGenerator articleGenerator;
    private static final Runtime runtime = Runtime.getRuntime();
    private static final long minMemory = runtime.totalMemory() / 10;
    private static final long MB = 1024 * 1024;

    public SimpleArticleService(ArticleGenerator articleGenerator) {
        this.articleGenerator = articleGenerator;
    }

    @Override
    public void generate(Store<Word> wordStore, int count, Store<Article> articleStore) {
        LOGGER.info("Генерация статей в количестве {}", count);
        LOGGER.info("Минимальный порог свободной памяти {}", minMemory / MB);
        var words = wordStore.findAll();
        List<Article> articles = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            if (runtime.freeMemory() <= minMemory) {
                LOGGER.info("Памяти осталось менее 10% - {} Мб", runtime.freeMemory() / MB);
                LOGGER.info("Выгружаю данные в БД");
                articles.forEach(articleStore::save);
                articles = new ArrayList<>();
                System.gc();
            } else {
                LOGGER.info("Сгенерирована статья № {}", i);
                articles.add(articleGenerator.generate(words));
            }
        }
        articles.forEach(articleStore::save);
    }
}
