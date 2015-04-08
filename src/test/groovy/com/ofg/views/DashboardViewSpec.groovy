package com.ofg.views

import javaposse.jobdsl.dsl.JobParent
import javaposse.jobdsl.dsl.View
import com.ofg.pipeline.util.JobSpecTrait
import com.ofg.pipeline.util.XmlComparator
import com.ofg.view.ViewBuilder
import spock.lang.Specification

class DashboardViewSpec extends Specification implements JobSpecTrait, XmlComparator {

    JobParent jobParent = createJobParent()

    def 'should produce properly generated dashboard view'() {
        given:
            ViewBuilder viewBuilder = new ViewBuilder(jobParent)

        when:
            View view = viewBuilder.buildDashboard()

        then:
            assertThatBuildPrJobIsProperlyBuiltFor(view)
    }

    void assertThatBuildPrJobIsProperlyBuiltFor(View view) {
        compareXmls("/microservice/views/dashboard.xml", view.node)
    }
}
