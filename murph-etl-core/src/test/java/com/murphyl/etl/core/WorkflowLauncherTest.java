package com.murphyl.etl.core;

import com.murphyl.etl.WorkflowLauncher;
import com.murphyl.etl.consts.Environments;

import org.junit.jupiter.api.Test;

import java.util.UUID;


class WorkflowLauncherTest {

    @Test
    public void testEmpty() {
        new WorkflowLauncher(UUID.randomUUID()).call();
    }

    @Test
    public void  testNoSchema() {
        new WorkflowLauncher(UUID.randomUUID(), "2021/11/11").call();
    }

    @Test
    public void  test01() {
        new WorkflowLauncher(UUID.randomUUID(), "2021/11/11", Environments.getResource("src/test/resources/jobs/test_01.xml")).call();
    }

    @Test
    public void  test03() {
        new WorkflowLauncher(UUID.randomUUID(), "2021/11/11", Environments.getResource("src/test/resources/jobs/test_03.xml")).call();
    }

}
