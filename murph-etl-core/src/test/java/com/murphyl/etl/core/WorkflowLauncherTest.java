package com.murphyl.etl.core;

import com.murphyl.etl.WorkflowLauncher;
import com.murphyl.etl.support.Environments;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sqlite.SQLiteConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;


class WorkflowLauncherTest {

    @Test
    public void testEmpty() throws ExecutionException, InterruptedException, TimeoutException {
        WorkflowLauncher.main();
    }

    @Test
    public void  testNoSchema() throws ExecutionException, InterruptedException, TimeoutException {
        WorkflowLauncher.main("2021/11/11");
    }

    @Test
    public void  test01() throws ExecutionException, InterruptedException, TimeoutException {
        WorkflowLauncher.main("2021/11/11", Environments.getResource("src/test/resources/jobs/test_03.xml"));
    }

    @Test
    public void  test03() throws ExecutionException, InterruptedException, TimeoutException {
        WorkflowLauncher.main(
                "2021/11/11",
                Environments.getResource("src/test/resources/jobs/test_03.xml")
        );
    }

    @Test
    public void  test04() throws Exception {
        Connection connection = DriverManager.getConnection("jdbc:sqlite:file::memory:?cache=shared");
        Statement statement = connection.createStatement();
        statement.addBatch("create table employee(id int primary key not null, name text not null, age int not null, address char(50), salary real)");
        for (int i = 0; i < 1000; i++) {
            String name = RandomStringUtils.random(5, true, false);
            int age = RandomUtils.nextInt(15, 65);
            String address = RandomStringUtils.random(45, true, false);
            double salary = RandomUtils.nextDouble(2000, 15000);
            statement.addBatch(String.format("insert into employee values(%s, '%s', %s, '%s', %s)", i, name, age, address, salary));
        }
        statement.executeBatch();
        WorkflowLauncher.main(
                "2021/11/11",
                Environments.getResource("src/test/resources/jobs/test_04.xml")
        );
    }

    @Test
    public void  test05() throws ExecutionException, InterruptedException, TimeoutException {
        WorkflowLauncher.main(
                "2021/11/11",
                Environments.getResource("src/test/resources/jobs/test_05.xml")
        );
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException, TimeoutException {
        new WorkflowLauncherTest().test03();
    }

}
