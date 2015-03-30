package pl.wybcz

import javaposse.jobdsl.dsl.Job
import javaposse.jobdsl.dsl.JobParent
import org.junit.Ignore
import spock.lang.Specification

@Ignore
class GithubPrBuilderSpec extends Specification implements JobSpecTrait, XmlComparator {

    JobParent jobParent = createJobParent()

    def 'should produce properly generated Github Pr builder XML'() {
        given:
            GithubPrBuilder githubPrBuilder = new GithubPrBuilder()

        when:
            Job job = githubPrBuilder.buildPrJob(jobParent)

        then:
            assertThatBuildPrJobIsProperlyBuiltFor(job)
    }

    void assertThatBuildPrJobIsProperlyBuiltFor(Job job) {
        compareXmls("/microservice/prs/build-pr-github.xml", job.node)
    }
}
