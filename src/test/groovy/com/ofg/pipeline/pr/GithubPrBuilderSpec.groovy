package com.ofg.pipeline.pr

import javaposse.jobdsl.dsl.Job
import javaposse.jobdsl.dsl.JobParent
import com.ofg.pipeline.util.JobSpecTrait
import com.ofg.pipeline.util.XmlComparator
import spock.lang.Specification

class GithubPrBuilderSpec extends Specification implements JobSpecTrait, XmlComparator {

    JobParent jobParent = createJobParent()

    def 'should produce properly generated Github Pr builder XML'() {
        given:
            String organizationUrl = 'https://github.com/microhackathon-2015-03-juglodz'
            String projectName = 'client-service-lodz'
            String cronToPollScm = '*/2 * * * *'
            String organization = 'microhackathon-2015-03-juglodz'
            String whitelistedUser = 'microservice-hackathon-bot'
        and:
            GithubPrBuilder githubPrBuilder = new GithubPrBuilder(
                    dslFactory: jobParent,
                    organizationUrl: organizationUrl,
                    cronToPollScm: cronToPollScm,
                    organizationName: organization,
                    whitelistedUsers: [whitelistedUser]
            )

        when:
            Job job = githubPrBuilder.buildPrJob(projectName)

        then:
            assertThatBuildPrJobIsProperlyBuiltFor(job)
    }

    void assertThatBuildPrJobIsProperlyBuiltFor(Job job) {
        compareXmls("/microservice/prs/build-pr-github.xml", job.node)
    }
}
