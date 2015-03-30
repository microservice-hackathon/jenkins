package pl.wybcz

import javaposse.jobdsl.dsl.Job
import javaposse.jobdsl.dsl.JobParent
import javaposse.jobdsl.dsl.View
import org.junit.Ignore
import spock.lang.Specification

@Ignore
class MicroserviceTemplateBuilderSpec extends Specification implements JobSpecTrait, XmlComparator {

    private static final String JOB_NAME = 'test-job'

    JobParent jobParent = createJobParent()

    void 'test XML output for jobs'() {
        given:
            MicroserviceTemplateBuilder builder = new MicroserviceTemplateBuilder(jobParent)

        when:
            List<Job> jobs = builder.buildJobs(JOB_NAME, 'git@github.com:example/example.git')

        then:
            jobs.each {
                assertThatJobIsOk(it)
            }
    }

    void 'test XML output for views'() {
        given:
            MicroserviceTemplateBuilder builder = new MicroserviceTemplateBuilder(jobParent)

        when:
            List<View> views = builder.buildViews('waw', ['build-waw', 'publish-waw'])

        then:
            views.each {
                assertThatViewIsOk(it)
            }
    }

    void assertThatJobIsOk(Job job) {
        String fileName = job.name - "$JOB_NAME-"
        compareXmls("/microservice/jobs/${fileName}.xml", job.node)
    }

    void assertThatViewIsOk(View view) {
        String fileName = view.name
        compareXmls("/microservice/views/${fileName}.xml", view.node)
    }

}
