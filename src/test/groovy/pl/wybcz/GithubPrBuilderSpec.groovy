package pl.wybcz

import javaposse.jobdsl.dsl.Job
import javaposse.jobdsl.dsl.JobParent
import spock.lang.Specification

class GithubPrBuilderSpec extends Specification implements JobSpecTrait, XmlComparator {

    JobParent jobParent = createJobParent()

    def 'should produce properly generated Github Pr builder XML'() {
        given:
            GithubPrBuilder githubPrBuilder = new GithubPrBuilder(jobParent)
        and:
            String organizationUrl = 'https://github.com/microhackathon-2015-03-juglodz'
            String projectName = 'client-service-lodz'
            String cronToPollScm = '*/2 * * * *'
            String organization = 'microhackathon-2015-03-juglodz'
            String whitelistedUser = 'microservice-hackathon-bot'

        when:
            Job job = githubPrBuilder.buildPrJob(organizationUrl, projectName, cronToPollScm, organization, [whitelistedUser])

        then:
            assertThatBuildPrJobIsProperlyBuiltFor(job)
    }

    void assertThatBuildPrJobIsProperlyBuiltFor(Job job) {
        compareXmls("/microservice/prs/build-pr-github.xml", job.node)
    }
}
