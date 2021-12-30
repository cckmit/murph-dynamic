package com.murphyl.saas.support.web.profile.loader;

import com.murphyl.saas.modules.GraalJsEngine;
import com.murphyl.saas.support.web.profile.manager.RouteProfileLoader;

/**
 * -
 *
 * @date: 2021/12/30 16:03
 * @author: murph
 */
public abstract class AbstractRouteProfileLoader implements RouteProfileLoader {

    protected GraalJsEngine graalJsEngine;

    public void setGraalJsEngine(GraalJsEngine graalJsEngine) {
        this.graalJsEngine = graalJsEngine;
    }

}
