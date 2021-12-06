package com.murphyl.etl.core;

import com.murphyl.etl.WorkflowLauncher;
import com.murphyl.etl.support.Environments;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
        WorkflowLauncher.main("2021/11/11", Environments.getResource("src/test/resources/jobs/test_01.xml"));
    }

    @Test
    public void  test03() throws ExecutionException, InterruptedException, TimeoutException {
        WorkflowLauncher.main(
                "2021/11/11",
                Environments.getResource("src/test/resources/jobs/test_03.xml")
        );
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException, TimeoutException {
        new WorkflowLauncherTest().test03();
    }

}
