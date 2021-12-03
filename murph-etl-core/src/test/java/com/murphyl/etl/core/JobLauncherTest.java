package com.murphyl.etl.core;

import org.junit.jupiter.api.Test;

import java.util.UUID;

class JobLauncherTest {

    @Test
    public void testEmpty() {
        new JobLauncher(UUID.randomUUID()).call();
    }

}
