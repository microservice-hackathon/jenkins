package com.ofg.pipeline.template
import javaposse.jobdsl.dsl.Item
import javaposse.jobdsl.dsl.JobParent
import javaposse.jobdsl.dsl.View
import com.ofg.pipeline.util.JobSpecTrait
import com.ofg.pipeline.util.XmlComparator
import com.ofg.pipeline.domain.GitProject
import spock.lang.Specification

class MicroserviceTemplateBuilderSpec extends Specification implements JobSpecTrait, XmlComparator {

    private static final String JOB_NAME = 'test-job'

    JobParent jobParent = createJobParent()

    void 'should generate proper job XMLs'() {
        given:
            MicroserviceTemplateBuilder.pipeline(jobParent) {
                forProjects([new GitProject(JOB_NAME, 'git@github.com:example/example.git')])
                buildJobs()
            }

        when:
            Set<Item> jobs = jobParent.getReferencedJobs()

        then:
            jobs.each {
                assertThatJobIsOk(it)
            }
    }

    void 'should generate proper job with github pr building'() {
        given:
            MicroserviceTemplateBuilder.pipeline(jobParent) {
                forProjects([new GitProject(JOB_NAME, 'git@github.com:example/example.git')])
                buildGithubPrs {
                    organizationUrl 'https://github.com/microhackathon-2015-03-juglodz'
                    cronToPollScm '*/2 * * * *'
                    organizationName 'microhackathon-2015-03-juglodz'
                    whitelistedUsers(['microservice-hackathon-bot'])
                }
                buildJobs()
            }

        when:
            Set<Item> jobs = jobParent.getReferencedJobs()
        then:
            allJobsWereCreatedIncludingPrBuild(jobs)

    }

    private void  allJobsWereCreatedIncludingPrBuild(Set<Item> jobs) {
        assert jobs.size() == 4
        assert jobs.collect { it.name }.contains('test-job-pr-build')
    }

    void 'should generate proper views XMLs'() {
        given:
            MicroserviceTemplateBuilder.pipeline(jobParent) {
                forProjects([
                        new GitProject('build-waw', 'git@github.com:example/example.git'),
                        new GitProject('publish-waw', 'git@github.com:example/example.git')
                ])
                buildViews()
            }
        when:
            Set<View> views = jobParent.getReferencedViews()

        then:
            views.each {
                assertThatViewIsOk(it)
            }
    }

    void assertThatJobIsOk(Item job) {
        String fileName = job.name - "$JOB_NAME-"
        compareXmls("/microservice/jobs/${fileName}.xml", job.node)
    }

    void assertThatViewIsOk(View view) {
        String fileName = view.name
        compareXmls("/microservice/views/${fileName}.xml", view.node)
    }

}
