package com.ofg.pipeline.pr

import javaposse.jobdsl.dsl.Job
import javaposse.jobdsl.dsl.JobParent
import com.ofg.pipeline.util.JobSpecTrait
import com.ofg.pipeline.util.XmlComparator
import spock.lang.Specification

class StashPrBuilderSpec extends Specification implements JobSpecTrait, XmlComparator {

    JobParent jobParent = createJobParent()

    def 'should produce properly generated Github Pr builder XML'() {
        given:
            String stashHost = 'stash.host.net'
            String cronToPollScm = '* * * * *'
            String mergeStrategy = 'default'
            StashPrBuilder.FastForwardMode fastForwardMode = StashPrBuilder.FastForwardMode.FF
            String username = 'username'
            String password = 'password'
            String projectCode = 'PROJECT_CODE'
            String repoName = 'REPO_NAME'
        and:
            StashPrBuilder stashPrBuilder = new StashPrBuilder(
                    dslFactory: jobParent,
                    stashHost: stashHost,
                    cronToPollScm: cronToPollScm,
                    mergeStrategy: mergeStrategy,
                    fastForwardMode: fastForwardMode,
                    username: username,
                    password: password,
                    projectCode: projectCode,
                    repoName: repoName
            )

        when:
            Job job = stashPrBuilder.buildPrJob(repoName)

        then:
            assertThatBuildPrJobIsProperlyBuiltFor(job)
    }

    void assertThatBuildPrJobIsProperlyBuiltFor(Job job) {
        compareXmls("/microservice/prs/build-pr-stash.xml", job.node)
    }
}
