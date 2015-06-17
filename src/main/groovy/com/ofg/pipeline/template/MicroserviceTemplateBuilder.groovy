package com.ofg.pipeline.template

import com.ofg.pipeline.domain.GitProject
import com.ofg.pipeline.domain.NexusBuilder
import com.ofg.pipeline.pr.GithubPrBuilder
import com.ofg.pipeline.pr.PrBuilder
import com.ofg.pipeline.pr.StashPrBuilder
import javaposse.jobdsl.dsl.DslFactory

class MicroserviceTemplateBuilder {

    private DslFactory dslFactory
    private PrBuilder prBuilder
    private NexusBuilder nexusBuilder
    private List<GitProject> projects

    static void pipeline(DslFactory dslFactory, @DelegatesTo(MicroserviceTemplateBuilder) Closure closure) {
        MicroserviceTemplateBuilder microserviceTemplateBuilder = new MicroserviceTemplateBuilder(
                dslFactory: dslFactory
        )
        closure.delegate = microserviceTemplateBuilder
        closure.call()
    }

    void buildGithubPrs(@DelegatesTo(GithubPrBuilder) Closure closure) {
        GithubPrBuilder githubPrBuilder = new GithubPrBuilder(
                dslFactory: dslFactory
        )
        closure.delegate = githubPrBuilder
        this.prBuilder = githubPrBuilder
        closure.call()
    }

    void withNexus(@DelegatesTo(NexusBuilder) Closure closure) {
        NexusBuilder builder = new NexusBuilder()
        this.nexusBuilder = builder
        closure.delegate = builder
        closure.call()
    }

    void buildStashPrs(@DelegatesTo(StashPrBuilder) Closure closure) {
        StashPrBuilder stashPrBuilder = new StashPrBuilder(
                dslFactory: dslFactory
        )
        closure.delegate = stashPrBuilder
        this.prBuilder = stashPrBuilder
        closure.call()
    }

    void forProjects(List<GitProject> projects) {
        this.projects = projects
    }

    void buildJobs() {
        MicroserviceJobsBuilder microserviceJobsBuilder = new MicroserviceJobsBuilder(dslFactory, prBuilder, nexusBuilder)
        projects.each {
            microserviceJobsBuilder.buildJobs(it.name, it.cloneUrl)
        }
    }

    void buildViews() {
        Map<String, List<String>> realmMultimap = new SuffixRealmParser().convertToRealmMultimap(projects)
        realmMultimap.each { String realm, List<String> projects ->
            new MicroserviceViewsBuilder(dslFactory).buildViews(realm, projects)
        }
    }

    void buildViews(RealmConverter realmConverter) {
        realmConverter.convertToRealmMultimap(projects).each { String realm, List<String> projects ->
            new MicroserviceViewsBuilder(dslFactory).buildViews(realm, projects)
        }
    }
}
