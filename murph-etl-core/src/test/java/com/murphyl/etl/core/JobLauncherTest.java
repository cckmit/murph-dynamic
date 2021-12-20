package com.murphyl.etl.core;

import com.murphyl.etl.JobLauncher;
import com.murphyl.etl.support.Environments;
import org.junit.jupiter.api.Test;

class JobLauncherTest {

    @Test
    public void test05() {
        JobLauncher.main(
                "2021/11/11",
                Environments.getResource("src/test/resources/jobs/test_05.xml")
        );
    }

}
