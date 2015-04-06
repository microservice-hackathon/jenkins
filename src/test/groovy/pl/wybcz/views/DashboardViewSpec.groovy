package pl.wybcz.views

import javaposse.jobdsl.dsl.JobParent
import javaposse.jobdsl.dsl.View
import pl.wybcz.pipeline.util.JobSpecTrait
import pl.wybcz.pipeline.util.XmlComparator
import pl.wybcz.view.ViewBuilder
import spock.lang.Specification

class DashboardViewSpec extends Specification implements JobSpecTrait, XmlComparator {

    JobParent jobParent = createJobParent()

    def 'should produce properly generated dashboard view'() {
        given:
            ViewBuilder viewBuilder = new ViewBuilder(jobParent)

        when:
            View view = viewBuilder.appendDashboard()

        then:
            assertThatBuildPrJobIsProperlyBuiltFor(view)
    }

    void assertThatBuildPrJobIsProperlyBuiltFor(View view) {
        compareXmls("/microservice/views/dashboard.xml", view.node)
    }
}
