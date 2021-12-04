package com.murphyl.etl.task.extractor;

import com.murphyl.dataframe.Dataframe;
import com.murphyl.etl.task.loader.ConsoleLoader;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Properties;

/**
 * -
 * author: murph
 * 2021/12/4 - 12:12
 */
class RandomExtractorTest {


    @Test
    public void test() {
        String dsl = "{\n" +
                "    \"engine\": \"jexl\",\n" +
                "    \"columns\": [\n" +
                "        {\n" +
                "            \"name\": \"id\",\n" +
                "            \"expr\": \"${idGenerator.next()}\",\n" +
                "            \"extra\": {\n" +
                "                \"idGenerator\": \"${seq()}\"\n" +
                "            }\n" +
                "        },\n" +
                "        {\n" +
                "            \"name\": \"name\",\n" +
                "            \"expr\": \"${uuid()}\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"name\": \"ts\",\n" +
                "            \"expr\": \"${timestamp()}\"\n" +
                "        }\n" +
                "    ]\n" +
                "}";
        Dataframe df = new RandomExtractor().extract(dsl, new HashMap<>(), new Properties());
        ConsoleLoader loader = new ConsoleLoader();
        loader.load(new HashMap<>(), df, new Properties());
        loader.load(new HashMap<>(), df.select("id", "name"), new Properties());
    }

}